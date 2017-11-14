package tewesday.androidtickettorideapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity
{
    private static final int RC_SIGN_IN = 123;

    GameLogicMaster mGameLogicMaster;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            // already signed in
        } else {
            // not signed in
            Intent intent = new Intent();
            startActivityForResult(intent, RC_SIGN_IN);
        }

        setupGame();

    }

    public void setupGame()
    {
        mGameLogicMaster = new GameLogicMaster();

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Games");

        DatabaseReference pushedPostRef = myRef.child(myRef.push().getKey());

        String gameSessionID = pushedPostRef.getKey();

        GameSession gameSession = new GameSession(gameSessionID);

        pushedPostRef.setValue(gameSession);

        mGameLogicMaster.assignGameSession(gameSession);

        InputStream destinationTicketsStream = getApplicationContext().getResources().openRawResource(R.raw.tickettoride_basicna_destinationtickets);
        InputStream citiesStream = getApplicationContext().getResources().openRawResource(R.raw.tickettoride_basicna_cities);
        InputStream routeStream = getApplicationContext().getResources().openRawResource(R.raw.tickettoride_basicna_cityrouteconnections);

        mGameLogicMaster.setupFiles(destinationTicketsStream, citiesStream, routeStream);
        mGameLogicMaster.setupDestinationTickets();
        mGameLogicMaster.setupGameBoardMap();
        mGameLogicMaster.loadGameSessionDataFromFirebase();
        mGameLogicMaster.updateRoute(1, 0);
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