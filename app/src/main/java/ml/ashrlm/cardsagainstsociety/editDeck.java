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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.google.android.gms.wearable.DataMap.TAG;

public class editDeck extends AppCompatActivity {

    private String deckPath;
    private String deckType;
    private EditText new_deck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_edit_deck);
        deckPath = getIntent().getStringExtra("path");
        deckType = getIntent().getStringExtra("card_type");
        new_deck = findViewById(R.id.deck_title);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        myToolbar.setTitle("Cards against Society - Editing " + deckPath.substring(0, deckPath.length() - 4));
        setSupportActionBar(myToolbar);
        //Read data to add to new_deck

        FileInputStream fis;
        try {
            fis = new FileInputStream(getFilesDir().getPath() + '/' + deckType + '/' + deckPath);
            StringBuffer fileContent = new StringBuffer();

            byte[] buffer = new byte[1024];
            int n;

            while ((n = fis.read(buffer)) != -1)
            {
                fileContent.append(new String(buffer, 0, n));
            }
            new_deck.setText(fileContent);
        } catch (IOException e) {
            e.printStackTrace();
           // Log.e(TAG, String.valueOf(e));
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

    public void updateDeck(View v) {

        if (new_deck.getText() == null || new_deck.getText().toString().isEmpty()) {
            return; //Do nothing - Empty
        }

        String fileContents = new_deck.getText().toString();
        FileOutputStream outputStream;

        try {
            new File("test");
            outputStream = new FileOutputStream(new File(String.format("%s/%s%s", getFilesDir().getPath(), deckType, deckPath)));
            outputStream.write(fileContents.getBytes());
            outputStream.close();
            finish();
        } catch (Exception e) {
           // Log.e(TAG, "Could not save to file. Error code: " + e + " DECKPATH: " + deckPath);
            e.printStackTrace();
        }
    }

}
