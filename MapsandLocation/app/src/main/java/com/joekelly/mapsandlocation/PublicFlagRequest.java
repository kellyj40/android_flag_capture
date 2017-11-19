package com.joekelly.mapsandlocation;

import android.os.AsyncTask;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;


/**
 * Created by joekelly on 26/10/2017.
 */

// Requests array of flags from database - for the public game
public class PublicFlagRequest {
    //Flag Queries
    private DatabaseReference mDataBase;
    private GeoFire mGeoFire;
    private LatLng userLocation;
    private GoogleMap mMap;
    private Map<String, GeoLocation> flagMap = new HashMap<String, GeoLocation>();
    private Map<String, Marker> markerMap = new HashMap<String, Marker>();
    private PrivateFlagRequest flagRequest = new PrivateFlagRequest();


    public PublicFlagRequest(LatLng userLocation, GoogleMap mMap){
        //getFlags
        mDataBase = FirebaseDatabase.getInstance().getReference();
        mGeoFire  = new GeoFire(mDataBase.child("flags"));
        this.userLocation = userLocation;
        this.mMap = mMap;
        getFlags();
    }



    public void getFlags(){
        GeoQuery geoQuery = mGeoFire.queryAtLocation(new GeoLocation(userLocation.latitude, userLocation.longitude),10000);


        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            //First get all the data within the radius of user and add to the list
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                // Add to hashMap the key and location of flags in vicinity
                flagMap.put(key, location);
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            // Once all finished on the initial call of the flags, plot onto the map and add listener to each
            @Override
            public void onGeoQueryReady() {
                //Log.i("Flag map: ", Double.toString(flagMap.get("keyvalue1").latitude));
                Log.i("Flag map ", flagMap.toString());
                Iterator it = flagMap.entrySet().iterator();
                // Iteratorate through flags and put on map
                while (it.hasNext()) {
                    Map.Entry flag = (Map.Entry)it.next();
                    Object key = flag.getKey();
                    // get position from hash map
                    LatLng positionFlag = new LatLng(flagMap.get(key).latitude, flagMap.get(key).longitude);
                    //marker the flag
                    markerMap.put(key.toString(), mMap.addMarker(new MarkerOptions().position(positionFlag).icon(BitmapDescriptorFactory.fromResource(R.drawable.mapicon))));
                }

                Log.i("Map size", Integer.toString(flagMap.size()));
                if (flagMap.size()<5){
                    makeFlags();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });


        // Get reference for flags to add a listener
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("flags");

        // Set up listener for new players to add to map
        ChildEventListener childEventListener = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // If it is not present in the hashFunction
                if (!flagMap.containsValue(dataSnapshot.getKey().toString())){

                    // Get the map from the datasnapshot
                    Map<String, Object> value = (Map<String, Object>) dataSnapshot.getValue();
                    // Its a string representation of an array, split on "," and remove "[" and  "]"
                    String[] locationString = value.get("l").toString().substring(1).split(",");
                    // Parse to Doubles
                    Double lat = Double.parseDouble(locationString[0]);
                    Double lng = Double.parseDouble(locationString[1].substring(0,locationString[1].length()-1));
                    // Add to the hash table
                    flagMap.put(dataSnapshot.getKey().toString(),new GeoLocation(lat, lng));
                    //Draw on the map
                    addToMap(dataSnapshot.getKey().toString(), new LatLng(lat, lng));
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

    }

    // If there is no flags within 2km, then make more and update the database
    public void makeFlags(){
        // Make flags around user
        ArrayList<double[]> flags = flagRequest.requestFlags(userLocation);

        // ------- Push flags up to the fireBase ---------- \\
        //Reference database
        DatabaseReference flagRef = FirebaseDatabase.getInstance().getReference("flags");

        // Use Firebase for special requests
        GeoFire flagGeoFire = new GeoFire(flagRef);

        //Loop through all the flags to push up
        for(double[] flag:flags){
            // Push to generate hash key
            String ref1 = flagRef.push().getKey();
            // Using hash key, use geofire to child "l"
            flagGeoFire.setLocation(ref1.toString(), new GeoLocation(flag[0], flag[1]));
            // As it updates the firebase, it will notify the listener in which will then add to the map automatically
        }

    }

    // When the fireBase is updated with new data
    public void addToMap(String key, LatLng Location){
        // Update on the map the flags
        markerMap.put(key, mMap.addMarker(new MarkerOptions().position(Location).icon(BitmapDescriptorFactory.fromResource(R.drawable.mapicon))));
    }



}
