package tewesday.androidtickettorideapp;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.util.ArraySet;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.ui.IconGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GameActivity extends AppCompatActivity implements OnMapReadyCallback,
                                                                GoogleMap.OnPolylineClickListener,
                                                                GoogleMap.OnCameraMoveListener
{

    private GoogleMap mMap;
    private final int RADIUS = 80000;
    private final int HIGHLIGHT_RADIUS = 120000;
    private Pair<Integer, Integer> mSelectedCard = null;
    private List<GameRouteConnection> mRoutes;
    private List<CityCoordinates> mCities;
    private List<String> mCityArray;
    private List<Marker> mMarkers;
    private List<Polyline> mPolylines;
    private List<Circle> mCircles;
    //LayoutItems
    private List<Button> mHandButtons;
    private List<ImageView> mDrawPile;
    private List<TextView> mAIInfo;
    private List<TextView> mPlayerInfo;
    private ImageView mAIImage;
    private ImageView mPlayerImage;
    private ScrollView mTicketScrollView;
    //GameControl
    private GameLogicMaster mGameLogicMaster;
    private final boolean mIsAIGame = true;
    private boolean mIsAITurn = false;
    private int mTimesTapped = 0;
    private final List<PatternItem> DOT_PATERN =
            new ArrayList<PatternItem>(Arrays.asList(new Dot()));

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

        if(getIntent().hasExtra("GAMELOGICMASTER"))
        {
            mGameLogicMaster = getIntent().getParcelableExtra("GAMELOGICMASTER");
            mRoutes = mGameLogicMaster.getGameBoardMap().getRoutes();
            mCityArray = mGameLogicMaster.getGameBoardMap().getCities();
        }
        else
        {
            mGameLogicMaster = new GameLogicMaster();
            mRoutes = getIntent().getParcelableArrayListExtra("ROUTE");
            mCityArray = getIntent().getStringArrayListExtra("CITY");
        }
        mMarkers = new ArrayList<>();
        mPolylines = new ArrayList<>();
        mCircles = new ArrayList<>();

        initializeLayoutItems();
    }

    private void initializeLayoutItems()
    {
        mHandButtons = new ArrayList<>();
        mHandButtons.add((Button)findViewById(R.id.handCard0));
        mHandButtons.add((Button)findViewById(R.id.handCard1));
        mHandButtons.add((Button)findViewById(R.id.handCard2));
        mHandButtons.add((Button)findViewById(R.id.handCard3));
        mHandButtons.add((Button)findViewById(R.id.handCard4));
        mHandButtons.add((Button)findViewById(R.id.handCard5));
        mHandButtons.add((Button)findViewById(R.id.handCard6));
        mHandButtons.add((Button)findViewById(R.id.handCard7));
        mHandButtons.add((Button)findViewById(R.id.handCard8));

        for (Button button : mHandButtons)
        {
            button.setTag(R.string.COLOR_TAG, mHandButtons.indexOf(button));
        }

        mDrawPile = new ArrayList<>();
        mDrawPile.add((ImageView)findViewById(R.id.drawPileDeck));
        mDrawPile.add((ImageView)findViewById(R.id.drawPile1));
        mDrawPile.add((ImageView)findViewById(R.id.drawPile2));
        mDrawPile.add((ImageView)findViewById(R.id.drawPile3));
        mDrawPile.add((ImageView)findViewById(R.id.drawPile4));
        mDrawPile.add((ImageView)findViewById(R.id.drawPile5));
        for (Button button : mHandButtons)
        {
            button.setTag(R.string.PILE_TAG, mHandButtons.indexOf(button));
        }

        mAIInfo = new ArrayList<>();
        mAIInfo.add((TextView)findViewById(R.id.AIname));
        mAIInfo.add((TextView)findViewById(R.id.AIpoints));
        mAIInfo.add((TextView)findViewById(R.id.AItrains));

        mPlayerInfo = new ArrayList<>();
        mPlayerInfo.add((TextView)findViewById(R.id.Playername));
        mPlayerInfo.add((TextView)findViewById(R.id.Playerpoints));
        mPlayerInfo.add((TextView)findViewById(R.id.Playertrains));

        mAIImage = findViewById(R.id.AIimage);
        mPlayerImage = findViewById(R.id.Playerimage);
        mTicketScrollView = findViewById(R.id.ticketScrollView);
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

            mCircles.add(mMap.addCircle(cir));
            mCircles.get(mCircles.size() - 1).setTag(city);
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

            //If it is a double route
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
                    .width(25)
                    .zIndex(100)
                    .clickable(true);

            if (route.isPlayerControlled())
            {
                poly.color(ROUTE_COLORS[route.getPlayerColor()]);
            }
            else
            {
                poly.color(ROUTE_COLORS[route.getRouteColor()]);
                poly.pattern(DOT_PATERN);
            }

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

    //Listeners
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

    //Click Listeners
    @Override
    public void onPolylineClick(Polyline polyline)
    {
        if (mSelectedCard == null ||
                mTimesTapped > 0)
            return;

        GameRouteConnection route = (GameRouteConnection) polyline.getTag();
        if(isCardAndRouteSeletion(mSelectedCard,route))
        {
            mGameLogicMaster.PlaceTrains(route);
            //TODO:Change Line
            GamePlayer player = mGameLogicMaster.getPlayer(0);
            claimRoute(player, route);

            Toast.makeText(this, "Route Claimed!", Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(this, "Invalid Move", Toast.LENGTH_LONG).show();
        }
    }

    public void drawPileClick(View view)
    {
        if(mTimesTapped > 1)
            return;

        mTimesTapped++;

        int pileNum = getDrawPileIndexFromImageView((ImageView)view);
        int color = getColorFromDrawPile(pileNum);

        if(color == 0)
            mTimesTapped++;

        //add card to hand display
        updateHandDisplay(color, getNumHandCardsFromColor(color) + 1);

        //TODO:Tell gameLogic about it

        //See if turn is over
        if(mTimesTapped > 1)
        {
            //end turn
            if (mIsAIGame) {
                setAITurn(true);
                mGameLogicMaster.AITurn();
            }
            mTimesTapped = 0;
        }
    }

    public void handCardClick(View view)
    {
        Button button = (Button) view;
        int color = (int) button.getTag(R.string.COLOR_TAG);
        int number = Integer.parseInt(button.getText().toString());
        mSelectedCard = new Pair<>(color, number);
    }

    public void onTicketClick(View view)
    {
        GameDestinationTicket ticket = (GameDestinationTicket) view.getTag();
        Circle source = getCircleFromCityName(ticket.getSourceCity());
        Circle des = getCircleFromCityName(ticket.getDestinationCity());

        if(source.getRadius() == RADIUS || des.getRadius() == RADIUS)
        {
            source.setRadius(HIGHLIGHT_RADIUS);
            des.setRadius(HIGHLIGHT_RADIUS);
        }
        else
        {
            source.setRadius(RADIUS);
            des.setRadius(RADIUS);
        }
    }

    //UI updaters

    public void updatePlayerName(String name)
    {
        mPlayerInfo.get(0).setText(name);
    }

    public void updateAIName(String name)
    {
        mAIInfo.get(0).setText(name);
    }

    public void updatePlayerPoints(int points)
    {
        mPlayerInfo.get(1).setText(getString(R.string.points, points));
    }
    public void updateAIPoints(int points)
    {
        mAIInfo.get(1).setText(getString(R.string.points, points));
    }

    public void updatePlayerTrains(int trains)
    {
        mPlayerInfo.get(2).setText(getString(R.string.trainsLeft, trains));
    }

    public void updateAITrains(int trains)
    {
        mAIInfo.get(2).setText(getString(R.string.trainsLeft, trains));
    }

    public void updatePlayerImage(Bitmap image)
    {
        mPlayerImage.setImageBitmap(image);
    }

    public void updateAIImage(Bitmap image)
    {
        mAIImage.setImageBitmap(image);
    }

    public void updateDrawPile(int drawPile, int color)
    {
        mDrawPile.get(drawPile).setImageBitmap(getCardImageFromColor(color));
        mDrawPile.get(drawPile).setTag(R.string.COLOR_TAG, color);
    }

    public void updateHandDisplay(int color, int numberOfCards)
    {
        mHandButtons.get(color).setText(numberOfCards);
    }

    //returns quantity of cards in hand of that color
    public int getNumHandCardsFromColor(int color)
    {
        return Integer.parseInt(mHandButtons.get(color).getText().toString());
    }

    public Bitmap getCardImageFromColor(int color)
    {
        int id = R.drawable.carddeck;

        switch (color)
        {
            //Color Key: Grey:0,Red:1,Blue:2,Yellow:3,Green:4,Orange:5,Pink:6,Black:7,White:8 -->

            case 0:
                id = R.drawable.cardwild;
                break;
            case 1:
                id = R.drawable.cardred;
                break;
            case 2:
                id = R.drawable.cardblue;
                break;
            case 3:
                id = R.drawable.cardyellow;
                break;
            case 4:
                id = R.drawable.cardgreen;
                break;
            case 5:
                id = R.drawable.cardorange;
                break;
            case 6:
                id = R.drawable.cardpink;
                break;
            case 7:
                id = R.drawable.cardblack;
                break;
            case 8:
                id = R.drawable.cardwhite;
                break;
        }

        return BitmapFactory.decodeResource(getResources(), id);
    }

    public int getColorFromDrawPile(int drawPile)
    {
        return (int) mDrawPile.get(drawPile).getTag(R.string.COLOR_TAG);
    }

    public int getDrawPileIndexFromImageView(ImageView imageview)
    {
        return (int) imageview.getTag(R.string.PILE_TAG);
    }

    public void addTicketToDisplay(GameDestinationTicket ticket)
    {
        TextView textView = new TextView(this);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTicketClick(v);
            }
        });
        textView.setText(getTicketString(ticket));
        textView.setTag(ticket);
        mTicketScrollView.addView(textView);
    }

    public String getTicketString(GameDestinationTicket ticket)
    {
        return ticket.getSourceCity() + "<-->" + ticket.getDestinationCity() +
                " - " + ticket.getPointValue() + "points";
    }

    public Circle getCircleFromCityName(String cityname)
    {
        for (Circle c : mCircles)
        {
            CityCoordinates tag = (CityCoordinates) c.getTag();
            if(tag.mName == cityname)
            {
                return c;
            }
        }
        return null;
    }

    public void setAITurn(boolean isAITurn)
    {
        mIsAITurn = isAITurn;
    }

    public boolean isCardAndRouteSeletion(Pair<Integer,Integer> card, GameRouteConnection route)
    {
        if(!route.isPlayerControlled())
        {
            if (route.getRouteColor() == card.first ||
                    card.first == 0 || route.getRouteColor() == 0)
            {
                if(route.getTrainDistance() <= card.second)
                    return true;
            }
        }
        return false;
    }

    public void claimRoute(GamePlayer player, GameRouteConnection route)
    {
        route.setPlayerControlled(true);
        route.setPlayerID(player.getPlayerID());
        route.setPlayerColor(player.getPlayerColor());
        updatePolyline(route);
    }

    public void updatePolyline(GameRouteConnection route)
    {
        for(Polyline p : mPolylines)
        {
            if(((GameRouteConnection)p.getTag()).getConnectionID()
                    == route.getConnectionID())
            {
                p.setTag(route);
                p.setColor(ROUTE_COLORS[route.getPlayerColor()]);
                p.setPattern(null);
            }
        }
    }
}
