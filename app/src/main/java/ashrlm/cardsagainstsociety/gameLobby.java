package ashrlm.cardsagainstsociety;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class gameLobby extends Activity {

    private String mRoomId;
    private static long role;
    private String mPlayerId;
    private List<Participant> mParticipants;
    private int MIN_PLAYERS = 1;
    private int MAX_PLAYERS = 1;
    private RoomConfig mRoomConfig;
    private boolean isCzar = false;
    private String mMyParticipantId;
    private boolean mPlaying = false;
    private ArrayList<String> whiteCards = new ArrayList<>();
    private ArrayList<String> blackCards = new ArrayList<>();
    private final String TAG = "ashrlm.cas";
    private boolean gameStartAttempted = false;
    private static final int RC_WAITING_ROOM = 9007;
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
            String sender = realTimeMessage.getSenderParticipantId();
            try {
                String message = new String(buf, "utf-8");
                if (message.charAt(0) == 'w') {
                    whiteCards.add(message);
                } else if (message.charAt(0) == 'b') {
                    //TODO: Update main black card - Added once I do the main game UI
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
        String whiteCardText = whiteButton.getText().toString();
        mRealTimeMultiplayerClient.sendReliableMessage(
                whiteCardText.getBytes(),
                mRoomId,
                czarId,
                null

        );
    }
}