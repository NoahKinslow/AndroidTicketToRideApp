package tewesday.androidtickettorideapp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GameLogicMaster
{
    private List<GameDestinationTicket> mDestinationTickets = new ArrayList<>();
    private GameBoardMap mGameBoardMap;
    private InputStream mDestinationTicketsStream;
    private InputStream mCitiesStream;
    private InputStream mRoutesStream;
    private GameSession mGameSession;

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
}