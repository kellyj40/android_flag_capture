package com.joekelly.mapsandlocation;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

// Activity for the public game
public class PublicMapActivity extends AppCompatActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    private LatLng userLocation;


    LocationManager locationManager;
    LocationListener locationListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getLocation();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Set the map
        mMap = googleMap;
        // Used for getting access to the systems location service
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);


        // Add listener for GPS movement
        locationListener = new LocationListener() {


            public void onLocationChanged(Location location) {

                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

                // Keep camera on the user
                mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));

                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("usersPlaying");

                // Telling GeoFire where we want to store it
                GeoFire geoFire = new GeoFire(ref);
                geoFire.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));

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
        // Once the map is ready put the location onto the map
        if (Build.VERSION.SDK_INT < 23) {
            Toast.makeText(PublicMapActivity.this, "UPdate", Toast.LENGTH_SHORT).show();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            mMap.setMyLocationEnabled(true);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        } else {
            Toast.makeText(PublicMapActivity.this, "Last Location", Toast.LENGTH_SHORT).show();
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != getPackageManager().PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            }
            //Use this for when opening the map
//            Location lastKnownLocation = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
//
//            LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            // Move camera to the location of the user
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 16));
            mMap.setMyLocationEnabled(true);
        }
        upDateUsers();

    }

    public void getLocation() {
        PrivateRequest getFlagsObject = new PrivateRequest();

        Intent intent = getIntent();
        Double startingLat = intent.getDoubleExtra("LAT", 0.0);
        Double startingLon = intent.getDoubleExtra("LON", 0.0);
//        Toast.makeText(this, "for real "+startingLat+" "+startingLon, Toast.LENGTH_LONG).show();

        userLocation = new LatLng(startingLat, startingLon);
    }


    private Marker userMarkerRef;
    public void upDateUsers(){
        String playerId = "ubd6f4rfl8aiPi8RzwahEjgTyBn2";

        DatabaseReference playerRef = FirebaseDatabase.getInstance().getReference("usersPlaying").child(playerId).child("l");

        playerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if (map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng playerLatLng = new LatLng(locationLat, locationLng);
                    if(userMarkerRef != null){
                        userMarkerRef.remove();
                    }
                    userMarkerRef = mMap.addMarker(new MarkerOptions().position(playerLatLng));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }



    protected void onPause() {
        super.onPause();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("usersPlaying");

        // Telling GeoFire where we want to store it
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);
    }

    protected void onStop() {
        super.onStop();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("usersPlaying");

        // Telling GeoFire where we want to store it
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);
        locationManager.removeUpdates(locationListener);
    }
}




