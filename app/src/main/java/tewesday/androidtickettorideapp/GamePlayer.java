package tewesday.androidtickettorideapp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class GamePlayer implements Parcelable
{
    private String mAssociatedUserID;
    private String mPlayerName;
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

    protected GamePlayer(Parcel in) {
        mAssociatedUserID = in.readString();
        mPlayerName = in.readString();
        mPlayerID = in.readInt();
        mTrainsLeft = in.readInt();
        mScore = in.readInt();
        mPlayerColor = in.readInt();
        mTickets = in.createTypedArrayList(GameDestinationTicket.CREATOR);
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
