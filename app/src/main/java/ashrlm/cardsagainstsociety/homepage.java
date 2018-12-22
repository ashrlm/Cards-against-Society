package ashrlm.cardsagainstsociety;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class homepage extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
    }

    private void startSignInIntent() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        Intent intent = signInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.i(TAG, "signInResult: Success");
            // Signed in successfully
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.i(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
        if (GoogleSignIn.getLastSignedInAccount(this) == null) {

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
        Intent gotoLobby = new Intent(this, gameLobby.class);
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