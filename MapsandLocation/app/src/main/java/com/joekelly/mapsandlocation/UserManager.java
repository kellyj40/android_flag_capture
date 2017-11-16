package com.joekelly.mapsandlocation;

import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by danieljordan on 15/11/2017.
 */

// Manages all users - updates their location on map, keeps track of how many are playing
public class UserManager {
//    Map<String, Object> users;
    Map<String, User> userMap = new HashMap<String, User>();

    // These deal with the user of the app - not other users
    private String userId;
    private boolean hasFlag = false;
    private GoogleMap mMap;


    public UserManager(GoogleMap mMap) {
        // get this user's ID
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.mMap = mMap;
        getUsers();
    }

    // sets user location in firebase
    public void setUserLocation(LatLng userLocation) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("usersPlaying").child("userIds");

        // Telling GeoFire where we want to store it
        GeoFire geoFire = new GeoFire(ref);
        geoFire.setLocation(userId, new GeoLocation(userLocation.latitude, userLocation.longitude));
        ref.child(userId).child("hasFlag").setValue(hasFlag);
    }

    public void removeUserFromPlaying() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("usersPlaying").child("userIds");

        // Telling GeoFire where we want to store it
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);
        ref.child(userId).removeValue();
    }


    public void getUsers() {
        //Get reference to database
        DatabaseReference playerRef = FirebaseDatabase.getInstance().getReference("usersPlaying").child("userIds");
        //Get snap shot of the database
        playerRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        makePlayerHashMap((Map<String, Object>) dataSnapshot.getValue());
                        Log.d("                -- ", userMap.toString());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });
    }


    // Populates hashmap with user objects, one for each other user on the firebase Backend who's playing
    public void makePlayerHashMap(Map<String, Object> users) {
//        ArrayList<Object> userIds = new ArrayList<>();

        //iterate through each user, making User object, and then adding them to userMap
        for (Map.Entry<String, Object> entry : users.entrySet()) {

            String playerId = entry.getKey();

            if (playerId != this.userId) {
                User newUser = new User(playerId, mMap);
                userMap.put(playerId, newUser);
            }
        }
    }
    // This method is to check if the player exists before adding to map when new user comes in
    public boolean checkIfPlayerExists(String playerId){
        if (playerId != this.userId)
            return userMap.containsValue(playerId);

        return true;
    }
    // When its a new user, add player to data structure and put on map
    public void addPlayerToHashMap(String playerId) {
        if (playerId != this.userId) {
            User newUser = new User(playerId, mMap);
            userMap.put(playerId, newUser);
        }
    }
}