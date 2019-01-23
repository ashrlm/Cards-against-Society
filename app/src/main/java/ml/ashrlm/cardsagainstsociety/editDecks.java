package ml.ashrlm.cardsagainstsociety;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.util.ArrayList;

public class editDecks extends AppCompatActivity {

    private ArrayList<String> cards = new ArrayList<String>();
    private LinearLayout decksLayout;
    private final String TAG = "ashrlm.cas";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_edit_decks);
        decksLayout = findViewById(R.id.layout_btns);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        myToolbar.setTitle("Cards against Society - Edit decks");
        setSupportActionBar(myToolbar);
    }

    private void getCards () {
        for (String card_dir : getFilesDir().list()) {
            File dir = new File(getFilesDir().getPath() + "/" + card_dir);
            for (String card : dir.list()) {
                cards.add(card);
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

    @Override
    protected void onResume() {
        super.onResume();
        //Clear all cards
        decksLayout.removeAllViews();

        cards = new ArrayList<>();
        getCards();
        int i = 0;

        for (final String deck : cards) {
            Uri file = Uri.fromFile(new File(deck));
            String fileExt = MimeTypeMap.getFileExtensionFromUrl(file.toString());
            if (fileExt.equals("txt")) {
                //Check black or white
                File tmp_file = new File(getFilesDir().getPath() + "/white/" + deck);
                String card_type = "white/";
                if (!tmp_file.exists()) {
                    card_type = "black/";
                }

                //set the properties for button
                final Button editDeckBtn = new Button(this);
                editDeckBtn.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                editDeckBtn.setText(String.format("Edit Deck: %s", deck));
                editDeckBtn.setTag(deck);
                editDeckBtn.setId(i);
                final String finalCard_type = card_type;
                editDeckBtn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent gotoEditDeck = new Intent(getApplicationContext(), editDeck.class);
                        Bundle data = new Bundle();
                        data.putString("card_type", finalCard_type);
                        data.putString("path", deck);
                        gotoEditDeck.putExtras(data);
                        startActivity(gotoEditDeck);

                    }
                });
                editDeckBtn.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        ViewGroup layout = (ViewGroup) editDeckBtn.getParent();
                        if (null != layout) {
                            File unwanted_deck = new File(getFilesDir().getPath() + "/black/" + deck);
                            if (!unwanted_deck.exists()) { unwanted_deck = new File(getFilesDir().getPath() + "/white/" + deck); }
                            unwanted_deck.delete();
                            layout.removeView(editDeckBtn);

                        }
                        return true;
                    }
                });

                //Styling of button
                editDeckBtn.setBackgroundResource(R.drawable.button);
                final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
                editDeckBtn.setWidth((int) (351 * scale + .5f));
                editDeckBtn.setHeight((int) (100 * scale + .5f));
                LayoutParams ll = (LayoutParams)editDeckBtn.getLayoutParams();
                ll.gravity = Gravity.CENTER;
                ll.setMargins(ll.leftMargin,
                        (int) (ll.topMargin+(5*scale + .5f)),
                        ll.rightMargin,
                        (int) (ll.bottomMargin+(5*scale + .5f)));
                editDeckBtn.setLayoutParams(ll);

                //add button to the layout
                decksLayout.addView(editDeckBtn);
                i++;
            }
        }
        //Generate final button (new deck)

        //set the properties for button
        final Button editDeckBtn = new Button(this);
        editDeckBtn.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        editDeckBtn.setText("New Deck");
        editDeckBtn.setId(i);
        editDeckBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent gotoNewDeck = new Intent(getApplicationContext(), newDeck.class);
                startActivity(gotoNewDeck);
            }
        });

        //Styling of button
        editDeckBtn.setBackgroundResource(R.drawable.button);
        final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
        editDeckBtn.setWidth((int) (351 * scale + .5f));
        editDeckBtn.setHeight((int) (100 * scale + .5f));
        LayoutParams ll = (LayoutParams)editDeckBtn.getLayoutParams();
        ll.gravity = Gravity.CENTER;
        ll.setMargins(ll.leftMargin,
                (int) (ll.topMargin+(10*scale + .5f)),
                ll.rightMargin,
                ll.bottomMargin);
        editDeckBtn.setLayoutParams(ll);

        //add button to the layout
        decksLayout.addView(editDeckBtn);
    }
}