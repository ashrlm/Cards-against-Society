package ashrlm.cardsagainstsociety;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

import java.io.File;
import java.io.FileOutputStream;

public class newDeck extends AppCompatActivity {

    private EditText deck_title;
    private EditText deck_content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_deck);
        setTitle("Cards against Society - New Deck");
        deck_title = findViewById(R.id.deck_title);
        deck_content = findViewById(R.id.deck_content);
    }

    public void createDeck(View view) {
        String deckPath;
        String deckContent;
        String deckFolder;

        RadioButton white_deck = findViewById(R.id.radioWhite);
        RadioButton black_deck = findViewById(R.id.radioBlack);

        String deckTitle = deck_title.getText().toString();
        String deckCards = deck_content.getText().toString();
        if (deckTitle != null || deckCards.isEmpty()) {
            if (!deckTitle.endsWith(".txt")) {
                deckTitle += ".txt";
            }
        } else {
            return; //No title - Do nothing
        }

        if (deckCards == null || deckCards.isEmpty()) {
            return; //No cards - Do nothing
        }

        deckContent = deck_content.getText().toString();

        if (white_deck.isChecked()) {
            deckPath = deckTitle;
            deckFolder = "/white";
            File white_dir = new File(getFilesDir(), "white");
            if (!white_dir.exists()) {
                white_dir.mkdirs();
            }
        } else if (black_deck.isChecked()) {
            deckPath = deckTitle;
            deckFolder = "/black";
            File black_dir = new File(getFilesDir(), "black");
            if (!black_dir.exists()) {
                black_dir.mkdirs();
            }
        } else {
            //Nothing selected - Do nothing
            return;
        }

        FileOutputStream outputStream;

        try {
            File new_deck = new File(getFilesDir().getPath() + deckFolder, deckPath);
            outputStream = new FileOutputStream(new_deck);
            outputStream.write(deckContent.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finish();
    }

}
