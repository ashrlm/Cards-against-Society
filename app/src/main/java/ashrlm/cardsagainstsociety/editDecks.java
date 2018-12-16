package ashrlm.cardsagainstsociety;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import java.io.File;
import java.io.IOException;

public class editDecks extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_decks);
        try {
            int i = 0;
            for (final String deck : getAssets().list("")) {
                Uri file = Uri.fromFile(new File(deck));
                String fileExt = MimeTypeMap.getFileExtensionFromUrl(file.toString());
                if (fileExt.equals("txt")) {
                    //Generate layout
                    LinearLayout layout = findViewById(R.id.layout_btns);

                    //set the properties for button
                    Button editDeckBtn = new Button(this);
                    editDeckBtn.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                    editDeckBtn.setText(String.format("Edit Deck: %s", deck));
                    editDeckBtn.setTag(deck);
                    editDeckBtn.setId(i);
                    editDeckBtn.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            Intent gotoEditDeck = new Intent(getApplicationContext(), editDeck.class);
                            Bundle data = new Bundle();
                            data.putString("path", deck);
                            gotoEditDeck.putExtras(data);
                            startActivity(gotoEditDeck);

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
                    layout.addView(editDeckBtn);
                    i++;
                }
            }
            //Generate final button (new deck)
            LinearLayout layout = findViewById(R.id.layout_btns);

            //set the properties for button
            Button editDeckBtn = new Button(this);
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
                    (int) (ll.topMargin+(5*scale + .5f)),
                    ll.rightMargin,
                    (int) (ll.bottomMargin+(5*scale + .5f)));
            editDeckBtn.setLayoutParams(ll);

            //add button to the layout
            layout.addView(editDeckBtn);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
