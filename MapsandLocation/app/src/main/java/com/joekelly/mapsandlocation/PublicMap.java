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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Activity for the public game
public class PublicMap extends AppCompatActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    private LatLng userLocation;
    private boolean hasFlag=false;

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
        // Get user location
        getLocation();
    }

    @Override // Called once the map is ready by google
    public void onMapReady(GoogleMap googleMap) {
        // Set the map
        mMap = googleMap;
        // Used for getting access to the systems location service
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Add listener for GPS movement - Check if collect flag, update to fireBase,
        locationListener = new LocationListener() {
            // Each time the user moves
            public void onLocationChanged(Location location) {
                //Get new location
                userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                // Update user on FireBase so other users can see
                updateUserLocationFirebase();
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
        // Add listener to all the users
        upDateUsers();
    }

    public void getLocation() {
        PrivateFlagRequest getFlagsObject = new PrivateFlagRequest();

        Intent intent = getIntent();
        Double startingLat = intent.getDoubleExtra("LAT", 0.0);
        Double startingLon = intent.getDoubleExtra("LON", 0.0);
//        Toast.makeText(this, "for real "+startingLat+" "+startingLon, Toast.LENGTH_LONG).show();

        userLocation = new LatLng(startingLat, startingLon);
        showToast(userLocation.toString());
    }


    private Marker userMarkerRef;
    public void upDateUsers(){


        //Get reference to database
        DatabaseReference playerRef = FirebaseDatabase.getInstance().getReference("usersPlaying").child("userIds");
        //Get snap shot of the database
        playerRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Get map of users in datasnapshot
                        collectIds((Map<String,Object>) dataSnapshot.getValue());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });

    }
    private void collectIds(Map<String, Object> users) {
        ArrayList<Object> userIds = new ArrayList<>();

        //iterate through each user, ignoring their UID
        for (Map.Entry<String, Object> entry : users.entrySet()) {

            //Get user map
            Map singleUser = (Map) entry.getValue();
            userIds.add(singleUser);
            //Get phone field and append to list
//            phoneNumbers.add((Boolean) singleUser.get("hasFlag"));
        }
        showToast(userIds.get(0).toString());
//        String playerId = "ubd6f4rfl8aiPi8RzwahEjgTyBn2";
//        DatabaseReference playerRef = FirebaseDatabase.getInstance().getReference("usersPlaying").child(playerId).child("l");
//
//        playerRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if(dataSnapshot.exists()){
//                    List<Object> map = (List<Object>) dataSnapshot.getValue();
//                    double locationLat = 0;
//                    double locationLng = 0;
//                    if (map.get(0) != null){
//                        locationLat = Double.parseDouble(map.get(0).toString());
//                        locationLng = Double.parseDouble(map.get(1).toString());
//                    }
//                    LatLng playerLatLng = new LatLng(locationLat, locationLng);
//                    if(userMarkerRef != null){
//                        userMarkerRef.remove();
//                    }
//                    userMarkerRef = mMap.addMarker(new MarkerOptions().position(playerLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.otherusers)));;
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
    }


    public void updateUserLocationFirebase(){
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("usersPlaying").child("userIds");

        // Telling GeoFire where we want to store it
        GeoFire geoFire = new GeoFire(ref);
        geoFire.setLocation(userId, new GeoLocation(userLocation.latitude, userLocation.longitude));
        ref.child(userId).child("hasFlag").setValue(hasFlag);
    }

    protected void onPause() {
        super.onPause();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("usersPlaying").child("userIds");
//
//        // Telling GeoFire where we want to store it
//        GeoFire geoFire = new GeoFire(ref);
//        geoFire.removeLocation(userId);
//        ref.child(userId).removeValue();
    }

    protected void onStart() {
        super.onStart();
    }

    protected void onStop() {
        super.onStop();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("usersPlaying").child("userIds");
//
//        // Telling GeoFire where we want to store it
//        GeoFire geoFire = new GeoFire(ref);
//        geoFire.removeLocation(userId);
//        ref.child(userId).removeValue();
//        locationManager.removeUpdates(locationListener);
    }

    public void showToast(String message) {
        Context context = getApplicationContext();
        CharSequence text = message;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}




