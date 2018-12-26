package ashrlm.cardsagainstsociety;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class scoreSheet extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_sheet);
        Intent receivedIntent = getIntent();
        HashMap<String, ArrayList<String>> scores = (HashMap<String, ArrayList<String>>) receivedIntent.getSerializableExtra("scores");
        //Update list of scores on screen - TODO;
        LinearLayout scoreList = findViewById(R.id.scores_layout);
        for (Map.Entry<String, ArrayList<String>> score : scores.entrySet()) {
            TextView scoreLabel = new TextView(this);
            scoreLabel.setText(score.getKey() + " " + score.getValue().toArray().length);
            scoreList.addView(scoreLabel);
        }
    }

}
