package tewesday.androidtickettorideapp;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameLogicMaster implements Parcelable
{


    public List<GameDestinationTicket> getDestinationTickets() {
        return mDestinationTickets;
    }

    public void setDestinationTickets(List<GameDestinationTicket> mDestinationTickets) {
        this.mDestinationTickets = mDestinationTickets;
    }

    private List<GameDestinationTicket> mDestinationTickets = new ArrayList<>();
    private List<GameDestinationTicket> mProposedTickets;


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

    public GameBoardMap getGameBoardMap() {
        return mGameBoardMap;
    }

    private GameBoardMap mGameBoardMap;
    private InputStream mDestinationTicketsStream;
    private InputStream mCitiesStream;
    private InputStream mRoutesStream;
    private GameSession mGameSession;
    private List<GamePlayer> mGamePlayers;

    GameLogicMaster()
    {

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

    // Setup the GameBoardMap with city and route data
    public void setupGameBoardMap()
    {
        mGameBoardMap = new GameBoardMap();
        mGameBoardMap.createBoardMap(mCitiesStream, mRoutesStream);
    }

    // Load all current GameSession data from the Firebase
    public void loadGameSessionDataFromFirebase()
    {
        mGameBoardMap.readRouteDataFromFireabase(mGameSession.getGameSessionID());
    }

    // Setup the basic files to be read from
    public void setupFiles(InputStream destinationTicketsStream, InputStream citiesStream, InputStream routesStream)
    {
        mDestinationTicketsStream = destinationTicketsStream;
        mCitiesStream = citiesStream;
        mRoutesStream = routesStream;
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

    public void assignGameSession(GameSession gameSession)
    {
        mGameSession = gameSession;
    }

    public void AITurn() {

    }

    public void PlaceTrains(GameRouteConnection route) {


    }

    public GamePlayer getPlayer(int index)
    {
        return mGamePlayers.get(index);
    }

    public void drawCard(int pileNum) {

    }

    public List<GameDestinationTicket> getProposedTickets() {
        mProposedTickets = new ArrayList<>(Arrays.asList(mDestinationTickets.get(0),
                mDestinationTickets.get(1),
                mDestinationTickets.get(2)));
        return mProposedTickets;
    }

    public int getDrawPileCardColor(int drawpile) {
        return 0;
    }

    public List<GamePlayer> getGamePlayers() {
        return mGamePlayers;
    }

    public int getCardNumber(int i) {
        return 0;
    }
}
