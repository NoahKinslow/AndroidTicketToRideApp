package tewesday.androidtickettorideapp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;


public class GameCity implements Parcelable
{
    private String mCityName;
    private ArrayList<GameRouteConnection> mCityRoutes = new ArrayList<>();

    // This default constructor is for the Firebase Database to use
    GameCity()
    {

    }

    // Constructor, setup the city's name
    GameCity(String cityName)
    {
        mCityName = cityName;
    }

    protected GameCity(Parcel in) {
        mCityName = in.readString();
        mCityRoutes = in.createTypedArrayList(GameRouteConnection.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mCityName);
        dest.writeTypedList(mCityRoutes);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GameCity> CREATOR = new Creator<GameCity>() {
        @Override
        public GameCity createFromParcel(Parcel in) {
            return new GameCity(in);
        }

        @Override
        public GameCity[] newArray(int size) {
            return new GameCity[size];
        }
    };

    // Add a routeConnection to this city
    public void addRouteConnection(GameRouteConnection routeConnection)
    {
        mCityRoutes.add(routeConnection);
    }

    // Update (replace) a routeConnection at the given index with the given routeConnection
    public void updateRouteConnection(GameRouteConnection routeConnection, int indexOfRoute)
    {
        mCityRoutes.remove(indexOfRoute);
        mCityRoutes.add(routeConnection);
    }

    // Give this city a name
    public void setCityName(String cityName)
    {
        mCityName = cityName;
    }

    // Get this city's name
    public String getCityName()
    {
        return mCityName;
    }

    public ArrayList<GameRouteConnection> getCityRoutes() {
        return mCityRoutes;
    }

    public void setCityRoutes(ArrayList<GameRouteConnection> mCityRoutes) {
        this.mCityRoutes = mCityRoutes;
    }

    @Override
    public String toString() {
        String cityRoutes = "";
        for (int i = 0; i < mCityRoutes.size();i++)
        {
            cityRoutes += mCityRoutes.get(i).toString();
        }
        return "GameCity{" +
                "mCityName='" + mCityName + '\'' +
                ", mCityRoutes=" + cityRoutes +
                '}';
    }
}
