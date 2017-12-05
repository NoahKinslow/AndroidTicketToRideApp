package tewesday.androidtickettorideapp;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import static android.content.ContentValues.TAG;



public class GameBoardMap
{
    public List<String> getCities() {
        return mCities;
    }

    // List of Cities
    private List<String> mCities;

    // Map of cities and routes connected to them
    private Map<String, GameCity> mCityMap = new LinkedHashMap<>();

    public List<GameRouteConnection> getRoutes() {
        return mRoutes;
    }

    private List<GameRouteConnection> mRoutes;

    // constructor
    GameBoardMap()
    {
        mCities = new ArrayList<>();
    }

    // Read data from input streams and then create a BoardMap from that data
    public void createBoardMap(InputStream citiesStream, InputStream routesStream)
    {
        Gson gson = new Gson();
        BufferedReader br = new BufferedReader(new InputStreamReader((citiesStream)));
        Type type = new TypeToken<List<GameCity>>(){}.getType();
        List<GameCity> cities = gson.fromJson(br, type);

        for (int i = 0;i < cities.size();i++)
        {
            mCities.add(cities.get(i).getCityName());
            addCity(cities.get(i).getCityName());
        }

        gson = new Gson();
        br = new BufferedReader(new InputStreamReader((routesStream)));
        type = new TypeToken<List<GameRouteConnection>>(){}.getType();
        List<GameRouteConnection> routes = gson.fromJson(br, type);

        mRoutes = routes;

        for (int i = 0;i < routes.size(); i++)
        {
            routes.get(i).setConnectionID(i);
            addRouteConnection(routes.get(i));
        }
    }

    // Add a city to the City list
    private void addCity(String cityName)
    {
        GameCity city = new GameCity(cityName);
        mCities.add(cityName);
        mCityMap.put(cityName, city);
    }

    public boolean removeCity(String cityName)
    {
        for (int i = 0; i < mCities.size(); i++)
        {
            if (mCities.get(i).equals(cityName))
            {
                mCities.remove(i);
                return true;
            }
        }
        return false;
    }

    // Add a route connection to a city
    private void addRouteConnection(GameRouteConnection routeConnection)
    {
        mCityMap.get(routeConnection.getSourceCity()).addRouteConnection(routeConnection);
    }

    // Get a RouteConnection using its sourceCity, destinationCity, and connection ID
    public GameRouteConnection getRouteConnection(String sourceCity, String destinationCity, int connectionID)
    {
        for (int i = 0;i < mCityMap.get(sourceCity).getmCityRoutes().size();i++)
        {
            if (mCityMap.get(sourceCity).getmCityRoutes().get(i).getDestinationCity().equals(destinationCity) &&
                    mCityMap.get(sourceCity).getmCityRoutes().get(i).getConnectionID() == connectionID)
            {
                return mCityMap.get(sourceCity).getmCityRoutes().get(i);
            }
        }
        return null;
    }

    // Write a given RouteConnection's data to the Firebase Database
    public void writeRouteDataToFirebase(String gameSession, GameRouteConnection routeConnection)
    {
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Games").child(gameSession).child("MapData").child("Routes");

        myRef.child(myRef.push().getKey()).setValue(routeConnection);
        updateRouteConnection(routeConnection);
    }

    // Read all current RouteConnection data from the Firebase Database, setup Listeners for new RouteConnection additions
    public void readRouteDataFromFireabase(String gameSession)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Games").child(gameSession).child("MapData").child("Routes");

        // Read all current data once
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    GameRouteConnection routeConnection = snapshot.getValue(GameRouteConnection.class);
                    updateRouteConnection(routeConnection);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Setup listeners for when future routeConnections are added
        myRef.addChildEventListener(new ChildEventListener(){
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName)
            {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                GameRouteConnection routeConnection = dataSnapshot.getValue(GameRouteConnection.class);
                Log.d(TAG, "Value is: " + routeConnection.toString());
                updateRouteConnection(routeConnection);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot)
            {

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String string)
            {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String string)
            {

            }
        });
    }

    // Find a routeConnection in the CityMap and update(replace) it with the given routeConnection
    public void updateRouteConnection(GameRouteConnection routeConnection)
    {
        for (int i = 0;i < mCityMap.get(routeConnection.getSourceCity()).getmCityRoutes().size();i++)
        {
            if ((mCityMap.get(routeConnection.getSourceCity()).getmCityRoutes().get(i).getConnectionID()) == routeConnection.getConnectionID())
            {
                mCityMap.get(routeConnection.getSourceCity()).updateRouteConnection(routeConnection, i);
                break;
            }
        }
    }







}
