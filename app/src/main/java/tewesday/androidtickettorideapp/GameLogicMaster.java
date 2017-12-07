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
import java.util.Random;

public class GameLogicMaster implements Parcelable
{
    private List<GameDestinationTicket> mDestinationTickets;
    private List<GameDestinationTicket> mProposedTickets;
    private GameBoardMap mGameBoardMap;
    private GameActivity mGameActivity;
    private InputStream mDestinationTicketsStream;
    private InputStream mCitiesStream;
    private InputStream mRoutesStream;
    private GameSession mGameSession;
    private List<GamePlayer> mGamePlayers;
    private List<Integer> TrainDeck;
    private List<Integer> DiscardTrainDeck;
    private List<Integer> drawPiles;
    private boolean mIsAITurn;
    private boolean mIsAIGame;

    GameLogicMaster()
    {

        TrainDeck = new ArrayList<>();
        DiscardTrainDeck = new ArrayList<>();
        drawPiles = new ArrayList<>();
        mIsAITurn = false;
        mIsAIGame = true;
        mDestinationTickets = new ArrayList<>();
        mGamePlayers = new ArrayList<>();
        mProposedTickets = new ArrayList<>();
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

    public void setupPlayers(boolean isAIGame)
    {
        if(isAIGame)
        {
            GamePlayer player = new GamePlayer();
            player.setPlayerColor(7);
            player.setPlayerID(0);
            player.setPlayerName("You");
            GamePlayer ai = new GamePlayer();
            ai.setPlayerColor(2);
            ai.setPlayerID(1);
            ai.setPlayerName("Computer");

            mGamePlayers.add(player);
            mGamePlayers.add(ai);
        }
        else
        {
            //firebase player
        }
    }

    // Load all current GameSession data from the Firebase
    public void loadGameSessionDataFromFirebase()
    {
        mGameBoardMap.readRouteDataFromFireabase(mGameSession.getGameSessionID());
    }

    // Function to call from GUI to update a route with player info
    public void updateRoute(int playerID, GameRouteConnection route)
    {
        route.setPlayerControlled(true);
        route.setPlayerID(playerID);
        if(!mIsAIGame)
            mGameBoardMap.writeRouteDataToFirebase(mGameSession.getGameSessionID(), route);
    }

    public void AITurn() {

        mIsAITurn = true;

        if(mGamePlayers.get(1).getTickets().isEmpty())
        {
            //make sure player is not using selected tickets
            while(!mProposedTickets.isEmpty())
                selectedTicket(getProposedTickets());
        }

        //https://stackoverflow.com/questions/2706500/how-do-i-generate-a-random-int-number-in-c
        Random rand = new Random();
        int randomNumber = rand.nextInt(100);

        if(randomNumber > 50)//|| card == 0)
        {
            int randCard = rand.nextInt(6);
            drawCard(1, randCard);
            mGameActivity.updateDrawPile(randCard, getDrawPileCardColor(randCard));
            randCard = rand.nextInt(6);
            drawCard(1, randCard);
            mGameActivity.updateDrawPile(randCard, getDrawPileCardColor(randCard));
        }
        else
        {
            boolean placed = false;
            int attempts = 0;
            while (!placed && attempts < 1000)
            {
                int randRoute = rand.nextInt(mGameBoardMap.getRoutes().size());
                boolean valid = PlaceTrains(mGameBoardMap.getRoutes().get(randRoute));
                if (valid)
                {
                    placed = true;
                    mGameActivity.claimRoute(mGamePlayers.get(1), mGameBoardMap.getRoutes().get(randRoute));
                }
                attempts++;
            }

            if (!placed)
            {
                //draw cards if cant take turn
                int randCard = rand.nextInt(6);
                drawCard(1, randCard);
                mGameActivity.updateDrawPile(randCard, getDrawPileCardColor(randCard));
                randCard = rand.nextInt(6);
                drawCard(1, randCard);
                mGameActivity.updateDrawPile(randCard, getDrawPileCardColor(randCard));
            }
        }
        mIsAITurn = false;
    }

    public boolean PlaceTrains(GameRouteConnection route) {
        int i = 0;
        if (mIsAIGame)
            i = mIsAITurn ? 1 : 0;
        else
            ;//i = /*FIREBASE USER ID*/

        if (isValidMove(i, route)) {
            updateRoute(i, route);
            getPlayer(i).setTrainsLeft(getPlayer(i).getTrainsLeft() - route.getTrainDistance());
            if (getPlayer(i).getTrainsLeft() < 3) {
                gameOver();
            }
            mGameActivity.updatePlayerTrains(i, getPlayer(i).getTrainsLeft());
            addRouteScore(i, route);

            validateTickets(i);
            return true;
        } else {
            return false;
        }
    }

    private void gameOver() {

        for (GamePlayer p : mGamePlayers) {
            validateTickets(p.getPlayerID());
        }

        boolean iWon = true;
        for (GamePlayer p : mGamePlayers) {
            if(p.getScore() > getPlayer(0).getScore())
                iWon = false;
        }
        mGameActivity.gameOver(iWon);
    }

    public void validateTickets(int playerID)
    {
        // Check if a ticket has been completed
        for (GameDestinationTicket ticket : getPlayer(playerID).getTickets())
        {
            if (!ticket.isCompleted())
            {
                if (mGameBoardMap.validateTicket(ticket, getPlayer(playerID)))
                {
                    ticket.setIsCompleted(true);
                    getPlayer(playerID).setScore(
                            getPlayer(playerID).getScore() + ticket.getPointValue());
                    mGameActivity.ticketComplete(ticket);
                    mGameActivity.updatePlayerPoints(playerID, getPlayer(playerID).getScore());


                    if(!mIsAIGame)
                        ;// update firebase
                }
            }
        }
    }

    private void addRouteScore(int playerid, GameRouteConnection route) {
        int points = 0;
        switch (route.getTrainDistance())
        {
            case 1:
                points = 1;
                break;
            case 2:
                points = 2;
                break;
            case 3:
                points = 4;
                break;
            case 4:
                points = 7;
                break;
            case 5:
                points = 10;
                break;
            case 6:
                points = 15;
                break;
        }
        getPlayer(playerid).setScore(getPlayer(playerid).getScore() + points);
        mGameActivity.updatePlayerPoints(playerid, getPlayer(playerid).getScore());
    }


    /// <summary>
    /// Draws card from pileNumber
    /// </summary>
    /// <param name="pileNumber">0-5 0 is deck</param>
    /// <returns>int color of card drawn</returns>
    public int drawCard(int player, int pileNumber)
    {
        int cardColor = drawPiles.get(pileNumber);

        int currentNum = getPlayer(player).getTrainCards().get(cardColor);
        getPlayer(player).updateTrainCard(cardColor, currentNum + 1);

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
        //handles incase button is clicked > 1 time and select same tickets.
        if(mProposedTickets.isEmpty())
            return;
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

        mProposedTickets = new ArrayList<>();
    }

    public List<GameDestinationTicket> getProposedTickets() {

        if(!mProposedTickets.isEmpty())
            return mProposedTickets;

        Collections.shuffle(mDestinationTickets);

        mProposedTickets = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            if (!mDestinationTickets.isEmpty()) {
                //first element
                mProposedTickets.add(mDestinationTickets.get(0));
                mDestinationTickets.remove(0);
            }
        }

        return mProposedTickets;
    }

    public int getDrawPileCardColor(int drawpile) {
        return drawPiles.get(drawpile);
    }

    public List<GamePlayer> getGamePlayers() {
        return mGamePlayers;
    }

    public int getCardNumber(int color) {
        if(!getPlayer(0).getTrainCards().isEmpty())
            return getPlayer(0).getTrainCards().get(color);
        else
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

    public boolean isValidMove(int playerid, GameRouteConnection route) {
        if(!route.isPlayerControlled())
        {
            //if card of routecolor >= route trains
            if(getPlayer(playerid).getTrainCards().get(route.getRouteColor())
                    >= route.getTrainDistance())
                return true;
            //if players wild cards >= route trains
            if(getPlayer(playerid).getTrainCards().get(0) >= route.getTrainDistance())
                return true;
            //if route is wild and player has card of quantity >= route trains
            if(route.getRouteColor() == 0) {
                for (int i : getPlayer(playerid).getTrainCards())
                    if (i >= route.getTrainDistance())
                        return true;
            }
        }
        return false;
    }

    //PARCELABLE
    protected GameLogicMaster(Parcel in) {
        mDestinationTickets = new ArrayList<>();
        mDestinationTickets = in.createTypedArrayList(GameDestinationTicket.CREATOR);
        mProposedTickets = new ArrayList<>();
        mProposedTickets = in.createTypedArrayList(GameDestinationTicket.CREATOR);
        mGameBoardMap = in.readParcelable(GameBoardMap.class.getClassLoader());
        mGameSession = in.readParcelable(GameSession.class.getClassLoader());
        mGamePlayers = new ArrayList<>();
        mGamePlayers = in.createTypedArrayList(GamePlayer.CREATOR);
        TrainDeck = new ArrayList<>();
        in.readList(TrainDeck, Integer.class.getClassLoader());
        DiscardTrainDeck = new ArrayList<>();
        in.readList(DiscardTrainDeck, Integer.class.getClassLoader());
        drawPiles = new ArrayList<>();
        in.readList(drawPiles, Integer.class.getClassLoader());
        mIsAITurn = false;
        mIsAITurn = in.readByte() != 0;
        mIsAIGame = true;
        mIsAIGame = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(mDestinationTickets);
        dest.writeTypedList(mProposedTickets);
        dest.writeParcelable(mGameBoardMap, flags);
        dest.writeParcelable(mGameSession, flags);
        dest.writeTypedList(mGamePlayers);
        dest.writeList(TrainDeck);
        dest.writeList(DiscardTrainDeck);
        dest.writeList(drawPiles);
        dest.writeByte((byte) (mIsAITurn ? 1 : 0));
        dest.writeByte((byte) (mIsAIGame ? 1 : 0));
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

    public void setGameBoardMap(GameBoardMap gameBoardMap) {
        mGameBoardMap = gameBoardMap;
    }
}
