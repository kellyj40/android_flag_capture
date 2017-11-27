package com.joekelly.mapsandlocation;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

// Activity for the private game
public class PrivateMap extends AppCompatActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    private int flagsCaptured;
    private Vibrator vib;

    // Step instance
    private int numSteps;
    private SensorObject stepObject;
    private TextView StepsTaken;

    //stats db
    private ArrayList <double[]> arrFlags; // Coordinates of all flags on map
    private FlagsOnMap flagsOnMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private PrivateFlagRequest getFlagLocation = new PrivateFlagRequest();

    private Databasehelperclass myDb;
    private DataBaseManagement flagsDb;
    //private DataBaseManagement referenceDataBase;
    private LatLng userLocation;
    private String message;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Set up database
        //createDatabaseTable();
        // Get location of user
        getLocation();
        // Set up sensor technology
        setUpSensors();

        // Make new flags that will be used on the map
        newFlags();

        // Set up stats page db
        myDb = new Databasehelperclass(this);
        flagsDb = new DataBaseManagement(this);

        //Initialise vibration
        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        //Initialise flags
        flagsCaptured = flagsDb.todaysFlags();
        // Update text with number of flags captured from other games
        TextView textView = (TextView) findViewById(R.id.distance);
        textView.setText("Todays Flags: " + Integer.toString(flagsCaptured));


    }


    public void getLocation() {
        // getting location
        Intent intent = getIntent();
        // Lat and lngs pasted in from the homepage
        Double startingLat = intent.getDoubleExtra("LAT", 0.0);
        Double startingLon = intent.getDoubleExtra("LON", 0.0);
        // Set to the global value
        userLocation = new LatLng(startingLat, startingLon);
        showToast(userLocation.toString());
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Set the map
        mMap = googleMap;

        // Used for getting access to the systems location service
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Set up Marker Object for a given map
        flagsOnMap = new FlagsOnMap(mMap);

        // Place markers onto the map
        flagsOnMap.drawFlagsToMap(arrFlags);

            // Add listener for GPS movement
        locationListener = new LocationListener() {

                // On location change Listener - Called each time the user moves
                // Updates user location and checks if near flags
                public void onLocationChanged(Location location) {
                    //Get the users new location and update variable
                    userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    // Call method to see if in range to collect flag
                    checkIfCapturedFlag();
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }
                @Override
                public void onProviderEnabled(String s) {

                }
                @Override
                public void onProviderDisabled(String s) {

                }
        };


        // Must be after the location listener is made
        if (Build.VERSION.SDK_INT < 23) {
            // check for permision, and then start requesting location updates. Otherwise, request permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                mMap.setMyLocationEnabled(true);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},8034);
            }

        } else {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != getPackageManager().PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
            // Move camera to the location of the user
            mMap.setMyLocationEnabled(true);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
        }



    }

    public void checkIfCapturedFlag(){
        // Calculate the distance to all the flags, if within the distance return location of the index, -1 if not in radius
        int value = DistanceCalculations.checkFlagDistances(userLocation, arrFlags);

        // if with in distance, value will be >= to zero if the index
        if (value>=0) {

            vib.vibrate(500);
            // Remove the flag from the map
            flagsOnMap.removeFlagFromMap(value);
            // Increment number of flags collected by the user

            // Update the local database
            flagsDb.updateLocalFlagTable();
            // Make toast that flag was captured
            showToast("Flag captured :) ");
            flagsCaptured = flagsDb.todaysFlags();

            // Up date the text view of the number of captured flags
            TextView textView = (TextView) findViewById(R.id.distance);
            textView.setText("Todays Flags: " + Integer.toString(flagsCaptured));

            // If there is no more flags make new ones
            if (flagsOnMap.numberOfFlagsRemaining<=0){
                newFlags();
                //Put the new flags onto the map
                flagsOnMap.drawFlagsToMap(arrFlags);
            }
        }
    }


    public void setUpSensors(){
        StepsTaken = (TextView) findViewById(R.id.tv_steps);
        stepObject = new SensorObject();
        numSteps= stepObject.numSteps;
        stepObject.passTextView(StepsTaken);
        message = getResources().getString(R.string.steps_walked);
        stepObject.initialiseStepSensor(this, message, StepsTaken, 0);

    }

    public void newFlags(){
        // Make a flagRequest for new flags from the users location
        arrFlags = getFlagLocation.requestFlags(userLocation);
    }

    public void showToast(String message) {
        // All Toast messages call this method
        Context context = getApplicationContext();
        CharSequence text = message;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();


        // ----------CHANGE BACK TO PRIVATE MENU WHEN EVERYONE HAS LOGGED OUT---------
//        inflater.inflate(R.menu.private_menu, menu);
        inflater.inflate(R.menu.public_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.statsMenu:
                // passes steps to stats page
                Intent registerIntent = new Intent(this, StatsActivity.class);
                int x = stepObject.numSteps;
                // adds current steps to db
                // myDb.addSteps(new Steps(x));
                registerIntent.putExtra("numSteps", x);
                startActivity(registerIntent);
//                finish();
                return true;
            case R.id.leaderboard:
                startActivity(new Intent(this, Abouter.class));
                return true;



                // ------REMOVE ONCE EVERYONE HAS LOGGED OUT-----
            case R.id.logout:
                // set logout to true, then destroy the instance.
                // user will be logged out onDestroy()
                logout = true;
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    protected void onStop() {
        super.onStop();
        myDb.addSteps(new Steps(stepObject.numSteps));
    }
    protected void onStart() {super.onStart();}
    protected void onPause() {super.onPause();}
    protected void onResume() {super.onResume();}







// -------REMOVE ONCE EVERYONE HAS LOGGED OUT
    boolean logout = false;


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (logout) {
            FirebaseAuth.getInstance().signOut();
        }
    }

}