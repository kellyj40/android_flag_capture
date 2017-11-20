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
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// Activity for the public game
public class PublicMap extends AppCompatActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    private LatLng userLocation;
    private boolean hasFlag=false;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_map);
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
        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

    }

    @Override // Called once the map is ready by google
    public void onMapReady(GoogleMap googleMap) {
        // Set the map
        mMap = googleMap;
        userManager = new UserManager(mMap);
        userManager.setUserLocation(userLocation);

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
                // updateUserLocationFirebase();
                userManager.setUserLocation(userLocation);

                //Check if captured flag
                flagRequest.checkIfCapturedFlag(userLocation);
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
//        Toast.makeText(this, "for real "+startingLat+" "+startingLon, Toast.LENGTH_LONG).show();

        userLocation = new LatLng(startingLat, startingLon);
    }


    protected void onPause() {
        super.onPause();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    protected void onStart() {
        super.onStart();
    }

    protected void onStop() {
        super.onStop();

        userManager.removeUserFromPlaying();
        locationManager.removeUpdates(locationListener);
        myDb.addSteps(new Steps(stepObject.numSteps));
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
        stepObject.initialiseStepSensor(this, StepsTaken);
    }

}




