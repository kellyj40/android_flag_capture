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
import android.util.Log;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

// Activity for the public game
public class PublicMap extends AppCompatActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    private LatLng userLocation;
    private UserManager userManager;

    LocationManager locationManager;
    LocationListener locationListener;

    private Vibrator vib;

    // Step instance
    private int numSteps;
    private SensorObject stepObject;
    private TextView StepsTaken;

    //Step database
    private Databasehelperclass myDb;

    //Flag instance
    PublicFlagRequest flagRequest;

    // Login/logout preference - allows us to check whether the user wants to be logged out on destroy
    private boolean logout = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Get user location
        getLocation();

        // Get reference for playing users
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("usersPlaying").child("userIds");

        // Set up listener for new players to add to map
        ChildEventListener childEventListener = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (!userManager.checkIfPlayerExists(dataSnapshot.getKey())){
                    userManager.addPlayerToHashMap(dataSnapshot.getKey().toString());
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {


            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.i("Child removed", dataSnapshot.getKey().toString());
                if (!userManager.checkIfPlayerExists(dataSnapshot.getKey())) {
                      userManager.removePlayerFromHashMap(dataSnapshot.getKey().toString());
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        ref.addChildEventListener(childEventListener);

        // Set up stats page db
        myDb = new Databasehelperclass(this);

        // Set up sensor technology
        setUpSensors();

        // Initialise vibration
        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE); }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
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
            case R.id.logout:
                // set logout to true, then destroy the instance.
                // user will be logged out onDestroy()
                logout = true;
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }



    }


    @Override // Called once the map is ready by google
    public void onMapReady(GoogleMap googleMap) {
        // Set the map
        mMap = googleMap;
        // Set up user and sync to the database
        userManager = new UserManager(mMap);
        userManager.setUserLocation(userLocation);
        userManager.setHasFlag(false);

        // Used for getting access to the systems location service
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = createLocationListener();

        // Must be after the location listener is made
        // Ask for permissions and zoom in on the user
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

        // Set up flags
        flagRequest = new PublicFlagRequest(userLocation, mMap);

    }




    public LocationListener createLocationListener() {
        // Add listener for GPS movement - Check if collect flag, update to fireBase,
        LocationListener newLocationListener = new LocationListener() {
            // Each time the user moves
            public void onLocationChanged(Location location) {
                //Get new location
                userLocation = new LatLng(location.getLatitude(), location.getLongitude());

                // Update user on FireBase so other users can see
                userManager.setUserLocation(userLocation);

                //Check to make sure doest have flag
                if (!userManager.getHasFlag()){

                    // If user doesnt have a flag check if captured flag
                    boolean checker = flagRequest.checkIfCapturedFlag(userLocation);
                    // If the user can collect flag, set the userManager object to have flag
                    if(checker){
                        showToast("Walk outside boundry to keep flag !");
                        vib.vibrate(500);
                        // Update object to having flag
                        userManager.setHasFlag(true);

                        // Draw radius for user to walk
                        flagRequest.drawPerimeterDistanceToWalk(userLocation);

                    }

                }else{
                    // otherwise check if walked 200 meters with flag.
                    if (DistanceCalculations.checkedWalkedDistance(userLocation)){
                        showToast("Flag collected :)");
                        vib.vibrate(1000);
                        // If walked 200m with flag, allow to capture flags again
                        userManager.setHasFlag(false);

                        // Remove radius for user to walk
                        flagRequest.removePerimeterDistanceToWalk();

                        // Update database score
                        userManager.capturedFlagUpdate();


                    }
                }


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

        return newLocationListener;
    }


    public void getLocation() {
        PrivateFlagRequest getFlagsObject = new PrivateFlagRequest();

        Intent intent = getIntent();
        Double startingLat = intent.getDoubleExtra("LAT", 0.0);
        Double startingLon = intent.getDoubleExtra("LON", 0.0);

        userLocation = new LatLng(startingLat, startingLon);
    }


    protected void onPause() {
        super.onPause();
    }

    protected void onStart() {
        super.onStart();
    }

    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userManager.removeUserFromPlaying();
        locationManager.removeUpdates(locationListener);
        myDb.addSteps(new Steps(stepObject.numSteps));
        if (logout) {
            FirebaseAuth.getInstance().signOut();
        }
    }

    public void showToast(String message) {
        Context context = getApplicationContext();
        CharSequence text = message;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public void setUpSensors(){
        StepsTaken = (TextView) findViewById(R.id.tv_steps);
        stepObject = new SensorObject();
        numSteps= stepObject.numSteps;
        stepObject.passTextView(StepsTaken);
        String message = getResources().getString(R.string.steps_walked);

        stepObject.initialiseStepSensor(this, message, StepsTaken, 0);
    }


}




