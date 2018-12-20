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
//        //Setup Sign-in client
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
//                //BUG: Returns error code 4 & no user selection
//                .requestServerAuthCode("@string/API_KEY")
//                .requestProfile()
//                .build();
//        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
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

    //------------------------------Code for responding to buttons----------------------------------
    public void newGameButton(View view) {
        Intent gotoNewGame = new Intent(this, setupNewGame.class);
        startActivity(gotoNewGame);
    }

    public void joinGameButton(View view) {
        Intent gotoJoinGame = new Intent(this, joinGame.class);
        startActivity(gotoJoinGame);
    }

    public void editDecksButton(View view) { //TODO: Add edit decks button which calls this

        Intent gotoEditDecks = new Intent(this, editDecks.class);
        startActivity(gotoEditDecks);
    }

    public void creditsButton(View view) {
        Intent gotoCredits = new Intent(this, credits.class);
        startActivity(gotoCredits);
    }

}