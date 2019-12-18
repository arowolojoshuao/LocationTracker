package com.ecosa.mapdirections;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.ecosa.mapdirections.interfaces.ConstantInterface;
import com.ecosa.mapdirections.util.AppUtility;
import com.ecosa.mapdirections.util.RunTimePermission;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import database.Coordinate;
import database.RoomRepository;

public class MainActivity extends AppCompatActivity implements ConstantInterface, OnMapReadyCallback, DirectionCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener{

    private SupportMapFragment mapFragment;
    private GoogleMap googleMap;
   // private TextView textAddress;
    private LatLng currentLatLng;


    private double startingLatitude;
    private double startingLongitude;

    private double stoppingLatitude;
    private double stoppingLongitude;

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private AppUtility appUtility;
    private RunTimePermission runTimePermission;

    private Double[] PositionA = new Double[2];
    private Double[] PositionB = new Double[2];

    @Bind(R.id.llmMapContainer)
    LinearLayout llmMapContainer;

    @Bind(R.id.llWatchContainer)
    LinearLayout llWatchContainer;

    @Bind(R.id.textAddress)
    TextView textAddress;

    @Bind(R.id.btnStart)
    Button btnStart;

    @Bind(R.id.btnStop)
    Button btnStop;


    @Bind(R.id.btnCalDistance)
    Button btnCalDistance;

    public static RoomRepository roomRepository;


    private static final String TAG = "MapsActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
       // roomRepository = new RoomRepository(getApplication());
        btnStop.setEnabled(false);
        btnCalDistance.setEnabled(false);
        initControls();




    }

    @Override
    public void onStop() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    private void initControls() {

        appUtility = new AppUtility(this);
        runTimePermission = new RunTimePermission(this);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        //textAddress = (TextView) findViewById(R.id.textAddress);
        textAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // This is to prevent user to click on the map under the distance text.
            }
        });
        if (appUtility.checkPlayServices()) {

            googleApiClient = new GoogleApiClient
                    .Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            googleApiClient.connect();
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));
        }

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                googleMap.clear();
                googleMap.addMarker(new MarkerOptions().position(latLng));

                GoogleDirection.withServerKey(getString(R.string.map_direction_key))
                        .from(currentLatLng)
                        .to(new LatLng(latLng.latitude, latLng.longitude))
                        .transportMode(TransportMode.DRIVING)
                        .execute(MainActivity.this);

                showDistance(latLng);

            }
        });
    }

    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {
        if (direction.isOK()) {
            ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
            googleMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 1, ContextCompat.getColor(this, R.color.colorPrimary)));
        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {

    }

    private void showDistance(LatLng latLng) {
        Location locationA = new Location("Location A");
        locationA.setLongitude(latLng.longitude);

        Location locationB = new Location("Location B");
        locationB.setLatitude(currentLatLng.latitude);
        locationB.setLongitude(currentLatLng.longitude);
        textAddress.setText("The Distance between Point A and Point B is : " + new DecimalFormat("##.##").format(locationA.distanceTo(locationB)) + "m");
    }

    // checking Runtime permission
    private void getPermissions(String[] strings) {

        runTimePermission.requestPermission(strings, new RunTimePermission.RunTimePermissionListener() {

            @Override
            public void permissionGranted() {
                locationChecker(googleApiClient, MainActivity.this);
            }

            @Override
            public void permissionDenied() {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    // Checking whether location service is enable or not.
    public void locationChecker(GoogleApiClient mGoogleApiClient, final Activity activity) {

        // Creating location request object
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setSmallestDisplacement(DISPLACEMENT);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        getLocation();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(activity, 1000);
                        } catch (IntentSender.SendIntentException e) {
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                        break;
                }
            }
        });
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (googleMap == null) {
            mapFragment.getMapAsync(this);
        }
    }



    @OnClick({R.id.btnStart, R.id.btnStop, R.id.btnCalDistance})
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btnStart:
                Toast.makeText(getApplicationContext(), "Start Button Clicked", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onClick: start button clicked... attempting to save to DB");
                btnStop.setEnabled(true);
                btnCalDistance.setEnabled(false);
                btnStart.setEnabled(false);

                currentLatLng = new LatLng(currentLatLng.latitude, currentLatLng.longitude);
                 startingLatitude = currentLatLng.latitude;
                 startingLongitude = currentLatLng.longitude;
              //  saveCoordinateToDatabase(1, "Point A", startingLatitude, startingLongitude);

                textAddress.setText(String.format("Your Start Location is %s", startingLatitude));
                Toast.makeText(getApplicationContext(), "Starting Location :"+ startingLatitude+"\n"
                        +"Starting Longitude is :"+ startingLongitude,  Toast.LENGTH_LONG).show();

                return;



            case R.id.btnStop:

                currentLatLng = new LatLng(currentLatLng.latitude, currentLatLng.longitude);
                   stoppingLatitude = currentLatLng.latitude;
                   stoppingLongitude = currentLatLng.longitude;

              //  saveCoordinateToDatabase(2, "Point B", startingLatitude, startingLongitude);

                textAddress.setText(String.format("Your Stopping Location is %s", stoppingLatitude));
                Toast.makeText(getApplicationContext(), "stopping Location :"+ stoppingLatitude+"\n"
                        +"Starting Longitude is :"+ stoppingLongitude,  Toast.LENGTH_LONG).show();



                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
                btnCalDistance.setEnabled(true);

                currentLatLng = new LatLng(currentLatLng.latitude, currentLatLng.longitude);
                textAddress.setText(String.format("Your  Stop Location is %s", currentLatLng.latitude));
                Toast.makeText(getApplicationContext(), "Stopping Location :"+ currentLatLng.latitude+"\n"
                        +"Starting Longitude is :"+ currentLatLng.longitude,  Toast.LENGTH_LONG).show();
                return;




            case R.id.btnCalDistance:

                //getCoordinatesFromDatabase();
                LatLng origin = new LatLng(startingLatitude, startingLongitude);
                LatLng destination = new LatLng(stoppingLatitude, stoppingLongitude);

//                LatLng origin = new LatLng(PositionA[0], PositionA[1]);
//                LatLng destination = new LatLng(PositionB[0], PositionB[1]);



                Location locationA = new Location("point A");
                locationA.setLatitude(origin.latitude);
                locationA.setLongitude(origin.longitude);

                Location locationB = new Location("point B");
                locationB.setLatitude(destination.latitude);
                locationB.setLongitude(destination.longitude);


                GoogleDirection.withServerKey(getString(R.string.map_direction_key))
                        .from(origin)
                        .to(destination)
                        .transportMode(TransportMode.DRIVING)
                        .execute(MainActivity.this);

                textAddress.setText("The Distance between Point A and Point B is : " + new DecimalFormat("##.##").format(locationA.distanceTo(locationB)) + "m");


                break;
            default:
                return;
        }
    }


    private void saveCoordinateToDatabase(int id, String position, double latitude, double longitude){

        Coordinate coordinate = new Coordinate(id, position, longitude, latitude);
        roomRepository.addCoordinate(coordinate);
        Toast.makeText(MainActivity.this, "coordinate successfully saved to db", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Offline Coordinates: id: "+id
                +"position: "+position
                +"longitude: "+longitude
                +"latitude: "+latitude);

    }


    /**
     * Method used to retrieve coordinates from RoomDb
     */
    private void getCoordinatesFromDatabase(){

        List<Coordinate> coordinates = roomRepository.getAllCoordinates();

        for (Coordinate coordinate : coordinates){

            int id = coordinate.getId();
            String position = coordinate.getPosition();
            double longitude = coordinate.getLongitude();
            double latitude = coordinate.getLatitude();

            if (id == 1){
                PositionA[0] = latitude;
                PositionA[1] = longitude;
                Log.d(TAG, "getCoordinatesFromDatabase:, latitude: "+latitude+" longitude: "+longitude+" for position "+position);
            }else if (id == 2){
                PositionB[0] = latitude;
                PositionB[1] = longitude;
                Log.d(TAG, "getCoordinatesFromDatabase: PositionB, latitude: "+latitude+" longitude: "+longitude+" for position "+position);
            }
        }

}}