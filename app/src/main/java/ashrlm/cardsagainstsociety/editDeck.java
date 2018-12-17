package ashrlm.cardsagainstsociety;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class editDeck extends Activity {

    private String deckPath;
    private String deckType;
    private EditText new_deck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_deck);
        deckPath = getIntent().getStringExtra("path");
        deckType = getIntent().getStringExtra("card_type");
        new_deck = findViewById(R.id.deck_title);
        //Update height of new_deck
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        new_deck.setLayoutParams (new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, (int) (height * .92)));
        //Read data to add to new_deck

        FileInputStream fis;
        try {
            fis = new FileInputStream(getFilesDir().getPath() + '/' + deckType + '/' + deckPath);
            StringBuffer fileContent = new StringBuffer();

            byte[] buffer = new byte[1024];
            int n;

            while ((n = fis.read(buffer)) != -1)
            {
                fileContent.append(new String(buffer, 0, n));
            }
            new_deck.setText(fileContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateDeck(View v) {

        if (new_deck.getText() == null || new_deck.getText().toString().isEmpty()) {
            return; //Do nothing - Empty
        }

        String fileContents = new_deck.getText().toString();
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(deckPath, Context.MODE_PRIVATE);
            outputStream.write(fileContents.getBytes());
            outputStream.close();
        } catch (Exception e) {

            e.printStackTrace();
        }
        finish();
    }

}
