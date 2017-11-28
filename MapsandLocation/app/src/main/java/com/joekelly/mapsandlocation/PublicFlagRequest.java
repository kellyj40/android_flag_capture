package com.joekelly.mapsandlocation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*
 * Created by joekelly on 26/10/2017.
 * This class is used for the game flags logic
 * Queries the database, crowd sources flags and checks if flags were captured
 */

// Requests array of flags from database - for the public game
public class PublicFlagRequest {

    // Flag Queries
    private DatabaseReference mDataBase;
    // GeoFire requests to generate around users
    private GeoFire mGeoFire;
    // user Location variable
    private LatLng userLocation;
    // Google maps variable to plot with graphs
    private GoogleMap mMap;
    // Store the flags in a hash table of keys from database and geolocation
    private Map<String, GeoLocation> flagLocations = new HashMap<String, GeoLocation>();
    // Store the marker objects with key of database location.
    private Map<String, Marker> markerMap = new HashMap<String, Marker>();
    // Flag request used to generate flags
    private PrivateFlagRequest flagRequest = new PrivateFlagRequest();
    // Reference to the overlay of distance to walk
    private GroundOverlay distanceToWalkOverLay;

    // Constructor for map, and user location to plot the flags
    public PublicFlagRequest(LatLng userLocation, GoogleMap mMap){
        // Initialise variables
        mDataBase = FirebaseDatabase.getInstance().getReference();
        mGeoFire  = new GeoFire(mDataBase.child("flags"));
        this.userLocation = userLocation;
        this.mMap = mMap;
        // getFlags flags from database
        getFlags();
    }


    // --------------- This method is used to query database around the users location and make listeners -----------\\
    public void getFlags(){
        // Query reference
        GeoQuery geoQuery = mGeoFire.queryAtLocation(new GeoLocation(userLocation.latitude, userLocation.longitude),1);

        // Listeners for original request, new flags and removed flags
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            //First get all the data within the radius of user and add to the list
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                // Add to hashMap the key and location of flags in vicinity
                Log.i("New Key Entered", key);
                if (!flagLocations.containsValue(key)){
                    // Add to the hash table
                    flagLocations.put(key,location);
                    //Draw on the map
                    addToMap(key, new LatLng(location.latitude, location.longitude));
                }

            }

            @Override
            public void onKeyExited(String key) {
                // Remove the flag from the map of the deleted flag from database
                removeFromMap(key);
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            // Once all finished on the initial call of the flags, plot onto the map and add listener to each
            @Override
            public void onGeoQueryReady() {
                // If when first put down there is not enough flags then generate them
                if (flagLocations.size()<10){
                    // Generate more flags using PrivateFlagRequest
                    makeFlags();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    //------------ If there is less than 5 flags in vicinity of the user, generate more and push to database --------------\\
    public void makeFlags(){
        // Make flags around user using PrivateFlagRequest
        ArrayList<double[]> flags = flagRequest.requestFlags(userLocation);

        // ------- Push flags up to the fireBase ---------- \\
        // Reference database
        DatabaseReference flagRef = FirebaseDatabase.getInstance().getReference("flags");

        // Use Firebase for special requests
        GeoFire flagGeoFire = new GeoFire(flagRef);

        // Loop through all the flags to push up
        for(double[] flag:flags){
            // Push to generate hash key
            String ref1 = flagRef.push().getKey();
            // Using hash key, use geofire to child "l"
            flagGeoFire.setLocation(ref1.toString(), new GeoLocation(flag[0], flag[1]));
            // As it updates the firebase, it will notify the listener in which will then add to the map automatically
        }

    }

    // ---------------- When the fireBase is updated with new data put onto the map ---------------\\
    public void addToMap(String key, LatLng Location){
        // Update on the map the flags
        markerMap.put(key, mMap.addMarker(new MarkerOptions().position(Location).icon(BitmapDescriptorFactory.fromResource(R.drawable.mapicon))));
    }

    // -------------------- When a flag has been removed from the database, remove from map ------------------- \\
    public void removeFromMap(String key){
        // Update on the map the flags
        Marker ref = markerMap.get(key);
        // Remove from the map
        ref.remove();
        // Remove from marker hash table
        markerMap.remove(key);
        // Remove from flag hash table
        flagLocations.remove(key);
    }

    // ------------- Game Logic, check if captured flag, if so update the database ------------------ \\
    //      1. Remove flag from database(This will sync in listener to remove from map)
    //      2. Update user so that they appear different colour
    public boolean checkIfCapturedFlag(LatLng userLocation){

        String capturedFlag = DistanceCalculations.checkFlagDistancesPublic(userLocation, flagLocations);
        if (capturedFlag != null){
            // Reference the flag
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("flags");
            // Remove the value from the database
            ref.child(capturedFlag).removeValue();

            // Update DataBase user has a flag.
            // get User Id
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            // Get child
            DatabaseReference playerRef = FirebaseDatabase.getInstance().getReference("usersPlaying").child("userIds").child(userId);
            // Set value to be true
            playerRef.child("hasFlag").setValue(true);

            // Return true to prevent player from getting more flags
            return true;
        }
        return false;

    }

    //This method will draw 200 meter distance to walk if flag captured
    public void drawPerimeterDistanceToWalk(LatLng userLocationUpdate){
        // drawing circle
        int d = 400; // diameter
        Bitmap bm = Bitmap.createBitmap(d, d, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        Paint p = new Paint();
        p.setColor(Color.RED);

        c.drawCircle(d/2, d/2, d/2, p);

        // generate BitmapDescriptor from circle Bitmap
        BitmapDescriptor bmD = BitmapDescriptorFactory.fromBitmap(bm);
        int radiusM = 100;

        //Add the circle
        distanceToWalkOverLay = mMap.addGroundOverlay(new GroundOverlayOptions().
                image(bmD).
                position(userLocationUpdate,radiusM*2,radiusM*2).transparency(0.4f));
    }

    public void removePerimeterDistanceToWalk(){
        distanceToWalkOverLay.remove();
    }
    
}
