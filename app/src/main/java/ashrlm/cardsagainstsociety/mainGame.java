package ashrlm.cardsagainstsociety;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.OnRealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateCallback;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class mainGame extends Activity {

    private String czarId;
    private String mRoomId;
    private static long role;
    private String mPlayerId;
    private int MIN_PLAYERS = 1;
    private int MAX_PLAYERS = 1;
    private RoomConfig mRoomConfig;
    private boolean isCzar = false;
    private String mMyParticipantId;
    private boolean mPlaying = false;
    private int numCardsRemaining = 10;
    private List<Participant> mParticipants;
    private final String TAG = "ashrlm.cas";
    private Dictionary<String, String> playedCards; //Used by the czar
    private HashMap<String, ArrayList<String>> wonCards; //Used for scoresheet
    private static final int RC_WAITING_ROOM = 9007;
    private HashMap<String, String> idNames = new HashMap();
    private ArrayList<String> whiteCards = new ArrayList<>();
    private ArrayList<String> blackCards = new ArrayList<>();
    private RealTimeMultiplayerClient mRealTimeMultiplayerClient;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        signInSilently();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_lobby);
        Intent intentFromHomepage = getIntent();
        role = intentFromHomepage.getIntExtra("role", 0x0);
        whiteCards = intentFromHomepage.getStringArrayListExtra("whiteCards");
        blackCards = intentFromHomepage.getStringArrayListExtra("blackCards");
        if (role == 0x1) { isCzar = true; }
        startQuickGame();
    }
    void startQuickGame() {
        Log.d(TAG, "Started quick-game");
        // quick-start a game with 1 randomly selected opponent
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_PLAYERS,
                MAX_PLAYERS, role);

        mRealTimeMultiplayerClient = Games.getRealTimeMultiplayerClient(
                getApplicationContext(),
                GoogleSignIn.getLastSignedInAccount(getApplicationContext())
        );

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .setAutoMatchCriteria(autoMatchCriteria)
                .build();
        Log.d(TAG, String.valueOf(mRoomConfig));
        mRealTimeMultiplayerClient.create(mRoomConfig);
    }
    //Message handler
    OnRealTimeMessageReceivedListener mOnRealTimeMessageReceivedListener = new OnRealTimeMessageReceivedListener() {
        @Override
        public void onRealTimeMessageReceived(@NonNull RealTimeMessage realTimeMessage) {
            byte[] buf = realTimeMessage.getMessageData();
            final String senderId = realTimeMessage.getSenderParticipantId();
            try {
                String message = new String(buf, "utf-8");

                if (message.startsWith("newblack")) {
                    //Message in format of "newblack [NEW BLACK CARD]"
                    //Update UI
                    TextView blackPrompt = findViewById(R.id.blackCardMain);
                    blackPrompt.setText(message.substring(9));
                } else if (message.startsWith("win")) {
                    //Message in format of win [text on card]
                    String wonCard = message.substring(4);
                    //Update wins hashmap
                    if (wonCards.containsKey(senderId)) {
                        //Update scores of existing participant
                        wonCards.get(senderId).add(wonCard);
                    } else {
                        //Add participant to scores
                        ArrayList<String> newWinTmp = new ArrayList<>();
                        newWinTmp.add(wonCard);
                        wonCards.put(idNames.get(senderId), newWinTmp);
                    }

                    //Update tags on own white cards
                    LinearLayout whiteCardsLayout = findViewById(R.id.whitesScrolledLayout);
                    for (int i = 0; i < whiteCardsLayout.getChildCount(); i++) {
                        if (whiteCardsLayout.getChildAt(i).getVisibility() == View.INVISIBLE) {
                            whiteCardsLayout.getChildAt(i).setTag(false); //Make card unplayable - Already done
                            break;
                        }
                    }

                    //Update list of wins
                    TextView winsText = findViewById(R.id.winsScrolledText);
                    String newWinsMsg = "SCORES\n\n";
                    for (Map.Entry<String, ArrayList<String>> win : wonCards.entrySet()) {
                        newWinsMsg += (idNames.get(win.getKey()) + "\n");
                        for (String cardWon : win.getValue()) {
                            newWinsMsg += ("    " + cardWon + "\n");
                        }
                    }
                    winsText.setText(newWinsMsg);

                    //Check if game is over
                    if (numCardsRemaining == 0) {
                        Intent showScores = new Intent(getApplicationContext(), scoreSheet.class);
                        showScores.putExtra("scores", wonCards);
                        finish();
                        startActivity(showScores);
                    }
                    numCardsRemaining--;
                } else if (isCzar) {
                    //White card sent to czar by player
                    playedCards.put(senderId, message);
                    LinearLayout whitesPlayed = findViewById(R.id.whitesScrolledLayout);
                    found: {
                        for (int i = 0; i < whitesPlayed.getChildCount(); i++) {
                            if (whitesPlayed.getChildAt(i).getTag().equals(senderId)) {
                                Button btnToOverwrite = (Button) whitesPlayed.getChildAt(i);
                                if (btnToOverwrite.getTag().equals(senderId)) {
                                    //Overwrite existing button
                                    btnToOverwrite.setText(message);
                                    break found;
                                }
                            }
                        }
                       //This player hasn't played yet - Add their card
                        LinearLayout whiteCardsLayout = findViewById(R.id.whitesScrolledLayout);
                        //Add deck buttons
                        Button playedWhiteCardBtn = new Button(getApplicationContext());
                        playedWhiteCardBtn.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT));
                        playedWhiteCardBtn.setText(message);
                        playedWhiteCardBtn.setTag(senderId);
                        playedWhiteCardBtn.setOnClickListener(
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        chooseCard(v, senderId);
                                    }
                                }
                        );
                        //Style button
                        playedWhiteCardBtn.setBackgroundResource(R.drawable.white_card);
                        final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
                        playedWhiteCardBtn.setWidth((int) (100 * scale + .5f));
                        playedWhiteCardBtn.setHeight((int) (100 * scale + .5f));
                        ConstraintLayout.LayoutParams ll = (ConstraintLayout.LayoutParams) playedWhiteCardBtn.getLayoutParams();
                        ll.setMargins((int) (ll.leftMargin + (3 * scale + .5f)),
                                ll.topMargin,
                                (int) (ll.rightMargin + (3 * scale + .5f)),
                                (int) (ll.bottomMargin + (5 * scale + .5f)));
                        playedWhiteCardBtn.setLayoutParams(ll);
                    playedWhiteCardBtn.setSingleLine(false);
                    playedWhiteCardBtn.setTextColor(Color.DKGRAY);
                    playedWhiteCardBtn.setTextSize(5 * scale + .5f);
                    whiteCardsLayout.addView(playedWhiteCardBtn);
                }

                } else if (message.charAt(0) == 'w') {
                    czarId = senderId;
                    //Received white card from czar - add to list of available cards
                    whiteCards.remove(message);
                    whiteCards.add(message.subSequence(1, message.length()-1).toString());
                    LinearLayout whiteCardsLayout = findViewById(R.id.whitesScrolledLayout);
                    //Add deck buttons
                    Button whiteCardBtn = new Button(getApplicationContext());
                    whiteCardBtn.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT));
                    whiteCardBtn.setText(message);
                    whiteCardBtn.setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    onWhiteCardClicked(v);
                                }
                            }
                    );
                    //Style button
                    whiteCardBtn.setBackgroundResource(R.drawable.white_card);
                    final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
                    whiteCardBtn.setWidth((int) (100 * scale + .5f));
                    whiteCardBtn.setHeight((int) (100 * scale + .5f));
                    ConstraintLayout.LayoutParams ll = (ConstraintLayout.LayoutParams) whiteCardBtn.getLayoutParams();
                    ll.setMargins((int) (ll.leftMargin + (3 * scale + .5f)),
                            ll.topMargin,
                            (int) (ll.rightMargin + (3 * scale + .5f)),
                            (int) (ll.bottomMargin + (5 * scale + .5f)));
                    whiteCardBtn.setLayoutParams(ll);
                    whiteCardBtn.setSingleLine(false);
                    whiteCardBtn.setTextColor(Color.DKGRAY);
                    whiteCardBtn.setTextSize(5 * scale + .5f);
                    whiteCardBtn.setTag(true); //Determines playable
                    whiteCardsLayout.addView(whiteCardBtn);
                } else if (message.charAt(0) == 'b') {
                    TextView mainBlack = findViewById(R.id.blackCardMain);
                    mainBlack.setText(message.substring(1));
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };

    private RoomUpdateCallback mRoomUpdateCallback = new RoomUpdateCallback() {
        @Override
        public void onRoomCreated(int statusCode, @Nullable Room room) {
            Log.d(TAG, "onRoomCreated(" + statusCode + ", " + room + ")");
            //TODO: Fix room always being null
            if (statusCode != GamesCallbackStatusCodes.OK) {
                mRealTimeMultiplayerClient.leave(mRoomConfig, "leaving room");
                Log.d(TAG, "leaving");
                finish();
                return;
            }
            mRoomId = room.getRoomId();
            showWaitingRoom(room);
            Log.d(TAG, "ROOM: " + room);
        }

        @Override
        public void onJoinedRoom(int statusCode, @Nullable Room room) {
            Log.d(TAG, "onJoinedRoom(" + statusCode + ", " + room + ")");
            showWaitingRoom(room);
        }

        @Override
        public void onLeftRoom(int statusCode, @NonNull String s) {
            Log.d(TAG, "onLeftRoom(" + statusCode + ", " + s + ")");
            finish();
        }

        @Override
        public void onRoomConnected(int statusCode, @Nullable Room room) {
            Log.d(TAG, "onRoomConnected(" + statusCode + ", " + room + ")");
            runGame();
        }
    };

    void updateRoom(Room room) {
        if (room != null) {
            mParticipants = room.getParticipants();
        }
        if (mParticipants != null) {
            //TODO: Update room depending on status
        }
    }

    void showWaitingRoom(Room room) {
        mRealTimeMultiplayerClient.getWaitingRoomIntent(room, 0)
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        // show waiting room UI
                        Log.d(TAG, "Waiting room UI shown");
                        startActivityForResult(intent, RC_WAITING_ROOM);
                    }
                });
    }

    private RoomStatusUpdateCallback mRoomStatusUpdateCallback = new RoomStatusUpdateCallback() {
        // Called when we are connected to the room. We're not ready to play yet! (maybe not everybody
        // is connected yet).
        @Override
        public void onConnectedToRoom(Room room) {

            //get participants and my ID:
            mParticipants = room.getParticipants();
            mMyParticipantId = room.getParticipantId(mPlayerId);

            // save room ID if its not initialized in onRoomCreated() so we can leave cleanly before the game starts.
            if (mRoomId == null) {
                mRoomId = room.getRoomId();
            }
        }

        // Called when we get disconnected from the room. We return to the main screen.
        @Override
        public void onDisconnectedFromRoom(Room room) {
            mRoomId = null;
            mRoomConfig = null;
        }

        // We treat most of the room update callbacks in the same way: we update our list of
        // participants and update the display. In a real game we would also have to check if that
        // change requires some action like removing the corresponding player avatar from the screen,
        // etc.
        @Override
        public void onPeerDeclined(Room room, @NonNull List<String> arg1) {
            updateRoom(room);
        }

        @Override
        public void onPeerInvitedToRoom(Room room, @NonNull List<String> arg1) {
            updateRoom(room);
        }

        @Override
        public void onP2PDisconnected(@NonNull String participant) {
        }

        @Override
        public void onP2PConnected(@NonNull String participant) {
        }

        @Override
        public void onPeerJoined(Room room, @NonNull List<String> arg1) {
            updateRoom(room);
        }

        @Override
        public void onPeerLeft(Room room, @NonNull List<String> peersWhoLeft) {
            updateRoom(room);
        }

        @Override
        public void onRoomAutoMatching(Room room) {
            updateRoom(room);
        }

        @Override
        public void onRoomConnecting(Room room) {
            updateRoom(room);
        }

        @Override
        public void onPeersConnected(Room room, @NonNull List<String> peers) {
            updateRoom(room);
        }

        @Override
        public void onPeersDisconnected(Room room, @NonNull List<String> peers) {
            updateRoom(room);
        }
    };

    private void signInSilently() {
        Log.d(TAG, "Silent sign-in attempted");
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        signInClient.silentSignIn().addOnCompleteListener(this,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
                            // The signed in account is stored in the task's result.
                            GoogleSignInAccount signedInAccount = task.getResult();
                            Log.d(TAG, "Silent sign-in succeeded");
                        } else {
                            Log.e(TAG, "Silent sign-in failed");
                        }
                    }
                });
    }

    //------------------------------------Main Game logic-------------------------------------------

    private void runGame () {
        Log.d(TAG, "Game started!");
        setContentView(R.layout.main_game);

        if (isCzar) {
            //Split decks
            ArrayList<ArrayList<String>> whiteCardsSplit = splitDeck(whiteCards);
            //Send decks to all participants
            for (int i = 0; i < mParticipants.size(); i++) {

                //Add participant name/id to deck
                idNames.put(mParticipants.get(i).getParticipantId(), mParticipants.get(i).getDisplayName());

                if (mParticipants.get(i).getParticipantId() == mMyParticipantId) { continue; }

                for (String card : whiteCardsSplit.get(i)) {
                    card = "w" + card;
                    try {
                        mRealTimeMultiplayerClient.sendReliableMessage(
                                card.getBytes("utf-8"),
                                mRoomId,
                                mParticipants.get(i).getParticipantId(),
                                null
                        );
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        Log.e(TAG, e.toString());
                    }
                }
            }
        }
    }

    private ArrayList<ArrayList<String>> splitDeck (ArrayList<String> deck) {
        ArrayList<ArrayList<String>> cardsSplit = new ArrayList<>();
        for (int i = 0; i < mParticipants.size(); i++) {
            ArrayList<String> cardsTmp = new ArrayList<>();
            for (int j = 0; j < Math.min(10, Math.floor(deck.size() / mParticipants.size())); j++) {
                String newCard = deck.get(new Random().nextInt(deck.size()));
                cardsTmp.add(newCard);
                deck.remove(newCard);
            }
            cardsSplit.add(cardsTmp);
        }
        return cardsSplit;
    }

    private void onWhiteCardClicked(View view) {
        Button whiteButton = (Button) view;
        //Set all other cards to deselected
        LinearLayout buttonsLayout = findViewById(R.id.whitesScrolledLayout);
        for (int i = 0; i < buttonsLayout.getChildCount(); i++) {
            if ((Boolean) buttonsLayout.getChildAt(i).getTag()) {
                buttonsLayout.getChildAt(i).setBackgroundResource(R.drawable.white_card);
            }
        }
        whiteButton.setBackgroundResource(R.drawable.selected_white); //Set card to selected
        String whiteCardText = whiteButton.getText().toString();
        mRealTimeMultiplayerClient.sendReliableMessage(
                whiteCardText.getBytes(),
                mRoomId,
                czarId,
                null

        );
    }

    private void chooseCard(View view, String senderId) {
        Button chosenCard = (Button) view;
        String newBlack = blackCards.get(new Random().nextInt(blackCards.size()));

        //Tell everyone to show card selecting UI
        for (Participant p : mParticipants) {

            mRealTimeMultiplayerClient.sendReliableMessage(
                    ("newblack " + newBlack).getBytes(),
                    mRoomId,
                    p.getParticipantId(),
                    null
            );

            //Tell everyone who got a point
            mRealTimeMultiplayerClient.sendReliableMessage(
                    ("win " + senderId + " " + chosenCard.getText().toString()).getBytes(),
                    mRoomId,
                    chosenCard.getTag().toString(),
                    null
            );
        }
    }
}