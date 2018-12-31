package ashrlm.cardsagainstsociety;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class homepage extends Activity {

    private static final String TAG = "ashrlm.cas";
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        mGoogleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
    }

    private void startSignInIntent() {
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {

            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
            } catch (ApiException apiException) {
                String message = apiException.getMessage();
                if (message == null || message.isEmpty()) {
                    message = "Sign-in Error";
                }

                new AlertDialog.Builder(this)
                        .setMessage(message)
                        .setNeutralButton(android.R.string.ok, null)
                        .show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (GoogleSignIn.getLastSignedInAccount(homepage.this) == null) {
            startSignInIntent();
        }
    }

    //---------------------------------Button response code-----------------------------------------
    public void newGameButton(View view) {
        Intent gotoGameSetup = new Intent(this, setupNewGame.class);
        startActivity(gotoGameSetup);
    }

    public void joinGameButton(View view) {
        Intent gotoLobby = new Intent(this, mainGame.class);
        Bundle data = new Bundle();
        data.putInt("role", 0x0); //Allow join any game
        gotoLobby.putExtras(data);
        startActivity(gotoLobby);
    }

    public void editDecksButton(View view) {
        Intent gotoEditDecks = new Intent(this, editDecks.class);
        startActivity(gotoEditDecks);
    }

    public void creditsButton(View view) {
        Intent gotoCredits = new Intent(this, credits.class);
        startActivity(gotoCredits);
    }

}