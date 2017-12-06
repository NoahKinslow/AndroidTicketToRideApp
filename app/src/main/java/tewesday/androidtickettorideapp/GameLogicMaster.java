package tewesday.androidtickettorideapp;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GameLogicMaster implements Parcelable
{
    private List<GameDestinationTicket> mDestinationTickets = new ArrayList<>();
    private List<GameDestinationTicket> mProposedTickets;
    private GameBoardMap mGameBoardMap;
    private GameActivity mGameActivity;
    private InputStream mDestinationTicketsStream;
    private InputStream mCitiesStream;
    private InputStream mRoutesStream;
    private GameSession mGameSession;
    private List<GamePlayer> mGamePlayers;
    private List<Integer> TrainDeck = new ArrayList<>();
    private List<Integer> DiscardTrainDeck = new ArrayList<>();
    private List<Integer> drawPiles = new ArrayList<>();
    private boolean mIsAITurn = false;
    private final boolean mIsAIGame = true;

    GameLogicMaster()
    {

    }

    // Setup the basic files to be read from
    public void setupFiles(InputStream destinationTicketsStream, InputStream citiesStream, InputStream routesStream)
    {
        mDestinationTicketsStream = destinationTicketsStream;
        mCitiesStream = citiesStream;
        mRoutesStream = routesStream;
    }

    // Setup the DestinationTickets
    public void setupDestinationTickets()
    {
        Gson gson = new Gson();
        BufferedReader br = new BufferedReader(new InputStreamReader((mDestinationTicketsStream)));
        Type type = new TypeToken<List<GameDestinationTicket>>(){}.getType();
        List<GameDestinationTicket> tickets = gson.fromJson(br, type);

        mDestinationTickets = tickets;
    }

    public void setupDrawPiles()
    {
        // Check if draw piles are empty
        if (drawPiles.isEmpty())
        {
            for (int i = 0; i < 6; i++)
            {
                drawPiles.add(drawCardFromTrainDeck());
            }
        }
    }

    // Setup the GameBoardMap with city and route data
    public void setupGameBoardMap()
    {
        mGameBoardMap = new GameBoardMap();
        mGameBoardMap.createBoardMap(mCitiesStream, mRoutesStream);
    }

    public void setupTrainDeck()
    {
        // Add correct number of Train Cards to TrainDeck
        for (int trainCounter = 0; trainCounter < 14; trainCounter++)
        {
            TrainDeck.add(0);
        }
        for (int trainColorCounter = 1; trainColorCounter < 9; trainColorCounter++)
        {
            for (int trainCounter = 0; trainCounter < 12; trainCounter++)
            {
                TrainDeck.add(trainColorCounter);
            }
        }
        Collections.shuffle(TrainDeck);
    }

    // Load all current GameSession data from the Firebase
    public void loadGameSessionDataFromFirebase()
    {
        mGameBoardMap.readRouteDataFromFireabase(mGameSession.getGameSessionID());
    }

    // Function to call from GUI to update a route with player info
    public void updateRoute(int playerID, int routeConnectionID)
    {
        // placeholder value
        String sourceCity = "Montreal";
        String destinationCity = "Boston";
        GameRouteConnection routeConnection = mGameBoardMap.getRouteConnection(sourceCity, destinationCity, routeConnectionID);
        routeConnection.setPlayerControlled(true);
        routeConnection.setPlayerID(playerID);
        mGameBoardMap.writeRouteDataToFirebase(mGameSession.getGameSessionID(), routeConnection);
    }

    public void AITurn() {
        //TODO

    }

    public void PlaceTrains(GameRouteConnection route) {
        //TODO
    }

    /// <summary>
    /// Draws card from pileNumber
    /// </summary>
    /// <param name="pileNumber">0-5 0 is deck</param>
    /// <returns>int color of card drawn</returns>
    public int drawCard(int pileNumber)
    {
        int cardColor = drawPiles.get(pileNumber);

        //update hand
        drawPiles.set(pileNumber, drawCardFromTrainDeck());
        DiscardTrainDeck.add(cardColor);
        checkDiscardPile();

        return cardColor;
    }

    private int drawCardFromTrainDeck()
    {
        int trainCard = TrainDeck.get(0);
        TrainDeck.remove(0);
        return trainCard;
    }

    public void checkDiscardPile()
    {
        // Check if discard pile is full
        if (DiscardTrainDeck.size() == 103)
        {
            // Shuffle discard pile into train deck
            Collections.shuffle(DiscardTrainDeck);
            TrainDeck.addAll(DiscardTrainDeck);
            DiscardTrainDeck.clear();
        }
    }

    public boolean startGame(String userID)
    {
        if (userID.equals(mGameSession.getOwner().getAssociatedUserID()))
        {
            mGameSession.setGameStarted(true);
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("Games").child(mGameSession.getGameSessionID()).child("gameStarted");
            myRef.setValue(true);
            // Give everyone tickets... other game start stuff...
            return true;
        }
        // Give everyone tickets... other game start stuff...
        return false;
    }

    //GETTERS AND SETTERS
    /// <summary>
    /// Adds selected tickets to players tickets
    /// of AI's tickets
    /// </summary>
    /// <param name="selected">List of the tickets selcted</param>
    public void selectedTicket(List<GameDestinationTicket> selected)
    {
        //add ticket to player's tickets.
        for (GameDestinationTicket ticket : selected)
        {
            for (int i = 0; i < mProposedTickets.size(); i++)
            {
                if (mProposedTickets.get(i).equals(ticket))
                {
                    if (mIsAITurn)
                    //add to AI tickets
                    {
                        mGamePlayers.get(1).addTicket(ticket);
                    }
                    else
                    //add to player's tickets
                    {
                        if (mIsAIGame)
                        {
                            mGamePlayers.get(0).addTicket(ticket);
                        }
                        else
                        {
                            //TODO: Get player
                            //String userID = authLink.User.LocalId;
                            //GameSession.PlayerList[GameSession.GetPlayerID(userID)].AddTicket(ticket);
                        }
                    }

                    mProposedTickets.remove(i);
                    i = 0;
                }
            }
        }

        //add ticket to deck of tickets
        if (!mProposedTickets.isEmpty())
        {
            mDestinationTickets.addAll(mProposedTickets);
        }

        mProposedTickets = null;
    }

    public List<GameDestinationTicket> getProposedTickets() {
        mProposedTickets = new ArrayList<>(Arrays.asList(mDestinationTickets.get(0),
                mDestinationTickets.get(1),
                mDestinationTickets.get(2)));
        return mProposedTickets;
    }



    public int getDrawPileCardColor(int drawpile) {
        return drawPiles.get(drawpile);
    }

    public List<GamePlayer> getGamePlayers() {
        return mGamePlayers;
    }

    public int getCardNumber(int i) {
        return 0;
    }

    public void assignGameSession(GameSession gameSession)
    {
        mGameSession = gameSession;
    }

    public GameActivity getGameActivity() {
        return mGameActivity;
    }

    public void setGameActivity(GameActivity gameActivity) {
        this.mGameActivity = gameActivity;
    }

    public List<GameDestinationTicket> getDestinationTickets() {
        return mDestinationTickets;
    }

    public void setDestinationTickets(List<GameDestinationTicket> mDestinationTickets) {
        this.mDestinationTickets = mDestinationTickets;
    }

    public GameBoardMap getGameBoardMap() {
        return mGameBoardMap;
    }

    public GamePlayer getPlayer(int index)
    {
        return mGamePlayers.get(index);
    }

    //PARCELABLE
    protected GameLogicMaster(Parcel in) {
        mDestinationTickets = in.createTypedArrayList(GameDestinationTicket.CREATOR);
        mProposedTickets = in.createTypedArrayList(GameDestinationTicket.CREATOR);
        mGameBoardMap = in.readParcelable(GameBoardMap.class.getClassLoader());
        mGameSession = in.readParcelable(GameSession.class.getClassLoader());
        mGamePlayers = in.createTypedArrayList(GamePlayer.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(mDestinationTickets);
        dest.writeTypedList(mProposedTickets);
        dest.writeParcelable(mGameBoardMap, flags);
        dest.writeParcelable(mGameSession, flags);
        dest.writeTypedList(mGamePlayers);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GameLogicMaster> CREATOR = new Creator<GameLogicMaster>() {
        @Override
        public GameLogicMaster createFromParcel(Parcel in) {
            return new GameLogicMaster(in);
        }

        @Override
        public GameLogicMaster[] newArray(int size) {
            return new GameLogicMaster[size];
        }

    };

}
