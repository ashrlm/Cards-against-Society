package ashrlm.cardsagainstsociety;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class credits extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);
        TextView t2 = findViewById(R.id.credits_text);
        t2.setMovementMethod(LinkMovementMethod.getInstance());
    }
}