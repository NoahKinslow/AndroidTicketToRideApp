package tewesday.androidtickettorideapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.maps.android.ui.IconGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class GameActivity extends AppCompatActivity implements OnMapReadyCallback,
                                                                GoogleMap.OnPolylineClickListener,
                                                                GoogleMap.OnCameraMoveListener
{

    private GoogleMap mMap;
    private final int RADIUS = 80000;
    private final int HIGHLIGHT_RADIUS = 120000;
    private final int REQUEST_THUMBNAIL_IMAGE = 1;
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
    private List<LinearLayout> mPlayerLayouts;
    private List<TextView> mNameTextViews;
    private List<TextView> mPointsTextViews;
    private List<TextView> mTrainsTextViews;
    private List<ImageView> mImageViews;
    private LinearLayout mTicketLayout;
    //GameControl
    private FirebaseAuth mAuthentication;
    private List<GamePlayer> mGamePlayers;
    private GameLogicMaster mGameLogicMaster;
    private boolean mIsAIGame = false;
    private boolean mIsAITurn = false;
    private int mTimesTapped = 0;
    private final List<PatternItem> DOT_PATTERN =
            new ArrayList<PatternItem>(Arrays.asList(new Dot(), new Gap(5)));
    private List<Integer> mSelectedItems;
    private boolean isGameStarted = false;

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

    public void startGameClick(View view) {
        Button button = (Button) view;

        //TODO: do stuff

        if (mIsAIGame || mGameLogicMaster.startGame(mAuthentication.getCurrentUser().getUid()))
        {
            isGameStarted = true;
            button.setVisibility(View.GONE);
        }
    }

    public void ticketComplete(GameDestinationTicket ticket) {
        TextView textView = mTicketLayout.findViewWithTag(ticket);
        textView.setTextColor(Color.GREEN);
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

        if(getIntent().hasExtra("GAMELOGICMASTER"))
        {
            mIsAIGame = false;
            mGameLogicMaster = getIntent().getParcelableExtra("GAMELOGICMASTER");
            mRoutes = mGameLogicMaster.getGameBoardMap().getRoutes();
            mCityArray = mGameLogicMaster.getGameBoardMap().getCities();
            mAuthentication = FirebaseAuth.getInstance();
            mGameLogicMaster.setCurrentUserID(mAuthentication.getCurrentUser().getUid());
        }
        else
        {
            mIsAIGame = true;
            mGameLogicMaster = new GameLogicMaster();
            mGameLogicMaster.setGameBoardMap(getIntent().<GameBoardMap>getParcelableExtra("MAP"));
            mRoutes = getIntent().getParcelableArrayListExtra("ROUTE");
            mCityArray = getIntent().getStringArrayListExtra("CITY");
            mGameLogicMaster.setDestinationTickets(getIntent()
                    .<GameDestinationTicket>getParcelableArrayListExtra("TICKET"));
            mGameLogicMaster.setupTrainDeck();
            mGameLogicMaster.setupDrawPiles();
            mGameLogicMaster.setupPlayers(true);
        }
        mMarkers = new ArrayList<>();
        mPolylines = new ArrayList<>();
        mCircles = new ArrayList<>();

        mGameLogicMaster.setGameActivity(this);
        initializeLayoutItems();
        initializeDrawPiles();
        initializePlayerList();
        initializeHand();
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

        mTicketLayout = findViewById(R.id.ticketListView);

        mImageViews = new ArrayList<>();
        mNameTextViews = new ArrayList<>();
        mPointsTextViews = new ArrayList<>();
        mTrainsTextViews = new ArrayList<>();
        mPlayerLayouts = new ArrayList<>();

        for (int i = 0; i < 5; i++)
        {
            //http://daniel-codes.blogspot.com/2009/12/dynamically-retrieving-resources-in.html
            Resources r = getResources();
            String packageName = getPackageName();
            int playerImage = r.getIdentifier("PlayerImage" + String.valueOf(i), "id", packageName);
            int playerName = r.getIdentifier("PlayerName" + String.valueOf(i), "id", packageName);
            int playerPoints = r.getIdentifier("PlayerPoints" + String.valueOf(i), "id", packageName);
            int playerTrains = r.getIdentifier("PlayerTrains" + String.valueOf(i), "id", packageName);
            int playerLayout = r.getIdentifier("PlayerLayout" + String.valueOf(i), "id", packageName);
            mImageViews.add((ImageView)findViewById(playerImage));
            mNameTextViews.add((TextView)findViewById(playerName));
            mPointsTextViews.add((TextView)findViewById(playerPoints));
            mTrainsTextViews.add((TextView)findViewById(playerTrains));
            mPlayerLayouts.add((LinearLayout)findViewById(playerLayout));
        }
    }

    private void initializeDrawPiles()
    {
        for (int i = 0; i < 6; i++)
            updateDrawPile(i, mGameLogicMaster.getDrawPileCardColor(i));
    }

    private void initializeHand()
    {
        for (int i = 0; i < 9; i++)
            updateHandDisplay(i, mGameLogicMaster.getCardNumber(i));
    }

    private void initializePlayerList()
    {
        mGamePlayers = mGameLogicMaster.getGamePlayers();

        int i = 0;
        for (GamePlayer p : mGamePlayers) {
            colorPlayerName(i, p.getPlayerColor());
            updatePlayerName(i, p.getPlayerName());
            updatePlayerPoints(i, p.getScore());
            updatePlayerTrains(i, p.getTrainsLeft());
            makePlayerVisible(i);
            i++;
        }

    }
    public void gameOver(boolean iWon) {
        if(iWon)
        {
            Toast.makeText(this, "Congratulations! You are the winner with " +
                    mGameLogicMaster.getPlayer(0).getScore() +
                    " points!", Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(this, "Sorry you lost with " +
                    mGameLogicMaster.getPlayer(0).getScore() +
                    " points!", Toast.LENGTH_LONG).show();
        }
        isGameStarted = false;
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

            Circle circle = mMap.addCircle(cir);
            circle.setTag(city);
            mCircles.add(circle);
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

            LatLng sourceLoc = null;
            LatLng desLoc = null;
            if (source != null && des != null) {
                sourceLoc = source.mLocation;
                desLoc = des.mLocation;
            }
            else {
                Log.d("CITY ERROR:", sourceStr + "-" + desStr);
                continue;
            }

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
                    .width(35)
                    .zIndex(100)
                    .clickable(true);

            if (route.isPlayerControlled())
            {
                poly.color(ROUTE_COLORS[route.getPlayerColor()]);
            }
            else
            {
                poly.color(ROUTE_COLORS[route.getRouteColor()]);
                poly.pattern(DOT_PATTERN);
            }

            Polyline polyline = mMap.addPolyline(poly);
            polyline.setTag(route);
            mPolylines.add(polyline);
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
            if (Geocoder.isPresent()) {
                cities = new ArrayList<>();
                Geocoder geo = new Geocoder(this);
                for (String cityName : mCityArray) {
                    List<Address> addresses = null;
                    try {
                        //gives some human assistance ;)
                        if (Objects.equals(cityName, "Washington"))
                            addresses = geo.getFromLocationName(cityName + " DC", 1);
                        else if (Objects.equals(cityName, "Vancouver"))
                            addresses = geo.getFromLocationName(cityName + " Canada", 1);
                        else if (Objects.equals(cityName, "Helena"))
                            addresses = geo.getFromLocationName(cityName + " Montana", 1);
                        else if (Objects.equals(cityName, "Duluth"))
                            addresses = geo.getFromLocationName(cityName + " Minnesota", 1);
                        else
                            addresses = geo.getFromLocationName(cityName, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d("CITY FAIL:", cityName);
                    }
                    if (addresses != null && !addresses.isEmpty() &&
                            addresses.get(0).hasLongitude() &&
                            addresses.get(0).hasLatitude()) {
                        Address city = addresses.get(0);
                        double lat = city.getLatitude();
                        double lng = city.getLongitude();
                        CityCoordinates cityCo = new CityCoordinates(cityName, new LatLng(lat, lng));
                        if(!cities.contains(cityCo))
                        cities.add(cityCo);
                    }
                }
            }
        }while(!Geocoder.isPresent());
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
        if (!isGameStarted) {
            Toast.makeText(this, "Press Start Game", Toast.LENGTH_SHORT);
            return;
        }
        if (mTimesTapped > 0) {
            Toast.makeText(this, "Cannot place route same turn as card draw. Draw another card.", Toast.LENGTH_SHORT);
            return;
        }
        if (mSelectedCard == null) {
            Toast.makeText(this, "Please select a card first", Toast.LENGTH_SHORT);
            return;
        }

        GameRouteConnection route = (GameRouteConnection) polyline.getTag();
        if(isCardAndRouteSeletion(mSelectedCard,route)
                && mGameLogicMaster.PlaceTrains(route))
        {
            if(mGameLogicMaster.PlaceTrains(route))
            claimRoute(mGamePlayers.get(0), route);
            updatePolyline(polyline, route);
            updateHandDisplay(mSelectedCard.first, mSelectedCard.second - route.getTrainDistance());

            Toast.makeText(this, "Route Claimed!", Toast.LENGTH_SHORT).show();
            mGameLogicMaster.AITurn();
        }
        else
        {
            Toast.makeText(this, "Invalid Move", Toast.LENGTH_SHORT).show();
        }
    }

    public void drawPileClick(View view)
    {
        if(!isGameStarted || mTimesTapped > 1)
            return;

        int pileNum = getDrawPileIndexFromImageView((ImageView)view);
        int color = getColorFromDrawPile(pileNum);

        if(color == 0 && mTimesTapped > 0 && pileNum != 0)
            return;

        if(color == 0)
            mTimesTapped += 2;
        else
            mTimesTapped++;

        //add card to hand display
        updateHandDisplay(color, getNumHandCardsFromColor(color) + 1);

        mGameLogicMaster.drawCard(0, pileNum);

        int newCard = mGameLogicMaster.getDrawPileCardColor(pileNum);
        updateDrawPile(pileNum, newCard);

        //See if turn is over
        if(mTimesTapped > 1)
        {
            //end turn
            mTimesTapped = 0;
            if (mIsAIGame) {
                setAITurn(true);
                mGameLogicMaster.AITurn();
            }
        }
    }

    public void handCardClick(View view)
    {
        if(isGameStarted) {
            Button button = (Button) view;
            int color = (int) button.getTag(R.string.COLOR_TAG);
            int number = Integer.parseInt(button.getText().toString());
            mSelectedCard = new Pair<>(color, number);
        }
    }

    public void addTicketClick(View view)
    {
        ScrollView ticketsv = findViewById(R.id.ticketScrollView);
        ticketsv.setVisibility(View.GONE);
        mTicketLayout.setVisibility(View.GONE);

        if(!isGameStarted || mTimesTapped > 0)
            return;

        mTimesTapped = 2;
        //https://developer.android.com/guide/topics/ui/dialogs.html
        //https://stackoverflow.com/questions/10714911/alertdialogs-items-not-displayed
        //https://stackoverflow.com/questions/3032342/arrayliststring-to-charsequence
        mSelectedItems = new ArrayList<>();
        final List<GameDestinationTicket> proposedTickets = mGameLogicMaster.getProposedTickets();
        List<String> dialogTicketItems = new ArrayList<>();
        for (GameDestinationTicket ticket : proposedTickets) {
            dialogTicketItems.add(getTicketString(ticket));
        }
        CharSequence[] charseq = dialogTicketItems.toArray(new CharSequence[dialogTicketItems.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.ticketDialogMessage);
        builder.setMultiChoiceItems(charseq, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked) {
                    mSelectedItems.add(which);
                } else if (mSelectedItems.contains(which)) {
                    mSelectedItems.remove(Integer.valueOf(which));
                }
            }
        });

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                List<GameDestinationTicket> selectedTickets = new ArrayList<>();
                for (int i : mSelectedItems)
                {
                    selectedTickets.add(proposedTickets.get(i));
                    addTicketToDisplay(proposedTickets.get(i));
                }
                mGameLogicMaster.selectedTicket(selectedTickets);
                mTimesTapped = 0;
                mGameLogicMaster.AITurn();
            }
        });

        builder.setPositiveButton(R.string.okay, null);
        final AlertDialog dialog = builder.create();
        //https://stackoverflow.com/questions/2620444/how-to-prevent-a-dialog-from-closing-when-a-button-is-clicked
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(mSelectedItems.size() > 1)
                            dialog.dismiss();
                        else
                            Toast.makeText(getApplicationContext(),"Choose Two Tickets",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        dialog.show();
    }

    public void onTicketClick(View view)
    {
        if(isGameStarted) {
            GameDestinationTicket ticket = (GameDestinationTicket) view.getTag();
            Circle source = getCircleFromCityName(ticket.getSourceCity());
            Circle des = getCircleFromCityName(ticket.getDestinationCity());
            if (source == null || des == null)
                return;

            if (source.getRadius() == RADIUS || des.getRadius() == RADIUS) {
                source.setRadius(HIGHLIGHT_RADIUS);
                des.setRadius(HIGHLIGHT_RADIUS);
                int color = (int) view.getTag(R.string.COLOR_TAG);
                source.setStrokeColor(color);
                des.setStrokeColor(color);
                source.setFillColor(color);
                des.setFillColor(color);
            } else {
                source.setRadius(RADIUS);
                des.setRadius(RADIUS);
                source.setStrokeColor(Color.BLACK);
                des.setStrokeColor(Color.BLACK);
                source.setFillColor(Color.BLACK);
                des.setFillColor(Color.BLACK);
            }
        }
    }

    public void myTicketsClick(View view) {
        if(!isGameStarted)
            return;

        ScrollView ticketsv = findViewById(R.id.ticketScrollView);
        if(ticketsv.getVisibility() == View.GONE) {
            ticketsv.setVisibility(View.VISIBLE);
            mTicketLayout.setVisibility(View.VISIBLE);
        }
        else {
            ticketsv.setVisibility(View.GONE);
            mTicketLayout.setVisibility(View.GONE);
        }
    }

    public void takeUserPhoto(View view) {
        //Taken from the wonderful:
        //https://github.com/thale4/TakingPhotosSimply/
        dispatchTakeThumbnailIntent();
    }

    private void dispatchTakeThumbnailIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_THUMBNAIL_IMAGE);
        }
    }

    // Get thumbnail for image and display to ImageView
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_THUMBNAIL_IMAGE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            updatePlayerImage(0,imageBitmap);
        }
    }


    //UI updaters and getters

    public void makePlayerVisible(int player)
    {
        mPlayerLayouts.get(player).setVisibility(View.VISIBLE);
    }

    public void updatePlayerName(int player, String name)
    {
        mNameTextViews.get(player).setText(name);
    }

    public void updatePlayerPoints(int player, int points)
    {
        mPointsTextViews.get(player).setText(getString(R.string.points, points));
    }

    public void updatePlayerTrains(int player, int trains)
    {
        mTrainsTextViews.get(player).setText(getString(R.string.trainsLeft, trains));
    }

    public void colorPlayerName(int player, int playercolor)
    {
        mNameTextViews.get(player).setTextColor(ROUTE_COLORS[playercolor]);
    }

    public void updatePlayerImage(int player, Bitmap image)
    {
        mImageViews.get(player).setImageBitmap(image);
    }

    public void updateDrawPile(int drawPile, int color)
    {
        if(drawPile != 0)
            mDrawPile.get(drawPile).setImageBitmap(getCardImageFromColor(color));
        mDrawPile.get(drawPile).setTag(R.string.PILE_TAG, drawPile);
        mDrawPile.get(drawPile).setTag(R.string.COLOR_TAG, color);
    }

    public void updateHandDisplay(int color, int numberOfCards)
    {
        mHandButtons.get(color).setText(String.valueOf(numberOfCards));
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
        int color = createTicketColor();
        TextView textView = new TextView(this);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTicketClick(v);
            }
        });
        textView.setText(getTicketString(ticket));
        textView.setTextColor(color);
        textView.setTag(ticket);
        textView.setTag(R.string.COLOR_TAG, color);
        mTicketLayout.addView(textView);
    }

    private final List<Integer> TICKET_COLORS = Arrays.asList
            (
                    Color.BLUE,
                    Color.YELLOW,
                    0xB87333,
                    0xCC329,
                    0x855E42,
                    Color.CYAN,
                    Color.GRAY,
                    0xF58231,
                    Color.RED,
                    0x000080,
                    0xCFB53B,
                    Color.MAGENTA,
                    Color.GRAY,
                    0x008080
            );
    private int createTicketColor()
    {
        Collections.shuffle(TICKET_COLORS);
        return TICKET_COLORS.get(0);
    }

    public String getTicketString(GameDestinationTicket ticket)
    {
        return ticket.getSourceCity() + " <> " + ticket.getDestinationCity() +
                " - " + ticket.getPointValue() + " points";
    }

    public Circle getCircleFromCityName(String cityname)
    {
        for (Circle c : mCircles)
        {
            CityCoordinates tag = (CityCoordinates) c.getTag();
            if(tag.mName.equals(cityname))
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
    public void updatePolyline(Polyline p, GameRouteConnection route)
    {
        p.setTag(route);
        p.setColor(ROUTE_COLORS[route.getPlayerColor()]);
        p.setPattern(null);
    }

    public void updatePolyline(GameRouteConnection route)
    {
        for(Polyline p : mPolylines)
        {
            if(((GameRouteConnection)p.getTag()).getConnectionID()
                    == route.getConnectionID())
            {
                updatePolyline(p,route);
                break;
            }
        }
    }
}
