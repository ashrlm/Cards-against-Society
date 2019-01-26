package ml.ashrlm.cardsagainstsociety;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.FileOutputStream;

public class newDeck extends AppCompatActivity {

    private EditText deck_title;
    private EditText deck_content;
    private String TAG = "ashrlm.cas";
    private SharedPreferences mSharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_new_deck);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        myToolbar.setTitle("Cards against Society - New Deck");
        setSupportActionBar(myToolbar);
        deck_title = findViewById(R.id.deck_title);
        deck_content = findViewById(R.id.deck_content);
        mSharedPrefs = getSharedPreferences("CAS_PREFS", MODE_PRIVATE);
        int DECK_COLOR_INFO = mSharedPrefs.getInt("DECK_COLOR_INFO", 1);
        if (DECK_COLOR_INFO != 0) {
            Toast.makeText(this, getString(R.string.DECK_COLOR_INFO), Toast.LENGTH_SHORT).show();
            if (DECK_COLOR_INFO == 1) {
                mSharedPrefs.edit().putInt("DECK_COLOR_INFO", 0).apply();
            }
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
       // Log.d(TAG, "signOut()");

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                           // Log.d(TAG, "signOut(): success");
                            finish();
                        } else {
                           // Log.d(TAG, "signOut(): failed");
                        }
                    }
                });
    }

    public void createDeck(View view) {
        String deckPath;
        String deckContent;
        String deckFolder;

        RadioButton white_deck = findViewById(R.id.radioWhite);
        RadioButton black_deck = findViewById(R.id.radioBlack);

        String deckTitle = deck_title.getText().toString();
        String deckCards = deck_content.getText().toString();
        if (deckTitle != null || deckCards.isEmpty()) {
            if (!deckTitle.endsWith(".txt")) {
                deckTitle += ".txt";
            }
        } else {
            return; //No title - Do nothing
        }

        if (deckCards == null || deckCards.isEmpty()) {
            return; //No cards - Do nothing
        }

        deckContent = deck_content.getText().toString();

        if (white_deck.isChecked()) {
            deckPath = deckTitle;
            deckFolder = "/white";
            File white_dir = new File(getFilesDir(), "white");
            if (!white_dir.exists()) {
                white_dir.mkdirs();
            }
        } else if (black_deck.isChecked()) {
            deckPath = deckTitle;
            deckFolder = "/black";
            File black_dir = new File(getFilesDir(), "black");
            if (!black_dir.exists()) {
                black_dir.mkdirs();
            }
        } else {
            //Nothing selected - Do nothing
            return;
        }

        FileOutputStream outputStream;

        try {
            File new_deck = new File(getFilesDir().getPath() + deckFolder, deckPath);
            outputStream = new FileOutputStream(new_deck);
            outputStream.write(deckContent.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finish();
    }

}
