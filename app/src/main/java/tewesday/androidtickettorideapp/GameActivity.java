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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.ui.IconGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GameActivity extends AppCompatActivity implements OnMapReadyCallback,
                                                                GoogleMap.OnPolylineClickListener,
        GoogleMap.OnCameraMoveListener

{

    private GoogleMap mMap;
    private int RADIUS = 80000;
    private List<GameRouteConnection> mRoutes;
    private List<CityCoordinates> mCities;
    private List<String> mCityArray;
    private List<Marker> mMarkers;
    private List<Polyline> mPolylines;
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

    public void drawPileClick(View view) {

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

        mMarkers = new ArrayList<>();
        mPolylines = new ArrayList<>();
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
        mMap.setMaxZoomPreference(7.5f);

        mCities = createListOfCities();

        //add Circles
        addCitiesToMap();

        //draw Lines
        addRoutesToMap();

        //Listener for line taps
        mMap.setOnPolylineClickListener(this);

        //Listener for zooming
        mMap.setOnCameraMoveListener(this);

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

            LatLng sourceLoc = source.mLocation;
            LatLng desLoc = des.mLocation;

            Polyline otherLine = null;
            for(Polyline p : mPolylines)
            {
                if (p.getPoints().get(0).equals(sourceLoc) &&
                        p.getPoints().get(1).equals(desLoc))
                {
                    otherLine = p;
                }
            }

            if(otherLine != null)
            {
                double b = lineLength(sourceLoc, desLoc);
                double a = sourceLoc.latitude - desLoc.latitude;
                double c = sourceLoc.longitude - desLoc.longitude;

                double ratio = .35 / b;

                double y = a * ratio;
                double x = c * ratio;

                List<LatLng> otherPoints = new ArrayList<>();
                otherPoints.add(new LatLng(sourceLoc.latitude + x, sourceLoc.longitude - y));
                otherPoints.add(new LatLng(desLoc.latitude + x, desLoc.longitude - y));
                otherLine.setPoints(otherPoints);

                sourceLoc = new LatLng(source.mLocation.latitude - x, source.mLocation.longitude + y);
                desLoc = new LatLng(des.mLocation.latitude - x, des.mLocation.longitude + y);
            }
            else
            {
                LatLng midPoint = getMidPoint(source.mLocation, des.mLocation);
                //https://stackoverflow.com/questions/22536845/android-google-map-marker-with-label
                IconGenerator label = new IconGenerator(this);
                label.setStyle(IconGenerator.STYLE_WHITE);
                //https://stackoverflow.com/questions/3656371/dynamic-string-using-string-xml
                String labelText = getString(R.string.RouteLabel, route.getTrainDistance());
                addIcon(label, labelText, midPoint);
            }

            PolylineOptions poly = new PolylineOptions()
                    .add(sourceLoc)
                    .add(desLoc)
                    .color(ROUTE_COLORS[route.getRouteColor()])
                    .width(25)
                    .zIndex(100)
                    .clickable(true);
            mPolylines.add(mMap.addPolyline(poly));
            mPolylines.get(mPolylines.size() - 1).setTag(route);
        }
    }

    //https://github.com/googlemaps/android-maps-utils/blob/master/demo/src/com/google/maps/android/utils/demo/IconGeneratorDemoActivity.java
    private void addIcon(IconGenerator iconFactory, CharSequence text, LatLng position) {
        MarkerOptions markerOptions = new MarkerOptions().
                icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(text))).
                position(position).
                visible(false).
                anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());

        mMarkers.add(mMap.addMarker(markerOptions));
    }

    //https://stackoverflow.com/questions/46508549/google-maps-android-only-show-markers-below-a-certain-zoom-level
    //https://stackoverflow.com/questions/38727517/oncamerachangelistener-is-deprecated
    @Override
    public void onCameraMove()
    {
        for(Marker m : mMarkers)
        {
            if(mMap.getCameraPosition().zoom > 5)
                m.setVisible(true);
            else
                m.setVisible(false);
        }
    }

    private double lineLength(LatLng p1, LatLng p2)
    {
        return Math.sqrt(Math.pow(p1.latitude - p2.latitude, 2)
                + Math.pow(p1.longitude - p2.longitude, 2));
    }

    private LatLng getMidPoint(LatLng p1, LatLng p2)
    {
        return new LatLng(p1.latitude + ((p2.latitude - p1.latitude) / 2),
                          p1.longitude + ((p2.longitude - p1.longitude) / 2));
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

        do {
            if (Geocoder.isPresent())
            {
                Geocoder geo = new Geocoder(this);
                for (String cityName : mCityArray) {
                    List<Address> addresses = null;
                    try {
                        //gives some human assistance ;)
                        if (Objects.equals(cityName, "Washington"))
                            addresses = geo.getFromLocationName(cityName + " DC", 1);
                        else if (Objects.equals(cityName, "Vancouver"))
                            addresses = geo.getFromLocationName(cityName + " Canada", 1);
                        else
                            addresses = geo.getFromLocationName(cityName, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (addresses != null && !addresses.isEmpty()) {
                        Address city = addresses.get(0);
                        double lat = city.getLatitude();
                        double lng = city.getLongitude();
                        cities.add(new CityCoordinates(cityName, new LatLng(lat, lng)));
                    }
                }
            }
        }while(!Geocoder.isPresent() || cities.isEmpty());
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
