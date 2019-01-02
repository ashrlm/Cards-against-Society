package ashrlm.cardsagainstsociety;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class setupNewGame extends AppCompatActivity {

    private ArrayList<CheckBox> checkboxes = new ArrayList<>();
    private String TAG = "ashrlm.cas";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_setup_new_game);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        myToolbar.setTitle("Cards against Society - Select decks");
        setSupportActionBar(myToolbar);

        LinearLayout layout = findViewById(R.id.layout_decks);
        //Create checkboxes for deck selection
        //White boxes
        String deckColor = "white";
        ArrayList<String> whiteDecks = getDecks(deckColor);
        for (String deck : whiteDecks) {
            CheckBox whiteBox = new CheckBox(this);
            whiteBox.setText(deck);
            whiteBox.setTag(deckColor);
            whiteBox.setTextSize(18);
            CompoundButtonCompat.setButtonTintList(whiteBox, ColorStateList.valueOf(Color.parseColor("#CCCCCC")));
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
            CompoundButtonCompat.setButtonTintList(blackBox, ColorStateList.valueOf(Color.parseColor("#CCCCCC")));
            checkboxes.add(blackBox);
            layout.addView(blackBox);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu ) {
        getMenuInflater().inflate( R.menu.menu, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_logout:
                logout();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void logout () {
        Log.d(TAG, "signOut()");

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            Log.d(TAG, "signOut(): success");
                            finish();
                        } else {
                            Log.d(TAG, "signOut(): failed");
                        }
                    }
                });
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

    public void tryPlay(View view) {
        if (validateSettings()) {
            ArrayList<ArrayList<String>> decks = getSelectedDecks();
            Intent gotoLobby = new Intent(getApplicationContext(), mainGame.class);
            Bundle data = new Bundle();
            data.putStringArrayList("whiteCards", decks.get(0));
            data.putStringArrayList("blackCards", decks.get(1));
            data.putInt("role", 0x1); //Role as card tsar - Only 1 per game
            gotoLobby.putExtras(data);
            finish();
            startActivity(gotoLobby);
        }
    }

    private boolean validateSettings() {

        boolean whiteDeckUsed = false;
        boolean blackDeckUsed = false;

        for (CheckBox deckOption : checkboxes) {
            if (deckOption.getTag().equals("white") && deckOption.isChecked()) { whiteDeckUsed = true; }
            if (deckOption.getTag().equals("black") && deckOption.isChecked()) { blackDeckUsed = true; }
        }

        return whiteDeckUsed && blackDeckUsed;

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