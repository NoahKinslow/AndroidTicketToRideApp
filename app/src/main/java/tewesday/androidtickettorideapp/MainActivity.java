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
    private String mGameSessionName;

    GameLogicMaster mGameLogicMaster;

    Button mJoinGameButton;
    Button mCreateGameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkLogin();

        Button signOutButton = findViewById(R.id.signOutButton);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                auth.signOut();
                checkLogin();
            }
        });

        setupGameSessionButtons();

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
            gameSession.setGameSessionName(mGameSessionName);

            FirebaseAuth auth = FirebaseAuth.getInstance();

            if (auth.getCurrentUser() != null)
            {
                if (auth.getCurrentUser().getDisplayName() != null) {
                    if (auth.getCurrentUser().getDisplayName().equals("")) {
                        gameSession.addNewPlayer(auth.getCurrentUser().getUid(), "Anonymous");
                    }
                }
                else
                {
                    gameSession.addNewPlayer(auth.getCurrentUser().getUid(), auth.getCurrentUser().getDisplayName());
                }
            }


            pushedPostRef.setValue(gameSession);

            Toast.makeText(MainActivity.this, "Game " + mGameSessionName + " created.",
                    Toast.LENGTH_SHORT).show();

            mGameLogicMaster.assignGameSession(gameSession);

            InputStream destinationTicketsStream = getApplicationContext().getResources().openRawResource(R.raw.tickettoride_basicna_destinationtickets);
            InputStream citiesStream = getApplicationContext().getResources().openRawResource(R.raw.tickettoride_basicna_cities);
            InputStream routeStream = getApplicationContext().getResources().openRawResource(R.raw.tickettoride_basicna_cityrouteconnections);

            mGameLogicMaster.setupFiles(destinationTicketsStream, citiesStream, routeStream);
            mGameLogicMaster.setupDestinationTickets();
            mGameLogicMaster.setupGameBoardMap();
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
                                    FirebaseAuth auth = FirebaseAuth.getInstance();
                                    if (gameSession.searchForPlayer(auth.getCurrentUser().getUid()) != null) {
                                        Toast.makeText(MainActivity.this, "Waiting for " + mGameSessionName + " to start. Rejoining now...",
                                                Toast.LENGTH_SHORT).show();

                                        // Assign User to matching Player
                                        gameSession.searchForPlayer(auth.getCurrentUser().getUid());

                                        mGameLogicMaster.assignGameSession(gameSession);

                                        InputStream destinationTicketsStream = getApplicationContext().getResources().openRawResource(R.raw.tickettoride_basicna_destinationtickets);
                                        InputStream citiesStream = getApplicationContext().getResources().openRawResource(R.raw.tickettoride_basicna_cities);
                                        InputStream routeStream = getApplicationContext().getResources().openRawResource(R.raw.tickettoride_basicna_cityrouteconnections);

                                        mGameLogicMaster.setupFiles(destinationTicketsStream, citiesStream, routeStream);
                                        mGameLogicMaster.setupDestinationTickets();
                                        mGameLogicMaster.setupGameBoardMap();
                                        mGameLogicMaster.loadGameSessionDataFromFirebase();
                                    } else {
                                        Toast.makeText(MainActivity.this, "Game " + mGameSessionName + " is full",
                                                Toast.LENGTH_SHORT).show();

                                    }
                                } else {
                                        // Game found and started, check if joining User is a Player
                                        FirebaseAuth auth = FirebaseAuth.getInstance();
                                        if (gameSession.searchForPlayer(auth.getCurrentUser().getUid()) != null) {
                                            Toast.makeText(MainActivity.this, "Welcome back to " + mGameSessionName + ". Rejoining now...",
                                                    Toast.LENGTH_SHORT).show();

                                            // Assign User to matching Player
                                            gameSession.searchForPlayer(auth.getCurrentUser().getUid());

                                            mGameLogicMaster.assignGameSession(gameSession);

                                            InputStream destinationTicketsStream = getApplicationContext().getResources().openRawResource(R.raw.tickettoride_basicna_destinationtickets);
                                            InputStream citiesStream = getApplicationContext().getResources().openRawResource(R.raw.tickettoride_basicna_cities);
                                            InputStream routeStream = getApplicationContext().getResources().openRawResource(R.raw.tickettoride_basicna_cityrouteconnections);

                                            mGameLogicMaster.setupFiles(destinationTicketsStream, citiesStream, routeStream);
                                            mGameLogicMaster.setupDestinationTickets();
                                            mGameLogicMaster.setupGameBoardMap();
                                            mGameLogicMaster.loadGameSessionDataFromFirebase();
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
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            // already signed in
            if (Objects.equals(auth.getCurrentUser().getDisplayName(), ""))
            {
                Toast.makeText(MainActivity.this, "Signed in as " + auth.getCurrentUser().getEmail(),
                        Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(MainActivity.this, "Signed in as " + auth.getCurrentUser().getDisplayName(),
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