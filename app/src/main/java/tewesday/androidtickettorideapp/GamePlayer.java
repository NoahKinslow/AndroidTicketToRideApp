package tewesday.androidtickettorideapp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GamePlayer implements Parcelable
{
    private String mAssociatedUserID;
    private String mPlayerName;
    private int mPlayerID;
    private int mTrainsLeft;
    private int mScore;
    private int mPlayerColor;
    private List<Integer> mTrainCards;
    private List<GameDestinationTicket> mTickets;

    // Default constructor for Firebase Database
    GamePlayer(){}

    GamePlayer(boolean aiGame)
    {
        mTickets = new ArrayList<>();
        mTrainsLeft = 45;
        mScore = 0;
        mTrainCards = Arrays.asList(0,0,0,0,0,0,0,0,0);
    }

    public void addTicket(GameDestinationTicket ticket)
    {
        mTickets.add(ticket);
    }

    protected GamePlayer(Parcel in) {
        mAssociatedUserID = in.readString();
        mPlayerName = in.readString();
        mPlayerID = in.readInt();
        mTrainsLeft = 45;
        mTrainsLeft = in.readInt();
        mScore = 0;
        mScore = in.readInt();
        mPlayerColor = in.readInt();
        mTickets = new ArrayList<>();
        mTickets = in.createTypedArrayList(GameDestinationTicket.CREATOR);
        mTrainCards = new ArrayList<>();
        in.readList(mTrainCards, Integer.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mAssociatedUserID);
        dest.writeString(mPlayerName);
        dest.writeInt(mPlayerID);
        dest.writeInt(mTrainsLeft);
        dest.writeInt(mScore);
        dest.writeInt(mPlayerColor);
        dest.writeTypedList(mTickets);
        dest.writeList(mTrainCards);

    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GamePlayer> CREATOR = new Creator<GamePlayer>() {
        @Override
        public GamePlayer createFromParcel(Parcel in) {
            return new GamePlayer(in);
        }

        @Override
        public GamePlayer[] newArray(int size) {
            return new GamePlayer[size];
        }
    };

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

    public List<Integer> getTrainCards() {
        return mTrainCards;
    }

    public void updateTrainCard(int index, int cards)
    {
        mTrainCards.set(index, cards);
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

    public void setTrainCards(List<Integer> trainCards) {
        mTrainCards = trainCards;
    }

    public List<GameDestinationTicket> getTickets() {
        return mTickets;
    }

    public void setTickets(List<GameDestinationTicket> tickets) {
        mTickets = tickets;
    }

}
