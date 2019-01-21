package ml.ashrlm.cardsagainstsociety;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class scoreSheet extends AppCompatActivity {

    private static final String TAG = "ashrlm.cas";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_score_sheet);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        myToolbar.setTitle("Cards against Society - Scores");
        setSupportActionBar(myToolbar);
        Intent receivedIntent = getIntent();
        HashMap<String, ArrayList<String>> scores = customSort((HashMap<String, ArrayList<String>>) receivedIntent.getSerializableExtra("scores"));
        //Update list of scores on screen TODO Style these
        LinearLayout scoreList = findViewById(R.id.scores_layout);
        for (Map.Entry<String, ArrayList<String>> score : scores.entrySet()) {
            final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
            TextView scoreLabel = new TextView(this);
            scoreList.addView(scoreLabel);
            scoreLabel.setText(score.getKey() + ": " + score.getValue().size());
            scoreLabel.setTextSize((int) 11.25 * scale + .5f);
            //Add won cards
            for (String wonCard : score.getValue()) {
                TextView newCard = new TextView(this);
                char[] paddedScore = new char[score.getValue().size() * 2];
                Arrays.fill(paddedScore, ' ');
                newCard.setText(new String(paddedScore) + wonCard);
                newCard.setTextSize(6 * scale + .5f);
                scoreList.addView(newCard);
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

    private HashMap<String, ArrayList<String>> customSort (HashMap<String, ArrayList<String>> unsorted) {
        //Sort by size of ArrayList<String>
        LinkedHashMap<String, ArrayList<String>> sorted = new LinkedHashMap<>();

        List<Map.Entry<String, ArrayList<String>>> list = new LinkedList<>(unsorted.entrySet());

        //sorting the list with a comparator
        Collections.sort(list, new Comparator<Map.Entry<String, ArrayList<String>>>() {
            public int compare(Map.Entry<String, ArrayList<String>> o1, Map.Entry<String, ArrayList<String>> o2) {
                return (String.valueOf(o1.getValue().size())).compareTo(String.valueOf(o2.getValue().size()));
            }
        });

        //convert sortedMap back to Map
        for (Map.Entry<String, ArrayList<String>> entry : Lists.reverse(list)) {
            sorted.put(entry.getKey(), entry.getValue());

        }
        Log.d(TAG, String.valueOf(sorted));
        return sorted;
    }

}