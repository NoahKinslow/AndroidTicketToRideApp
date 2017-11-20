package tewesday.androidtickettorideapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.util.Objects;

public class MainActivity extends AppCompatActivity
{
    private static final int RC_SIGN_IN = 123;
    private FirebaseAuth mAuthentication;
    private String mGameSessionName;

    GameLogicMaster mGameLogicMaster;

    Button mJoinGameButton;
    Button mCreateGameButton;

    private InputStream mDestinationTicketsStream;
    private InputStream mCitiesStream;
    private InputStream mRoutesStream;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkLogin();

        Button signOutButton = findViewById(R.id.signOutButton);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mAuthentication = FirebaseAuth.getInstance();
                mAuthentication.signOut();
                checkLogin();
            }
        });

        setupGameSessionButtons();
        prepareInputFiles();
    }

    // Split this into more functions!
    // Boolean value key: newGame = true, setup a new GameSession. newGame = false, look for an existing GameSession
    public void setupGame(Boolean newGame)
    {
        if (newGame)
        {
            mGameLogicMaster = new GameLogicMaster();

            // Write a message to the database
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("Games");

            DatabaseReference pushedPostRef = myRef.child(myRef.push().getKey());

            String gameSessionID = pushedPostRef.getKey();

            GameSession gameSession = new GameSession(gameSessionID);

            if (mAuthentication.getCurrentUser() != null)
            {
                if (mAuthentication.getCurrentUser().getDisplayName() != null) {
                    if (mAuthentication.getCurrentUser().getDisplayName().equals("")) {
                        gameSession.addNewPlayer(mAuthentication.getCurrentUser().getUid(), "Anonymous");
                    }
                }
                else
                {
                    gameSession.addNewPlayer(mAuthentication.getCurrentUser().getUid(), mAuthentication.getCurrentUser().getDisplayName());
                }
            }


            pushedPostRef.setValue(gameSession);

            Toast.makeText(MainActivity.this, "Game " + mGameSessionName + " created.",
                    Toast.LENGTH_SHORT).show();

            setupGameLogicMaster(gameSession);

        }
        else
        {
            mGameLogicMaster = new GameLogicMaster();

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("Games");

            // Read all current data once
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    boolean gameFound = false;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.child("gameSessionName").getValue().equals(mGameSessionName)) {
                            GameSession gameSession = snapshot.getValue(GameSession.class);
                            Toast.makeText(MainActivity.this, "Game " + mGameSessionName + " found.",
                                    Toast.LENGTH_SHORT).show();
                            gameFound = true;

                            if (!gameSession.isGameStarted())
                            {
                                // Check if the GameSession is full
                                if (gameSession.getPlayerList().size() == 4) {
                                    // Check if User is in GameSession
                                    if (gameSession.searchForPlayer(mAuthentication.getCurrentUser().getUid()) != null) {
                                        Toast.makeText(MainActivity.this, "Waiting for " + mGameSessionName + " to start. Rejoining now...",
                                                Toast.LENGTH_SHORT).show();

                                        // Assign User to matching Player
                                        gameSession.searchForPlayer(mAuthentication.getCurrentUser().getUid());

                                        setupGameLogicMaster(gameSession);

                                    } else {
                                        Toast.makeText(MainActivity.this, "Game " + mGameSessionName + " is full",
                                                Toast.LENGTH_SHORT).show();

                                    }
                                } else {
                                        // Game found and started, check if joining User is a Player
                                        if (gameSession.searchForPlayer(mAuthentication.getCurrentUser().getUid()) != null) {
                                            Toast.makeText(MainActivity.this, "Welcome back to " + mGameSessionName + ". Rejoining now...",
                                                    Toast.LENGTH_SHORT).show();

                                            // Assign User to matching Player
                                            gameSession.searchForPlayer(mAuthentication.getCurrentUser().getUid());

                                            setupGameLogicMaster(gameSession);
                                        }
                                        else
                                        {
                                            Toast.makeText(MainActivity.this, "You are not in " + mGameSessionName + ".",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                }
                            }
                        }
                    }
                    if (!gameFound)
                    {
                        Toast.makeText(MainActivity.this, "Game " + mGameSessionName + " not found.",
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public void prepareInputFiles()
    {
        mDestinationTicketsStream = getApplicationContext().getResources().openRawResource(R.raw.tickettoride_basicna_destinationtickets);
        mCitiesStream = getApplicationContext().getResources().openRawResource(R.raw.tickettoride_basicna_cities);
        mRoutesStream = getApplicationContext().getResources().openRawResource(R.raw.tickettoride_basicna_cityrouteconnections);
    }

    public void setupGameLogicMaster(GameSession gameSession)
    {
        mGameLogicMaster.assignGameSession(gameSession);
        mGameLogicMaster.setupFiles(mDestinationTicketsStream, mCitiesStream, mRoutesStream);
        mGameLogicMaster.setupDestinationTickets();
        mGameLogicMaster.setupGameBoardMap();
        mGameLogicMaster.loadGameSessionDataFromFirebase();
        // Switch to GameBoardActivity/UI here
    }

    public void setupGameSessionButtons()
    {
        mJoinGameButton = findViewById(R.id.joinGameButton);
        mJoinGameButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView gameSessionNameTextView = findViewById(R.id.gameSessionText);
                mGameSessionName = gameSessionNameTextView.getText().toString();
                setupGame(false);
            }
        });
        mCreateGameButton = findViewById(R.id.createGameButton);
        mCreateGameButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView gameSessionNameTextView = findViewById(R.id.gameSessionText);
                mGameSessionName = gameSessionNameTextView.getText().toString();
                setupGame(true);
            }
        });
    }

    private void checkLogin()
    {
        mAuthentication = FirebaseAuth.getInstance();
        if (mAuthentication.getCurrentUser() != null) {
            // already signed in
            if (Objects.equals(mAuthentication.getCurrentUser().getDisplayName(), ""))
            {
                Toast.makeText(MainActivity.this, "Signed in as " + mAuthentication.getCurrentUser().getEmail(),
                        Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(MainActivity.this, "Signed in as " + mAuthentication.getCurrentUser().getDisplayName(),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // not signed in
            Intent intent = new Intent(this, AuthenticationActivity.class);

            startActivityForResult(intent, RC_SIGN_IN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            //IdpResponse response = IdpResponse.fromResultIntent(data);

//            if (resultCode == ResultCodes.OK) {
//                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//                // ...
//            } else {
//                // Sign in failed, check response for error code
//                // ...
//            }
        }
    }



}