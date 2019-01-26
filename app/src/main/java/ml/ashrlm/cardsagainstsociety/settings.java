package ml.ashrlm.cardsagainstsociety;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class settings extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private SharedPreferences mSharedPrefs;

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
        mSharedPrefs = getSharedPreferences("CAS_PREFS", MODE_PRIVATE);
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

    @Override
    public void onResume() {
        super.onResume();
        //Show tutorial toasts
        String[] messages = {
                "Use the [PREFERRED ROLE] button to increase chances of getting that role in a game",
                "I'm assuming you know what the [TUTORIAL] button does",
                "Check out the [CREDITS] button to have a look at my website"
        };
        String[] spKeys = {
                "PREFERRED_ROLE",
                "TUTORIAL_JOKE",
                "CREDITS_INFO"
        };
        showToasts(messages, spKeys);
    }

    private void logout () {
        // Log.d(TAG, "signOut()");

        mGoogleSignInClient.signOut();
    }

    //------------------------------Button Response Code--------------------------------------------

    public void preferredRoleButton(View view) {
        // Setup prefRoleView
        View layoutInflated = getLayoutInflater().inflate(R.layout.alert_dialog_selection, null);
        ((ViewGroup) layoutInflated).removeView(layoutInflated.findViewById(R.id.tryPlayButton));
        LinearLayout rolesLayout = layoutInflated.findViewById(R.id.layout_decks);

        int currentPref = mSharedPrefs.getInt("PREF_ROLE", 0);

        final RadioGroup rolesButtonGroup = new RadioGroup(this);
        rolesLayout.addView(rolesButtonGroup);

        RadioButton roleAny = new RadioButton(this);
        roleAny.setText("Any");
        roleAny.setTag(0);
        rolesButtonGroup.addView(roleAny);
        if (currentPref == 0) { rolesButtonGroup.check(roleAny.getId()); }
        RadioButton roleCzar = new RadioButton(this);
        roleCzar.setText("Czar");
        roleCzar.setTag(1);
        rolesButtonGroup.addView(roleCzar);
        if (currentPref == 1) { rolesButtonGroup.check(roleCzar.getId()); }
        RadioButton rolePlayer = new RadioButton(this);
        rolePlayer.setText("Player");
        rolePlayer.setTag(2);
        rolesButtonGroup.addView(rolePlayer);
        if (currentPref == 2) { rolesButtonGroup.check(rolePlayer.getId()); }

        // Setup alert dialog
        AlertDialog.Builder prefRoleDialogBuilder = new AlertDialog.Builder(this);
        prefRoleDialogBuilder.setTitle("Choose preferred role");
        prefRoleDialogBuilder.setPositiveButton("Set preference", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (int i = 0; i < rolesButtonGroup.getChildCount(); i++) {
                    if (((RadioButton)rolesButtonGroup.getChildAt(i)).isChecked()) {
                        mSharedPrefs.edit().putInt("PREF_ROLE", (int) rolesButtonGroup.getChildAt(i).getTag()).apply();
                    }
                }
                dialog.dismiss();
            }
        });
        AlertDialog prefRoleDialog = prefRoleDialogBuilder.create();
        prefRoleDialog.setView(layoutInflated);
        prefRoleDialog.show();
    }

    public void tutorialButton(View view) {
        // Setup tutModeView
        View layoutInflated = getLayoutInflater().inflate(R.layout.alert_dialog_selection, null);
        ((ViewGroup) layoutInflated).removeView(layoutInflated.findViewById(R.id.tryPlayButton));
        LinearLayout tutModeLayout = layoutInflated.findViewById(R.id.layout_decks);

        int currentTutMode = mSharedPrefs.getInt("PREF_ROLE", 0);

        final RadioGroup tutModeGroup = new RadioGroup(this);
        tutModeLayout.addView(tutModeGroup);

        RadioButton tutNone = new RadioButton(this);
        tutNone.setText("No tutorial");
        tutNone.setTag(0);
        tutModeGroup.addView(tutNone);
        if (currentTutMode == 0) { tutModeGroup.check(tutNone.getId()); }
        RadioButton tutOnce = new RadioButton(this);
        tutOnce.setText("Tutorial once only");
        tutOnce.setTag(1);
        tutModeGroup.addView(tutOnce);
        if (currentTutMode == 1) { tutModeGroup.check(tutOnce.getId()); }
        RadioButton tutAlways = new RadioButton(this);
        tutAlways.setText("Always shown");
        tutAlways.setTag(2);
        tutModeGroup.addView(tutAlways);
        if (currentTutMode == 2) { tutModeGroup.check(tutAlways.getId()); }

        // Setup alert dialog
        AlertDialog.Builder prefRoleDialogBuilder = new AlertDialog.Builder(this);
        prefRoleDialogBuilder.setTitle("Choose preferred role");
        prefRoleDialogBuilder.setPositiveButton("Set preference", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (int i = 0; i < tutModeGroup.getChildCount(); i++) {
                    if (((RadioButton)tutModeGroup.getChildAt(i)).isChecked()) {
                        int tutModeType = (int) tutModeGroup.getChildAt(i).getTag();
                        SharedPreferences.Editor spEditor = mSharedPrefs.edit();

                        //Czar-specific
                        spEditor.putInt("CHOOSE_AVAILABLE_DECKS", tutModeType);
                        spEditor.putInt("WAIT_FOR_PLAYERS", tutModeType);
                        spEditor.putInt("CHOOSE_BEST_CARD_CZAR", tutModeType);
                        //Player-specific
                        spEditor.putInt("CHOOSE_BEST_CARD_PLAYER", tutModeType);
                        spEditor.putInt("WAIT_WHILE_CZAR_DECIDES", tutModeType);

                        //Edit decks
                        spEditor.putInt("PICK_DECK_EDIT", tutModeType);
                        //New Deck
                        spEditor.putInt("DECK_COLOR_INFO", tutModeType);

                        //Settings
                        spEditor.putInt("PREFERRED_ROLE", tutModeType);
                        spEditor.putInt("TUTORIAL_JOKE", tutModeType);
                        spEditor.putInt("CREDITS_INFO", tutModeType);

                        spEditor.apply();
                    }
                }
                dialog.dismiss();
            }
        });
        AlertDialog tutModeDialog = prefRoleDialogBuilder.create();
        tutModeDialog.setView(layoutInflated);
        tutModeDialog.show();
    }

    public void creditsButton(View view) {
        Intent showCredits = new Intent(this, credits.class);
        startActivity(showCredits);
    }

    private void showToasts(final String[] messages, final String[] spKeys) {
        final Thread showToastThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < messages.length; i++) {
                    final String message = messages[i];
                    int toastMode = mSharedPrefs.getInt(spKeys[i], 1);
                    if (toastMode != 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(settings.this, message, Toast.LENGTH_SHORT).show();
                            }
                        });
                        if (toastMode == 1) {
                            mSharedPrefs.edit().putInt(spKeys[i], 0).apply();
                        }
                    }
                    try {
                        Thread.sleep(2100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        showToastThread.start();
    }

}