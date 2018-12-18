package ashrlm.cardsagainstsociety;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static android.content.ContentValues.TAG;

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
                ll.bottomMargin,
        startGameBtn.setLayoutParams(ll);
        startGameBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (validateSettings()) {
                            //TODO: Generate intent to go to newGame
                        }
                    }
                }
        );
        layout.addView(startGameBtn);
    }

    private ArrayList<String> getDecks (String deckColor) {
        ArrayList<String> decks = new ArrayList<>();
        //Custom decks
        File tmpFile = new File(getFilesDir().getAbsolutePath() + "/" + deckColor + "/");
        decks.addAll(Arrays.asList(tmpFile.list()));
        //Builtin decks
        final AssetManager assetmanager = getApplicationContext().getAssets();
        try {
            decks.addAll(Arrays.asList(assetmanager.list(deckColor)));
        } catch (IOException e) {
            Log.e(TAG, "Error getting builtin decks. Error code: " + e);
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

        if (!(whiteDeckUsed&&blackDeckUsed)) { return false; } //No white/black deck selected

        return true;

    }
}