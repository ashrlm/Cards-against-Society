package ashrlm.cardsagainstsociety;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class homepage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
    }

    public void newGameButton(View view) {
        Intent gotoNewGame = new Intent(this, newGame.class);
        startActivity(gotoNewGame);
    }

    public void joinGameButton(View view) {
        Intent gotoJoinGame = new Intent(this, joinGame.class);
        startActivity(gotoJoinGame);
    }

    public void creditsButton(View view) {
        Intent gotoCredits = new Intent(this, credits.class);
        startActivity(gotoCredits);
    }

}