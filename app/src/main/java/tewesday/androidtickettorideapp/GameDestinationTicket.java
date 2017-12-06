package tewesday.androidtickettorideapp;


import android.os.Parcel;
import android.os.Parcelable;

public class GameDestinationTicket implements Parcelable
{
    private String mSourceCity;
    private String mDestinationCity;
    private int mPointValue;

    private boolean mIsCompleted = false;

    GameDestinationTicket()
    {

    }

    protected GameDestinationTicket(Parcel in) {
        mSourceCity = in.readString();
        mDestinationCity = in.readString();
        mPointValue = in.readInt();
        mIsCompleted = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mSourceCity);
        dest.writeString(mDestinationCity);
        dest.writeInt(mPointValue);
        dest.writeByte((byte) (mIsCompleted ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GameDestinationTicket> CREATOR = new Creator<GameDestinationTicket>() {
        @Override
        public GameDestinationTicket createFromParcel(Parcel in) {
            return new GameDestinationTicket(in);
        }

        @Override
        public GameDestinationTicket[] newArray(int size) {
            return new GameDestinationTicket[size];
        }
    };

    public String getSourceCity()
    {
        return mSourceCity;
    }

    public void setSourceCity(String sourceCity)
    {
        mSourceCity = sourceCity;
    }

    public String getDestinationCity()
    {
        return mDestinationCity;
    }

    public void setDestinationCity(String destinationCity)
    {
        mDestinationCity = destinationCity;
    }

    public int getPointValue()
    {
        return mPointValue;
    }

    public void setPointValue(int pointValue)
    {
        mPointValue = pointValue;
    }

    public boolean isCompleted() {
        return mIsCompleted;
    }

    public void setIsCompleted(boolean mIsCompleted) {
        this.mIsCompleted = mIsCompleted;
    }

    @Override
    public String toString() {
        return "GameDestinationTicket{" +
                "mSourceCity='" + mSourceCity + '\'' +
                ", mDestinationCity='" + mDestinationCity + '\'' +
                ", mPointValue=" + mPointValue +
                ", mIsCompleted=" + mIsCompleted +
                '}';
    }
}
