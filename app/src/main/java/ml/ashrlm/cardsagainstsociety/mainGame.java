package ml.ashrlm.cardsagainstsociety;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class mainGame extends AppCompatActivity {

    private Room mRoom;
    private String mName;
    private String czarId;
    private String mRoomId;
    private int numPerDeck;
    private static long role;
    private String mPlayerId;
    private Button targetCard;
    private int numPlayed = 0;
    private int numReceived = 0;
    private Button chooseCardBtn;
    private String mParticipantId;
    private RoomConfig mRoomConfig;
    private boolean isCzar = false;
    private boolean mPlaying = false;
    private AlertDialog deckSelection;
    private BigInteger mCzarSubmission;
    private List<Participant> mParticipants;
    private final String TAG = "ashrlm.cas";
    private GoogleSignInAccount mSignedInAccount;
    private boolean returnedFromWaitingUi = false; // Used until custom waiting room UI
    private static final int RC_WAITING_ROOM = 9007;
    private HashMap<String, ArrayList<String>> wonCards; // Used for scoresheet
    private HashMap<String, String> idNames = new HashMap();
    private ArrayList<String> whiteCards = new ArrayList<>();
    private ArrayList<String> blackCards = new ArrayList<>();
    private ArrayList<BigInteger> czarNums = new ArrayList<>();
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

    private void onConnected(final GoogleSignInAccount signedInAccount) {
        if (mSignedInAccount!= signedInAccount) {

            mSignedInAccount = signedInAccount;

            mRealTimeMultiplayerClient = Games.getRealTimeMultiplayerClient(this, signedInAccount);
            PlayersClient playersClient = Games.getPlayersClient(this, signedInAccount);
            playersClient.getCurrentPlayer()
                    .addOnSuccessListener(new OnSuccessListener<Player>() {
                        @Override
                        public void onSuccess(Player player) {
                            mName = player.getDisplayName();
                            mPlayerId = player.getPlayerId();
                            Log.d(TAG, "mPlayerId: " + mPlayerId);
                            Log.d(TAG, "name: " + mName);
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

    // Message handler
    OnRealTimeMessageReceivedListener mOnRealTimeMessageReceivedListener = new OnRealTimeMessageReceivedListener() {
        @Override
        public void onRealTimeMessageReceived(@NonNull RealTimeMessage realTimeMessage) {
            byte[] buf = realTimeMessage.getMessageData();
            final String senderId = realTimeMessage.getSenderParticipantId();
            try {
                String message = new String(buf, "utf-8");

                if (message.startsWith("czarnum")) {
                    czarNums.add(new BigInteger(message.substring(7)));
                    if (czarNums.size() == mParticipants.size()) {
                        setupGame();
                        if (mCzarSubmission.equals(Collections.max(czarNums))) {
                            isCzar = true;
                            Log.d(TAG, "Czar");
                            // Request cards
                            requestCards();
                        } else {
                            Log.d(TAG, "Not czar");
                            runGame();
                        }

                    }

                } else if (message.startsWith("name")) {
                    Log.d(TAG, "New Name: " + message.substring(4));
                    idNames.put(senderId, message.substring(4));
                    Log.d(TAG, "name hashmap: " + idNames);

                } else if (message.equals("leave")) {
                    Log.d(TAG, "leaveMsgReceived");
                    // Game over
                    mRealTimeMultiplayerClient.leave(mRoomConfig, "Game over");
                    finish();
                    Intent showScores = new Intent(getApplicationContext(), scoreSheet.class);
                    HashMap<String, ArrayList<String>> namedWonCards = replaceIds(wonCards);
                    showScores.putExtra("scores", namedWonCards);
                    startActivity(showScores);

                } else if (message.startsWith("newblack")) {
                    Log.d(TAG, "newblack: " + message.substring(9));
                    // Message in format of "newblack [NEW BLACK CARD]"
                    updateBlack(message.substring(9));

                } else if (message.startsWith("win")) {
                    Log.d(TAG, "win: " + message.substring(4));
                    // Message in format of win [text on card] winnerId [winnerId]
                    String winnerId = message.substring(message.lastIndexOf("winnerId") + 9);
                    Log.d(TAG, "winnnerId:" + winnerId);
                    updateWins(message.substring(4, message.lastIndexOf("winnerId") - 1), winnerId);
                    // Re-enable all bottom cards
                    LinearLayout whitesScrolledLayout = findViewById(R.id.whitesScrolledLayout);
                    for (int i = 0; i < whitesScrolledLayout.getChildCount(); i++) {
                        whitesScrolledLayout.getChildAt(i).setEnabled(true);
                    }

                } else if (message.startsWith("cw")) {
                    Log.d(TAG, "whiteReceived: " + message);
                    // White card sent to czar by player
                    updateCzarWhite(message.substring(2), senderId);
                    numReceived++;
                    LinearLayout whiteLayout = findViewById(R.id.whitesScrolledLayout);
                    for (int i = 0; i < whiteLayout.getChildCount(); i++) {
                        whiteLayout.getChildAt(i).setEnabled(numReceived == mParticipants.size()-1);
                    }

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

            mParticipantId = mRoom.getParticipantId(mPlayerId);
            mCzarSubmission = new BigInteger(String.valueOf(new Random().nextInt(9)) + mPlayerId.substring(2));
            czarNums.add(mCzarSubmission);
            sendMsg("czarnum" + String.valueOf(mCzarSubmission));
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
                                this will be fixed when a custom waiting room UI is implemented, as it will only
                                change layout, not activity.*/
                    }
                });
    }

    private RoomStatusUpdateCallback mRoomStatusUpdateCallback = new RoomStatusUpdateCallback() {
        // Called when we are connected to the room. We're not ready to play yet! (maybe not everybody
        // is connected yet).
        @Override
        public void onConnectedToRoom(Room room) {

            // get participants and my ID:
            mParticipants = room.getParticipants();

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
            leaveRoomPrep(room);
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
            // Tell others players what to do after we leave
            if (isCzar) {
                // Czar left - Tell others to leave
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
                // Not as important to the game - May be able to leave freely
                if (room.getParticipants().size() <= 3) {
                    // Once this player leaves, game will be too boring - end game
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
        if (mRoom != null && mRealTimeMultiplayerClient != null && mRoomConfig != null) { mRealTimeMultiplayerClient.leave(mRoomConfig, mRoomId); }
    }

    protected void onResume() {
        super.onResume();
        if (returnedFromWaitingUi && !mPlaying) {
            Log.d(TAG, "returnedFromWaitingUI");
            finish();
        }
    }

    // ------------------------------------Main Game logic-------------------------------------------

    /* TODO (Bugs to fix):
        - Weird crash in runGame if isCzar
        - Czar selection popup thingy not showing
     */

    /* TODO: (Refactor)
       - Convert EVERYTHING IN APP (User facing) to @string
       - Replace all unnecesary sendReliableMessage with sendMsg
     */

    /* TODO: (Necessary Features)

        - Add tutorial button in homepage (Keep 4 buttons as newGame is leaving

        - Add support for choose multiple
            - PLAYER
                - Highlight multiple
            - CZAR
                - show in stack that expands horizontally and replaces all other cards
                - Disable select_winner when not in stack
                - Return button first in stack
     */

    /* TODO: (Optional features)
        - In scores on side, when a card has to be wrapped, it loses indentation. Fix this. (Find better solution than adding spaces at start) LOW

        - Add voice chat LOW
            - Icons for mute/silence in status bar


        - Custom waiting room UI MID

     */

    private void runGame() {

        Log.d(TAG, "runGame();");

        chooseCardBtn = findViewById(R.id.sendCardButton);
       
        sendMsg("name" + mName); // Share name

        if (isCzar) {

            chooseCardBtn.setEnabled(false);
            chooseCardBtn.setText(R.string.choose_card_czar);

            // Split decks
            wonCards = new HashMap<>();
            ArrayList<ArrayList<String>> whiteCardsSplit = splitDeck(whiteCards);

            // Send decks to all participants
            for (int i = 0; i < mParticipants.size(); i++) {
                if (mParticipants.get(i).getParticipantId().equals(mParticipantId)) { continue; }
                ArrayList<String> targetDeck;
                try {
                    targetDeck = whiteCardsSplit.get(i);
                } catch (IndexOutOfBoundsException e) {
                    targetDeck = whiteCardsSplit.get(i-1);
                }
                for (String card : targetDeck) {
                    sendTargetedMsg("w" + card, mParticipants.get(i));
                }
            }

            // Send initial black card
            Log.d(TAG, "numBlacks: " + blackCards.size());
            String newBlack = blackCards.get(new Random().nextInt(blackCards.size()));
            blackCards.remove(newBlack);
            updateBlack(newBlack);
            sendMsg("newblack " + newBlack);
        } else {
            chooseCardBtn.setEnabled(false);
            chooseCardBtn.setText(R.string.choose_card_player);
        }
    }

    private ArrayList<ArrayList<String>> splitDeck(ArrayList<String> deck) {
        ArrayList<ArrayList<String>> cardsSplit = new ArrayList<>();
        numPerDeck = Math.min(10, (int) Math.floor(deck.size() / mParticipants.size() - 1));
        for (int i = 0; i < mParticipants.size() - 1; i++) {
            ArrayList<String> cardsTmp = new ArrayList<>();
            for (int j = 0; j < numPerDeck; j++) {
                String newCard = deck.get(new Random().nextInt(deck.size()));
                cardsTmp.add(newCard);
                deck.remove(newCard);
            }
            cardsSplit.add(cardsTmp);
        }

        for (ArrayList<String> splitDeck : cardsSplit) {
            Log.d(TAG, "Split deck size: " + String.valueOf(splitDeck.size()));
        }
        return cardsSplit;
    }

    // Czar selection code

    private void requestCards() {
        View layoutInflated = getLayoutInflater().inflate(R.layout.czar_selection, null);
        ((ViewGroup) layoutInflated).removeView(layoutInflated.findViewById(R.id.tryPlayButton));
        LinearLayout cardsLayout = layoutInflated.findViewById(R.id.layout_decks);

        final ArrayList<CheckBox> checkboxes = new ArrayList<>();
        // Create checkboxes for deck selection
        // White boxes
        String deckColor = "white";
        ArrayList<String> whiteDecks = getDecks(deckColor);
        for (String deck : whiteDecks) {
            CheckBox whiteBox = new CheckBox(this);
            whiteBox.setText(deck);
            whiteBox.setTag(deckColor);
            whiteBox.setTextSize(18);
            CompoundButtonCompat.setButtonTintList(whiteBox, ColorStateList.valueOf(Color.parseColor("#CCCCCC")));
            checkboxes.add(whiteBox);
            cardsLayout.addView(whiteBox);
        } // Black boxes
        deckColor = "black";
        TextView divider = new TextView(this);
        cardsLayout.addView(divider);
        ArrayList<String> blackDecks = getDecks(deckColor);
        for (String deck : blackDecks) {
            CheckBox blackBox = new CheckBox(this);
            blackBox.setText(deck);
            blackBox.setTag(deckColor);
            blackBox.setTextSize(18);
            CompoundButtonCompat.setButtonTintList(blackBox, ColorStateList.valueOf(Color.parseColor("#CCCCCC")));
            checkboxes.add(blackBox);
            cardsLayout.addView(blackBox);
        }

        // Create dialog
        AlertDialog.Builder decksBuilder = new AlertDialog.Builder(mainGame.this);
        decksBuilder.setTitle("Select playable decks");
        decksBuilder.setCancelable(false);
        decksBuilder.setView(layoutInflated);
        decksBuilder.setPositiveButton(R.string.start_game, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Card selection validation & loading
                boolean whiteSelected = false;
                boolean blackSelected = false;
                for (CheckBox deck : checkboxes) {
                    try {
                        Log.d(TAG, "deckMd: " + deck.getTag().toString() +  deck.isChecked());
                        if (deck.getTag().equals("white") && deck.isChecked()) {
                            whiteSelected = true;
                            File whiteDeck = new File(getFilesDir().getAbsolutePath() + "/white/" + deck.getText().toString());
                            if (whiteDeck.exists()) {
                                BufferedReader br = new BufferedReader(new FileReader(whiteDeck));
                                String line;

                                while ((line = br.readLine()) != null) {
                                    whiteCards.add(line);
                                }
                                br.close();
                            } else {
                                BufferedReader reader = new BufferedReader(
                                        new InputStreamReader(getAssets().open("white/" + deck.getText().toString())));

                                String mLine;
                                while ((mLine = reader.readLine()) != null) {
                                    whiteCards.add(mLine);
                                }
                            }
                        } else if (deck.getTag().equals("black") && deck.isChecked()) {
                            blackSelected = true;
                            File blackDeck = new File(getFilesDir().getAbsolutePath() + "/black/" + deck.getText().toString());
                            if (blackDeck.exists()) {
                                BufferedReader br = new BufferedReader(new FileReader(blackDeck));
                                String line;

                                while ((line = br.readLine()) != null) {
                                    blackCards.add(line);
                                }
                                br.close();
                            } else {
                                BufferedReader reader = new BufferedReader(
                                        new InputStreamReader(getAssets().open("black/" + deck.getText().toString())));

                                String mLine;
                                while ((mLine = reader.readLine()) != null) {
                                    blackCards.add(mLine);
                                }
                            }
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error loading cards: ", e);
                    }
                }

                Log.d(TAG, "whiteSelected: " + whiteSelected);
                Log.d(TAG, "blackSelected: " + blackSelected);
                if ((whiteSelected && blackSelected)) {
                    runGame();
                } else {
                    deckSelection.show();
                }

            }
        });
        deckSelection = decksBuilder.create();
        deckSelection.show();
    }

    private ArrayList<String> getDecks (String deckColor) {
        // Custom decks
        File tmpFile = new File(getFilesDir().getAbsolutePath() + "/" + deckColor + "/");
        if (!tmpFile.exists()) { tmpFile.mkdir(); } // Prevent crashes caused by no custom decks existing
        ArrayList<String> decks = new ArrayList<>(Arrays.asList(tmpFile.list()));
        // Builtin decks
        final AssetManager assetmanager = getApplicationContext().getAssets();
        try {
            decks.addAll(Arrays.asList(assetmanager.list(deckColor)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return decks;
    }
    
    // Highlight cards

    private void highlightCard(View view) {
        Button targetWhite = (Button) view;
        LinearLayout buttonsLayout = findViewById(R.id.whitesScrolledLayout);
        for (int i = 0; i < buttonsLayout.getChildCount(); i++) {
            buttonsLayout.getChildAt(i).setBackgroundResource(R.drawable.white_card);
            if (!isCzar) {
                buttonsLayout.getChildAt(i).setTag(true);
            }
        }
        targetWhite.setBackgroundResource(R.drawable.selected_white); // Set card to selected
        targetCard = targetWhite;

        if (!isCzar) { targetWhite.setTag(false); }
        if (!isCzar || numReceived == mParticipants.size()-1) { chooseCardBtn.setEnabled(true); }
    }

    public void chooseCard(View view) {
        findViewById(R.id.sendCardButton).setEnabled(false);
        if (isCzar) {
            String newBlack = blackCards.get(new Random().nextInt(blackCards.size()));
            updateBlack(newBlack);
            sendMsg("newblack " + newBlack);

            String winningCardText = targetCard.getText().toString();
            String winnerId = targetCard.getTag().toString();

            sendMsg("win " + targetCard.getText().toString() + " winnerId " + winnerId);
            updateWins(winningCardText, winnerId);

            // Clear all cards from bottom of screen
            ((ViewGroup) findViewById(R.id.whitesScrolledLayout)).removeAllViews();

            // Update numReceived to ensure all played before czar makes selection
            numReceived = 0;
        } else {
            // Disable all cards
            LinearLayout whitesScrolledLayout = findViewById(R.id.whitesScrolledLayout);
            for (int i = 0; i < whitesScrolledLayout.getChildCount(); i++) {
                whitesScrolledLayout.getChildAt(i).setEnabled(false);
            }
            whitesScrolledLayout.removeView(targetCard);
            // Share white card with czar
            String whiteCardText = targetCard.getText().toString();
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
    }

    // Messaging

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

    // UI updates

    private void updateBlack(String newblack) {
        TextView blackPrompt = findViewById(R.id.blackCardMain);
        blackPrompt.setText(newblack);
    }

    private void updateWins(String wonCard, String winnerId) {
        Log.d(TAG, "wonCard " + wonCard);
        // Update wins hashmap
        if (idNames.get(winnerId) == null) {
            idNames.put(winnerId, mName);
        }
        if (wonCards == null) { wonCards = new HashMap<>(); }
        if (wonCards.containsKey(winnerId)) {
            // Update scores of existing participant
            wonCards.get(winnerId).add(wonCard);
        } else {
            // Add participant to scores
            ArrayList<String> newWinTmp = new ArrayList<>();
            newWinTmp.add(wonCard);
            Log.d(TAG, "idNames.get(winnerId) " + idNames.get(winnerId));
            wonCards.put(winnerId, newWinTmp);
        }

        // Update list of wins
        TextView winsText = findViewById(R.id.winsScrolledText);
        String newWinsMsg = "\n\n\nSCORES\n\n";
        for (Map.Entry<String, ArrayList<String>> win : wonCards.entrySet()) {
            newWinsMsg += (idNames.get(win.getKey()) + "\n");
            for (String cardWon : win.getValue()) {
                Log.d(TAG, "cardWon " + cardWon);
                newWinsMsg += ("    " + cardWon + "\n");
            }
            newWinsMsg += "\n";
        }

        winsText.setText(newWinsMsg);

        // Update tags on own white cards
        if (!isCzar) {
            LinearLayout whiteCardsLayout = findViewById(R.id.whitesScrolledLayout);
            for (int i = 0; i < whiteCardsLayout.getChildCount(); i++) {
                if (!(Boolean) whiteCardsLayout.getChildAt(i).getTag()) {
                    whiteCardsLayout.removeView(whiteCardsLayout.getChildAt(i));
                    break;
                }
            }
        } else {
            numPlayed++;
            if (numPlayed == numPerDeck) {
                sendMsg("leave");
                mRealTimeMultiplayerClient.leave(mRoomConfig, "Game over");
                finish();
                Intent showScores = new Intent(getApplicationContext(), scoreSheet.class);
                HashMap<String, ArrayList<String>> namedWonCards = replaceIds(wonCards);
                showScores.putExtra("scores", namedWonCards);
                startActivity(showScores);
            }
        }
    }

    private void updateCzarWhite(String message, String senderId) {
        LinearLayout whitesPlayed = findViewById(R.id.whitesScrolledLayout);
        found:
        {
            for (int i = 0; i < whitesPlayed.getChildCount(); i++) {
                if (whitesPlayed.getChildAt(i).getTag().equals(senderId)) {
                    Button btnToOverwrite = (Button) whitesPlayed.getChildAt(i);
                    if (btnToOverwrite.getTag().equals(senderId)) {
                        // Overwrite existing button
                        btnToOverwrite.setText(message);
                        break found;
                    }
                }
            }
            // This player hasn't played yet - Add their card
            // Add deck buttons
            Button playedWhiteCardBtn = new Button(getApplicationContext());
            playedWhiteCardBtn.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT));
            playedWhiteCardBtn.setText(message);
            playedWhiteCardBtn.setTag(senderId);
            playedWhiteCardBtn.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            highlightCard(v);
                        }
                    }
            );
            // Style button
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
            whitesPlayed.addView(playedWhiteCardBtn);
        }
    }

    private void updatePlayerWhite(String message) {
        Log.d(TAG, String.valueOf(whiteCards));
        // Received white card from czar - add to list of available cards
        if (whiteCards == null) { whiteCards = new ArrayList<>(); }
        LinearLayout whiteCardsLayout = findViewById(R.id.whitesScrolledLayout);

        whiteCards.add(message);

        // Add deck buttons
        Button whiteCardBtn = new Button(getApplicationContext());

        whiteCardBtn.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT));
        whiteCardBtn.setText(message);
        whiteCardBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        highlightCard(v);
                    }
                }
        );
        // Style button
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
        whiteCardBtn.setTag(true); // Determines playable
        whiteCardsLayout.addView(whiteCardBtn);
    }

    // Misc
    private HashMap<String, ArrayList<String>> replaceIds (HashMap<String, ArrayList<String>> idWins) {
        HashMap<String, ArrayList<String>> namedWins = new HashMap<>();
        for (Map.Entry<String, ArrayList<String>> win : idWins.entrySet()) {
            namedWins.put(idNames.get(win.getKey()), win.getValue());
        }
        return namedWins;
    }

    private void setupGame() {
        Log.d(TAG, "Game started!");
        setContentView(R.layout.main_game);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        myToolbar.setTitle("Cards against Society");
        setSupportActionBar(myToolbar);
        mPlaying = true;
    }
}