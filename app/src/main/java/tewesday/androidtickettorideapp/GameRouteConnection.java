package tewesday.androidtickettorideapp;

public class GameRouteConnection
{
    // Connection's unique ID to differentiate from a similar connection
    private int mConnectionID;
    // Name of the Source City
    private String mSourceCity;
    // Name of the Destination City
    private String mDestinationCity;
    // Distance from Source to Destination in train count
    private int mTrainDistance;
    // Color of Route Connection/Train color required
    private int mRouteColor;
    // Color Key: Grey:0,Red:1,Blue:2,Yellow:3,Green:4,Orange:5,Pink:6,Black:7,White:8

    // Does a player own this route?
    private boolean mPlayerControlled = false;
    // What is the ID of the player that owns this route
    private int mPlayerID;


    // This default constructor is for Firebase Database to use
    GameRouteConnection()
    {

    }

    GameRouteConnection(int connectionID)
    {
        mConnectionID = connectionID;
    }

    GameRouteConnection(int connectionID, String cityName, String cityConnectedTo, int trainDistance, int routeColor)
    {
        mConnectionID = connectionID;
        mSourceCity = cityName;
        mDestinationCity = cityConnectedTo;
        mTrainDistance = trainDistance;
        mRouteColor = routeColor;
    }

    public int getConnectionID()
    {
        return mConnectionID;
    }

    public String getSourceCity()
    {
        return mSourceCity;
    }

    public String getDestinationCity()
    {
        return mDestinationCity;
    }

    public int getTrainDistance()
    {
        return mTrainDistance;
    }

    public int getRouteColor()
    {
        return mRouteColor;
    }

    public void setConnectionID(int connectionID)
    {
        mConnectionID = connectionID;
    }

    public void setSourceCity(String cityName)
    {
        mSourceCity = cityName;
    }

    public void setDestinationCity(String cityName)
    {
        mDestinationCity = cityName;
    }

    public void setTrainDistance(int trainDistance)
    {
        mTrainDistance = trainDistance;
    }

    public void setRouteColor(int routeColor)
    {
        mRouteColor = routeColor;
    }

    public boolean isPlayerControlled() {
        return mPlayerControlled;
    }

    public void setPlayerControlled(boolean playerControlled) {
        mPlayerControlled = playerControlled;
    }

    public int getPlayerID() {
        return mPlayerID;
    }

    public void setPlayerID(int playerID) {
        mPlayerID = playerID;
    }

    @Override
    public String toString()
    {
        return "GameRouteConnection{" +
                "mConnectionID=" + mConnectionID +
                ", mSourceCity='" + mSourceCity + '\'' +
                ", mDestinationCity='" + mDestinationCity + '\'' +
                ", mTrainDistance=" + mTrainDistance +
                ", mRouteColor=" + mRouteColor +
                ", mPlayerControlled=" + mPlayerControlled +
                ", mPlayerID=" + mPlayerID +
                '}';
    }
}
