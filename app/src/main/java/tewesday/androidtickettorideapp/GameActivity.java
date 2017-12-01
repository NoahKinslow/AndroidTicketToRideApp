package tewesday.androidtickettorideapp;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener{

    private GoogleMap mMap;
    private int RADIUS = 80000;
    private List<GameRouteConnection> mRoutes;
    private List<CityCoordinates> mCities;
    private List<String> mCityArray;
    //Wild, Red, Blue, Yellow, Green, Orange, Pink, Black, White
    private final int[] ROUTE_COLORS = {
            Color.GRAY,
            Color.RED,
            Color.BLUE,
            Color.YELLOW,
            Color.GREEN,
            Color.rgb(255,165,0),
            Color.rgb(255,192,203),
            Color.BLACK,
            Color.WHITE
    };

    @Override
    public void onPolylineClick(Polyline polyline) {

    }

    private class CityCoordinates
    {
        public LatLng mLocation;
        public String mName;

        public CityCoordinates(String name, LatLng latLng)
        {
            mLocation = latLng;
            mName = name;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                        .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mRoutes = getIntent().getParcelableArrayListExtra("ROUTE");
        mCityArray = getIntent().getStringArrayListExtra("CITY");
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * If Google Play services is not installed on the device, the user will be prompted to install
         * it inside the SupportMapFragment. This method will only be triggered once the user has
         * installed Google Play services and returned to the app.
         */

        mMap = googleMap;

        mCities = createListOfCities();

        //add Circles
        addCitiesToMap();

        //draw Lines
        addRoutesToMap();

        //Listener
        mMap.setOnPolylineClickListener(this);

        //http://www.kansastravel.org/geographicalcenter.htm
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(39.8283, -98.5795)));
    }

    private void addCitiesToMap() {
        for (CityCoordinates city : mCities) {
            CircleOptions cir = new CircleOptions()
                    .center(city.mLocation)
                    .radius(RADIUS)
                    .fillColor(Color.BLACK)
                    .zIndex(200);

            mMap.addCircle(cir);
        }
    }

    private void addRoutesToMap()
    {
        for(GameRouteConnection route : mRoutes)
        {
            String sourceStr = route.getSourceCity();
            String desStr = route.getDestinationCity();

            CityCoordinates source = getCityCoordinates(sourceStr);
            CityCoordinates des = getCityCoordinates(desStr);

            if(source == null || des == null)
                break;

            PolylineOptions poly = new PolylineOptions()
                    .add(source.mLocation)
                    .add(des.mLocation)
                    .color(ROUTE_COLORS[route.getRouteColor()])
                    .width(15)
                    .zIndex(100)
                    .clickable(true);

            mMap.addPolyline(poly);
        }
    }


    private CityCoordinates getCityCoordinates(String city)
    {
        for(CityCoordinates c : mCities)
        {
            if(city.equals(c.mName))
            {
                return c;
            }
        }
        return null;
    }

    private List<CityCoordinates> createListOfCities() {

        List<CityCoordinates> cities = new ArrayList<>();

        Geocoder geo = new Geocoder(this);
        for (String cityName : mCityArray) {
            List<Address> addresses = null;
            try {
                addresses = geo.getFromLocationName(cityName, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Address city = addresses.get(0);
            double lat = city.getLatitude();
            double lng = city.getLongitude();
            cities.add(new CityCoordinates(cityName, new LatLng(lat, lng)));
        }
        return cities;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
}
