package ashrlm.cardsagainstsociety;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class setupNewGame extends Activity {

    private ArrayList<CheckBox> checkboxes = new ArrayList<>();
    private EditText deckTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_new_game);

        LinearLayout layout = findViewById(R.id.layout_decks);
        //Deck Title
        deckTitle = new EditText(this);
        deckTitle.setHint("Deck title");
        layout.addView(deckTitle);
        //Create checkboxes for deck selection
        //White boxes
        String deckColor = "white";
        TextView header = new TextView(this);
        header.setText("Choose playable decks");
        header.setTextSize(18);
        layout.addView(header);
        ArrayList<String> whiteDecks = getDecks(deckColor);
        for (String deck : whiteDecks) {
            CheckBox whiteBox = new CheckBox(this);
            whiteBox.setText(deck);
            whiteBox.setTag(deckColor);
            whiteBox.setTextSize(18);
            checkboxes.add(whiteBox);
            layout.addView(whiteBox);
        } //Black boxes
        deckColor = "black";
        TextView divider = new TextView(this);
        layout.addView(divider);
        ArrayList<String> blackDecks = getDecks(deckColor);
        for (String deck : blackDecks) {
            CheckBox blackBox = new CheckBox(this);
            blackBox.setText(deck);
            blackBox.setTag(deckColor);
            blackBox.setTextSize(18);
            checkboxes.add(blackBox);
            layout.addView(blackBox);
        }
        //Start game button
        Button startGameBtn = new Button(this);
        startGameBtn.setText("Start Game");
        startGameBtn.setTextSize(21);
        startGameBtn.setBackgroundResource(R.drawable.button);
        final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
        startGameBtn.setWidth((int) (351 * scale + .5f));
        startGameBtn.setHeight((int) (100 * scale + .5f));
        startGameBtn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams ll = (LinearLayout.LayoutParams)startGameBtn.getLayoutParams();
        ll.gravity = Gravity.CENTER;
        ll.setMargins(ll.leftMargin,
                (int) (ll.topMargin+(10*scale + .5f)),
                ll.rightMargin,
                ll.bottomMargin);
        startGameBtn.setLayoutParams(ll);
        startGameBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (validateSettings()) {
                            Intent gotoNewGame = new Intent(getApplicationContext(), newGame.class);
                            Bundle data = new Bundle();
                            data.putString("gameTitle", deckTitle.getText().toString());
                            ArrayList<ArrayList<String>> decks = getSelectedDecks();
                            data.putStringArrayList("whiteCards", decks.get(0));
                            data.putStringArrayList("blackCards", decks.get(1));
                            startActivity(gotoNewGame);
                        }
                    }
                }
        );
        layout.addView(startGameBtn);
    }

    private ArrayList<String> getDecks (String deckColor) {
        //Custom decks
        File tmpFile = new File(getFilesDir().getAbsolutePath() + "/" + deckColor + "/");
        if (!tmpFile.exists()) { tmpFile.mkdir(); } //Prevent crashes caused by no custom decks existing
        ArrayList<String> decks = new ArrayList<>(Arrays.asList(tmpFile.list()));
        //Builtin decks
        final AssetManager assetmanager = getApplicationContext().getAssets();
        try {
            decks.addAll(Arrays.asList(assetmanager.list(deckColor)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return decks;
    }

    private boolean validateSettings() {
        if (deckTitle.getText().toString().equals("")) { return false; } //No game title

        boolean whiteDeckUsed = false;
        boolean blackDeckUsed = false;

        for (CheckBox deckOption : checkboxes) {
            if (deckOption.getTag().equals("white") && deckOption.isChecked()) { whiteDeckUsed = true; }
            if (deckOption.getTag().equals("black") && deckOption.isChecked()) { blackDeckUsed = true; }
        }

        return whiteDeckUsed&&blackDeckUsed;

    }

    private ArrayList<ArrayList<String>> getSelectedDecks() {
        ArrayList<String> whiteDecks = new ArrayList<>();
        ArrayList<String> blackDecks = new ArrayList<>();
        for (CheckBox deck : checkboxes) {
            if (deck.isChecked()) {
                if (deck.getTag().equals("white")) {
                    File whiteDeck = new File(getFilesDir().getAbsolutePath() + "/white/" + deck.getText().toString());
                    if (whiteDeck.exists()) {
                        try {
                            BufferedReader br = new BufferedReader(new FileReader(whiteDeck));
                            String line;

                            while ((line = br.readLine()) != null) {
                                whiteDecks.add(line);
                            }
                            br.close();
                        }
                        catch (IOException e) {
                        }

                    } else {
                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(getAssets().open("white/" + deck.getText().toString())))) {

                            String mLine;
                            while ((mLine = reader.readLine()) != null) {
                                whiteDecks.add(mLine);
                            }
                        } catch (IOException e) {
                        }
                    }

                } else if (deck.getTag().equals("black")) {
                    File blackDeck = new File(getFilesDir().getAbsolutePath() + "/black/" + deck.getText().toString());
                    if (blackDeck.exists()) {
                        try {
                            BufferedReader br = new BufferedReader(new FileReader(blackDeck));
                            String line;

                            while ((line = br.readLine()) != null) {
                                blackDecks.add(line);
                            }
                            br.close();
                        }
                        catch (IOException e) {
                        }

                    } else {
                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(getAssets().open("black/" + deck.getText().toString())))) {

                            String mLine;
                            while ((mLine = reader.readLine()) != null) {
                                blackDecks.add(mLine);
                            }
                        } catch (IOException e) {

                        }
                    }
                }
            }
        }
        ArrayList<ArrayList<String>> decks = new ArrayList<>();
        decks.add(whiteDecks);
        decks.add(blackDecks);
        return decks;
    }
}