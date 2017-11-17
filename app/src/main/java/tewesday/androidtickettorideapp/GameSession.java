package tewesday.androidtickettorideapp;

import java.util.ArrayList;
import java.util.List;

public class GameSession
{
    private String mGameSessionID;
    private String mGameSessionName;
    private GamePlayer mOwner;

    private List<GamePlayer> mPlayerList = new ArrayList<>();

    final private int DEFAULTTRAINCOUNT = 45;

    GameSession()
    {

    }

    GameSession(String gameSessionID)
    {
        mGameSessionID = gameSessionID;
    }

    // Add a new player to this GameSession
    public void addNewPlayer(String userID, String playerName)
    {
        GamePlayer playerToAdd = new GamePlayer();
        playerToAdd.setAssociatedUserID(userID);
        playerToAdd.setPlayerName(playerName);
        playerToAdd.setTrainsLeft(DEFAULTTRAINCOUNT);
        if (mPlayerList.isEmpty())
        {
            playerToAdd.setPlayerID(1);
            mOwner = playerToAdd;
            mPlayerList.add(playerToAdd);
        }
        else
        {
            int playerNumber = (mPlayerList.size() + 1);
            playerToAdd.setPlayerID(playerNumber);
            mPlayerList.add(playerToAdd);
        }
    }

    // Re-add a player that was already assigned to this GameSession
    public void addExistingPlayer(GamePlayer player)
    {
        mPlayerList.add(player);
    }

    // Get a player from the PlayerList using the Player's ID
    public GamePlayer getPlayer(int playerID)
    {
        return mPlayerList.get(playerID - 1);
    }

    public String getGameSessionID() {
        return mGameSessionID;
    }

    public void setGameSessionID(String gameSessionID) {
        mGameSessionID = gameSessionID;
    }

    // Gets the entire PlayerList for the Firebase Database
    public List<GamePlayer> getPlayerList() {
        return mPlayerList;
    }

    // Sets the entire PlayerList for the Firebase Database
    public void setPlayerList(List<GamePlayer> playerList) {
        mPlayerList = playerList;
    }

    public String getGameSessionName() {
        return mGameSessionName;
    }

    public void setGameSessionName(String gameSessionName) {
        mGameSessionName = gameSessionName;
    }

    public GamePlayer getOwner() {
        return mOwner;
    }

    public void setmOwner(GamePlayer owner) {
        mOwner = owner;
    }
}
