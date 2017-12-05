package tewesday.androidtickettorideapp;

import java.util.List;

public class GamePlayer
{
    private String mAssociatedUserID;
    private String mPlayerName;

    public int getmPlayerColor() {
        return mPlayerColor;
    }

    public void setmPlayerColor(int mPlayerColor) {
        this.mPlayerColor = mPlayerColor;
    }

    private int mPlayerColor;
    private int mPlayerID;
    private int mTrainsLeft = 45;
    private int mScore;
    private int mPlayerColor;
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

    public int getScore() {
        return mScore;
    }

    public void setScore(int score) {
        mScore = score;
    }

    public int getPlayerColor() {
        return mPlayerColor;
    }

    public void setPlayerColor(int playerColor) {
        mPlayerColor = playerColor;
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
