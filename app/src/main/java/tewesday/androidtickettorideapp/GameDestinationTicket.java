package tewesday.androidtickettorideapp;


public class GameDestinationTicket
{
    private String mSourceCity;
    private String mDestinationCity;
    private int mPointValue;

    private boolean mIsCompleted = false;

    GameDestinationTicket()
    {

    }

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
