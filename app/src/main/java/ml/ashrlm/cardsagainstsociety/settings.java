package ml.ashrlm.cardsagainstsociety;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class settings extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
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

        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            // Log.d(TAG, "signOut(): success");
                            onResume();
                        } else {
                            // Log.d(TAG, "signOut(): failed");
                        }
                    }
                });
    }

    //------------------------------Button Response Code--------------------------------------------

    public void preferredRoleButton(View view) {
        //TODO: Add alert dialog with radio buttons that enable OK to set role and store in SP
        comingSoonDialog();
    }

    public void tutorialButton(View view) {
        //TODO: Set all tutorial toast points in SP to true
        comingSoonDialog();
    }

    public void creditsButton(View view) {
        Intent showCredits = new Intent(this, credits.class);
        startActivity(showCredits);
    }

    private void comingSoonDialog() {
        AlertDialog.Builder comingSoonBuilder = new AlertDialog.Builder(this);
        comingSoonBuilder.setMessage("Coming Soon!");
        comingSoonBuilder.create().show();
    }
}
