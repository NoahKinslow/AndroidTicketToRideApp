package tewesday.androidtickettorideapp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class GameSession implements Parcelable
{
    private String mGameSessionID;
    private String mGameSessionName;
    private GamePlayer mOwner;
    private boolean mGameStarted = false;

    private List<GamePlayer> mPlayerList = new ArrayList<>();

    final private int DEFAULTTRAINCOUNT = 45;

    GameSession()
    {

    }

    GameSession(String gameSessionID)
    {
        mGameSessionID = gameSessionID;
    }

    protected GameSession(Parcel in) {
        mGameSessionID = in.readString();
        mGameSessionName = in.readString();
        mOwner = in.readParcelable(GamePlayer.class.getClassLoader());
        mGameStarted = in.readByte() != 0;
        mPlayerList = in.createTypedArrayList(GamePlayer.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mGameSessionID);
        dest.writeString(mGameSessionName);
        dest.writeParcelable(mOwner, flags);
        dest.writeByte((byte) (mGameStarted ? 1 : 0));
        dest.writeTypedList(mPlayerList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GameSession> CREATOR = new Creator<GameSession>() {
        @Override
        public GameSession createFromParcel(Parcel in) {
            return new GameSession(in);
        }

        @Override
        public GameSession[] newArray(int size) {
            return new GameSession[size];
        }
    };

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
/*    public void addExistingPlayer(GamePlayer player)
    {
        mPlayerList.add(player);
    }*/

    // Get a player from the PlayerList using the Player's ID
    public GamePlayer getPlayer(int playerID)
    {
        return mPlayerList.get(playerID - 1);
    }

    // Search for a player in the PlayerList using the Player's UserID
    public GamePlayer searchForPlayer(String userID)
    {
        for (GamePlayer player : mPlayerList)
        {
            if (player.getAssociatedUserID().equals(userID))
            {
                return player;
            }
        }
        return null;
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

    public void setOwner(GamePlayer owner) {
        mOwner = owner;
    }

    public boolean isGameStarted() {
        return mGameStarted;
    }

    public void setGameStarted(boolean gameStarted) {
        mGameStarted = gameStarted;
    }
}
