package ml.ashrlm.cardsagainstsociety;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class mainGame extends AppCompatActivity {

    private Room mRoom;
    private String czarId;
    private String mRoomId;
    private static long role;
    private String mPlayerId;
    private RoomConfig mRoomConfig;
    private boolean isCzar = false;
    private String mMyParticipantId;
    private boolean mPlaying = false;
    private int numCardsRemaining = 10;
    private List<Participant> mParticipants;
    private final String TAG = "ashrlm.cas";
    private HashMap<String, String> playedCards; //Used by the czar
    private GoogleSignInAccount mSignedInAccount;
    private static final int RC_WAITING_ROOM = 9007;
    private HashMap<String, ArrayList<String>> wonCards; //Used for scoresheet
    private boolean returnedFromWaitingUi = false; //Used until custom waiting room UI
    private HashMap<String, String> idNames = new HashMap();
    private ArrayList<String> whiteCards = new ArrayList<>();
    private ArrayList<String> blackCards = new ArrayList<>();
    private RealTimeMultiplayerClient mRealTimeMultiplayerClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        signInSilently();
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(  R.layout.activity_game_lobby);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        myToolbar.setTitle("Cards against Society - Loading lobby");
        setSupportActionBar(myToolbar);
        Intent intentFromHomepage = getIntent();
        role = intentFromHomepage.getIntExtra("role", 0x0);
        whiteCards = intentFromHomepage.getStringArrayListExtra("whiteCards");
        blackCards = intentFromHomepage.getStringArrayListExtra("blackCards");
        if (role == 0x1) { isCzar = true; }
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        if (mPlaying) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setMessage("If you exit this game, you will not be able to rejoin.");
            if (isCzar) {
                builder1.setMessage("If you exit this game, it will be cancelled.");
            }
            builder1.setCancelable(true);

            builder1.setPositiveButton(
                    "Exit Game",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            leaveRoomPrep(mRoom);
                        }
                    });

            builder1.setNeutralButton(
                    "Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();
        } else {
            leaveRoomPrep(mRoom);
        }
    }

    private void onConnected(GoogleSignInAccount signedInAccount) {
        if (mSignedInAccount!= signedInAccount) {

            mSignedInAccount = signedInAccount;

            mRealTimeMultiplayerClient = Games.getRealTimeMultiplayerClient(this, signedInAccount);
            PlayersClient playersClient = Games.getPlayersClient(this, signedInAccount);
            playersClient.getCurrentPlayer()
                    .addOnSuccessListener(new OnSuccessListener<Player>() {
                        @Override
                        public void onSuccess(Player player) {
                            mPlayerId = player.getPlayerId();
                        }
                    });
        }
        startQuickGame();
    }

    void startQuickGame() {
        Log.d(TAG, "Started quick-game");
        int MIN_PLAYERS = 2;
        int MAX_PLAYERS = 7;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_PLAYERS,
                MAX_PLAYERS, role);
        Log.d(TAG, String.valueOf(autoMatchCriteria));

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .setAutoMatchCriteria(autoMatchCriteria)
                .build();
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

                if (message.startsWith("name")) {
                    Log.d(TAG, "New Name: " + message.substring(4));
                    idNames.put(senderId, message.substring(4));
                } else if (message.equals("leave")) {
                    Log.d(TAG, "leaveMsgReceived");
                    //Czar left - Game over
                    mRealTimeMultiplayerClient.leave(mRoomConfig, "czar left");
                    finish();
                    Intent showScores = new Intent(getApplicationContext(), scoreSheet.class);
                    showScores.putExtra("scores", wonCards);
                    startActivity(showScores);
                } else if (message.startsWith("newblack")) {
                    Log.d(TAG, "newblack: " + message.substring(9));
                    //Message in format of "newblack [NEW BLACK CARD]"
                    updateBlack(message.substring(9));
                } else if (message.startsWith("win")) {
                    Log.d(TAG, "win: " + message.substring(4));
                    //Message in format of win [text on card] winnerId
                    updateWins(message.substring(4), senderId); //TODO: add winnerId to message and receive here

                } else if (message.startsWith("cw")) {
                    Log.d(TAG, "whiteReceived: " + message);
                    //White card sent to czar by player
                    updateCzarWhite(message.substring(2), senderId);

                } else if (message.charAt(0) == 'w') {
                    Log.d(TAG, "whiteFromCzar: " + message.substring(1));
                    czarId = senderId;
                    updatePlayerWhite(message.substring(1));
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
            if (statusCode != GamesCallbackStatusCodes.OK) {
                Log.d(TAG, "leaving");
                finish();
                return;
            }
            mRoomId = room.getRoomId();
            mRoom = room;
            showWaitingRoom(room);
            Log.d(TAG, "ROOM: " + room);
        }

        @Override
        public void onJoinedRoom(int statusCode, @Nullable Room room) {
            Log.d(TAG, "onJoinedRoom(" + statusCode + ", " + room + ")");
            mRoom = room;
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
        mRoom = room;
    }

    void showWaitingRoom(Room room) {
        mRealTimeMultiplayerClient.getWaitingRoomIntent(room, 0)
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        // show waiting room UI
                        Log.d(TAG, "Waiting room UI shown");
                        returnedFromWaitingUi = true;
                        startActivityForResult(intent, RC_WAITING_ROOM);
                        /*NOTE: In time, this will be updated to use a custom UI that better fits
                                the rest of the app. Do not worry about the weird behaviour of back,
                                this will be fixed when a custom waiting room UI is implemented.*/
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
            Log.d(TAG, "onDisconnectedFromRoom(" + room + ")");
            mRoomId = null;
            mRoomConfig = null;
            finish();
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
                            onConnected(task.getResult());
                            Log.d(TAG, "Silent sign-in succeeded");
                        } else {
                            Log.e(TAG, "Silent sign-in failed");
                        }
                    }
                });
    }

    private void leaveRoomPrep(Room room) {
        if (mPlaying) {
            //Tell others players what to do after we leave
            if (isCzar) {
                //Czar left - Tell others to leave
                for (Participant p : room.getParticipants()) {
                    try {
                        Log.d(TAG, "leaveMsg sent");

                        mRealTimeMultiplayerClient.sendReliableMessage(
                                "leave".getBytes("utf-8"),
                                mRoomId,
                                p.getParticipantId(),
                                null
                        );
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        Log.e(TAG, String.valueOf(e));
                    }
                }
            } else {
                //Not as important to the game - May be able to leave freely
                if (room.getParticipants().size() <= 3) {
                    //Once this player leaves, game will be too boring - end game
                    for (Participant p : room.getParticipants()) {
                        try {
                            Log.d(TAG, "leaveMsg sent");
                            mRealTimeMultiplayerClient.sendReliableMessage(
                                    "leave".getBytes("utf-8"),
                                    mRoomId,
                                    p.getParticipantId(),
                                    null
                            );
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            Log.e(TAG, String.valueOf(e));
                        }
                    }
                }
            }
        }
        Log.e(TAG, "roomLeft");
        mRealTimeMultiplayerClient.leave(mRoomConfig, mRoomId);
    }

    protected void onResume() {
        super.onResume();
        if (returnedFromWaitingUi && !mPlaying) {
            Log.d(TAG, "returnedFromWaitingUI");
            finish();
        }
    }

    //------------------------------------Main Game logic-------------------------------------------

    /* TODO (Bugs to fix) :
        - Repeating cards for users (Sometimes already played)
        - In scores on side, when a card has to be wrapped, it loses indentation (Find better solution than adding spaces at start)
        - Also in scores on side, the name is always null, so they are all under one name
        - updateWins uses senderId, which means the czar is shown to have won all - Add tag to card with sender id
     */

    /* TODO: (Necessary Features)
        - Require a czar to be present
            - Find some way of requiring a role in automatch
                               OR
            - If none is present at game start time, choose one and let them select decks (Require initial role submission to all? not quite sure how this would work)

        - Update selection UI
            - PLAYER
                - Highlight cards
                - Add "PLAY CARDS" button on side
                    - Disabled initially
                    - Only enable when a card is selected
                    - When pressed, disable on click for "PLAY CARDS" button (Global flag)
                    - Re-enable on "win" msg received
            - CZAR
                - Highlight cards
                 - Add "SELECT WINNER" button on side
                    - Disabled initially
                    - When pressed, disable on click for "SELECT WINNER" button (Global flag")
                    - Only re-enabled when all players have submitted cards
                    - Clear cards on click

        - Add support for choose multiple
            - PLAYER
                - Highlight multiple
            - CZAR
                - show in stack that expands horizontally and replaces all other cards
                - Have "SELECT WINNER" button in this stack
     */

    /* TODO: (Optional features)
        - Add voice chat
            - Icons for mute/silence in status bar
     */

    private void runGame() {
        Log.d(TAG, String.valueOf(isCzar));
        mPlaying = true;

        Log.d(TAG, "Game started!");
        setContentView(R.layout.main_game);

        //Share name
        sendMsg("name" + GoogleSignIn.getLastSignedInAccount(this).getDisplayName());

        if (isCzar) {
            //Split decks
            wonCards = new HashMap<>();
            playedCards = new HashMap<>();
            ArrayList<ArrayList<String>> whiteCardsSplit = splitDeck(whiteCards);
            //Send decks to all participants

            for (int i = 0; i < mParticipants.size(); i++) {
                for (String card : whiteCardsSplit.get(i)) {
                    sendTargetedMsg("w" + card, mParticipants.get(i));
                }
            }

            for (int i = 0; i < mParticipants.size(); i++) {

                //Add participant name/id to deck
                idNames.put(mParticipants.get(i).getParticipantId(), mParticipants.get(i).getDisplayName());
                Log.d(TAG, "partNIdAdded");

                if (mParticipants.get(i).getParticipantId().equals(mMyParticipantId)) { continue; }

                for (String card : whiteCardsSplit.get(i)) {
                    card = "w" + card;
                    try {
                        Log.d(TAG, "sentWhiteCard");
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
            //Send initial black card
            String newBlack = blackCards.get(new Random().nextInt(blackCards.size()));
            blackCards.remove(newBlack);
            updateBlack(newBlack);
            sendMsg("newblack " + newBlack);
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
            buttonsLayout.getChildAt(i).setBackgroundResource(R.drawable.white_card);
        }
        whiteButton.setBackgroundResource(R.drawable.selected_white); //Set card to selected
        whiteButton.setTag(false);
        String whiteCardText = whiteButton.getText().toString();
        try {
            mRealTimeMultiplayerClient.sendReliableMessage(
                    ("cw" + whiteCardText).getBytes("utf-8"),
                    mRoomId,
                    czarId,
                    null

            );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void chooseCard(View view) {
        Button chosenCard = (Button) view;

        String newBlack = blackCards.get(new Random().nextInt(blackCards.size()));
        updateBlack(newBlack);
        sendMsg("newblack " + newBlack);

        String winningCardText = chosenCard.getText().toString();
        updateWins(winningCardText, null); //TODO: Find a way to encode the id of the participant in the message sent by the czar
        sendMsg("win " + chosenCard.getText().toString());

        //Clear all cards from bottom of screen
        ((ViewGroup) view.getParent()).removeAllViews();
    }

    //Messaging

    private void sendMsg(String message) {
        for (Participant p : mParticipants) {
            try {
                mRealTimeMultiplayerClient.sendReliableMessage(
                        message.getBytes("utf-8"),
                        mRoomId,
                        p.getParticipantId(),
                        null
                );
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                Log.e(TAG, String.valueOf(e));
            }
        }
    }

    private void sendTargetedMsg(String message, Participant p) {
        try {
            mRealTimeMultiplayerClient.sendReliableMessage(
                    message.getBytes("utf-8"),
                    mRoomId,
                    p.getParticipantId(),
                    null
            );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    //UI updates

    private void updateBlack(String newblack) {
        TextView blackPrompt = findViewById(R.id.blackCardMain);
        blackPrompt.setText(newblack);
    }

    private void updateWins(String wonCard, String winnerId) {
        Log.d(TAG, "wonCard " + wonCard);
        //Update wins hashmap
        if (wonCards == null) { wonCards = new HashMap<>(); }
        if (wonCards.containsKey(idNames.get(winnerId))) {
            //Update scores of existing participant
            wonCards.get(idNames.get(winnerId)).add(wonCard);
        } else {
            //Add participant to scores
            ArrayList<String> newWinTmp = new ArrayList<>();
            newWinTmp.add(wonCard);
            Log.d(TAG, "idNames.get(winnerId) " + idNames.get(winnerId));
            wonCards.put(idNames.get(winnerId), newWinTmp);
        }

        //Update list of wins
        TextView winsText = findViewById(R.id.winsScrolledText);
        String newWinsMsg = "SCORES\n\n";
        for (Map.Entry<String, ArrayList<String>> win : wonCards.entrySet()) {
            newWinsMsg += (idNames.get(win.getKey()) + "\n");
            for (String cardWon : win.getValue()) {
                Log.d(TAG, "cardWon " + cardWon);
                newWinsMsg += ("    " + cardWon + "\n");
            }
        }

        winsText.setText(newWinsMsg);

        //Update tags on own white cards
        if (!isCzar) {
            LinearLayout whiteCardsLayout = findViewById(R.id.whitesScrolledLayout);
            for (int i = 0; i < whiteCardsLayout.getChildCount(); i++) {
                if (!(Boolean) whiteCardsLayout.getChildAt(i).getTag()) {
                    whiteCardsLayout.removeView(whiteCardsLayout.getChildAt(i));
                    break;
                }
            }

            //Check if game is over
            if (numCardsRemaining == 0) {
                Log.d(TAG, "Out of Cards");
                Intent showScores = new Intent(getApplicationContext(), scoreSheet.class);
                showScores.putExtra("scores", wonCards);
                finish();
                startActivity(showScores);
            }
            numCardsRemaining--;
        }
    }

    private void updateCzarWhite(String message, String senderId) {
        playedCards.put(senderId, message);
        LinearLayout whitesPlayed = findViewById(R.id.whitesScrolledLayout);
        found:
        {
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
                            chooseCard(v);
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
    }

    private void updatePlayerWhite(String message) {
        //Received white card from czar - add to list of available cards
        if (whiteCards == null) { whiteCards = new ArrayList<>(); }
        LinearLayout whiteCardsLayout = findViewById(R.id.whitesScrolledLayout);
        whiteCards.add(message);

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
    }
}