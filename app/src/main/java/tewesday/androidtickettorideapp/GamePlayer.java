package tewesday.androidtickettorideapp;

import java.util.List;

public class GamePlayer
{
    private String mAssociatedUserID;
    private String mPlayerName;
    private int mPlayerID;
    private int mTrainsLeft;

    private List<GameTrainCards> mTrainCards;
    private List<GameDestinationTicket> mTickets;

    // Default constructor for Firebase Database
    GamePlayer()
    {

    }

    public String getAssociatedUserID()
    {
        return mAssociatedUserID;
    }

    public void setAssociatedUserID(String userID)
    {
        mAssociatedUserID = userID;
    }

    public String getPlayerName() {
        return mPlayerName;
    }

    public void setPlayerName(String playerName) {
        mPlayerName = playerName;
    }

    public int getPlayerID() {
        return mPlayerID;
    }

    public void setPlayerID(int playerID) {
        mPlayerID = playerID;
    }

    public int getTrainsLeft() {
        return mTrainsLeft;
    }

    public void setTrainsLeft(int trainsLeft) {
        mTrainsLeft = trainsLeft;
    }

    public List<GameTrainCards> getTrainCards() {
        return mTrainCards;
    }

    public void setTrainCards(List<GameTrainCards> trainCards) {
        mTrainCards = trainCards;
    }

    public List<GameDestinationTicket> getTickets() {
        return mTickets;
    }

    public void setTickets(List<GameDestinationTicket> tickets) {
        mTickets = tickets;
    }
}
