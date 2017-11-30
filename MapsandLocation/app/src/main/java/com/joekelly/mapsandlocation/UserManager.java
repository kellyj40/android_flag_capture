package com.joekelly.mapsandlocation;

import android.support.annotation.NonNull;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    private int numberOfFlagsCollected;
    private int numberOfFlagsStolen;
    private int numberOfFlagsStolenChecker;
    private DatabaseReference userThatStoleFlag;
    public UserManager(GoogleMap mMap) {
        // get this user's ID
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.mMap = mMap;

        // -------- Get previous score -------\\
        // Get reference to database
        DatabaseReference playerRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        //Get snap shot of the database
        playerRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Map<String, Object> scoreMap = (Map<String, Object>) dataSnapshot.getValue();
                        numberOfFlagsCollected = Integer.parseInt(scoreMap.get("flags collected").toString());
                        numberOfFlagsStolen = Integer.parseInt(scoreMap.get("flags stolen").toString());
                        numberOfFlagsStolenChecker = numberOfFlagsStolen;
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });

        // Listener to check if stole a flag
        DatabaseReference playerRefFlagsStolen = FirebaseDatabase.getInstance().getReference("users").child(userId).child("flags stolen");
        //Listener for if user steals a flag
        ValueEventListener stolenListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("FlagStolen", "FlagStolen");
                numberOfFlagsStolen = numberOfFlagsStolenChecker+1;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
            }
        };
        playerRefFlagsStolen.addValueEventListener(stolenListener);

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
        //iterate through each user, making User object, and then adding them to userMap
        for (Map.Entry<String, Object> entry : users.entrySet()) {

            String playerId = entry.getKey();

            if (playerId != this.userId){
                if (!userMap.containsValue(playerId)) {
                    User newUser = new User(playerId, mMap);
                    userMap.put(playerId, newUser);
                }
            }
        }
    }

    //When player leaves map, remove them from hashMap and Map
    public void removePlayerFromHashMap(String playerId) {
        if (playerId != this.userId) {
            User userBeingRemoved = userMap.get(playerId);
            userBeingRemoved.removePlayerFromMap();
            userMap.remove(playerId);
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

    public void setHasFlag(boolean value){
        hasFlag = value;
    }
    public boolean getHasFlag(){
        return hasFlag;
    }

    public void capturedFlagUpdate(){
        numberOfFlagsCollected++;
        // Update Database of new flag
        DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        current_user_db.child("flags collected").setValue(numberOfFlagsCollected);

    }
    public boolean checkIfUserStoleFlag(){
        if (numberOfFlagsStolen != numberOfFlagsStolenChecker){
            numberOfFlagsStolenChecker = numberOfFlagsStolen;
            return true;
        }
        return false;
    }

    public boolean checkIfOtherPlayersStoleFlag(LatLng userLatLng){
        // The user playing the games phone is userLatLng, other players are player references
        //iterate through each user, making User object, and then adding them to userMap
        for (Map.Entry<String, User> entry : userMap.entrySet()) {

            String otherPlayerId = entry.getKey();
            User playerRef = userMap.get(otherPlayerId);


            // The user is checking to see if someone else has stolen their flag, someone who doesn't have a flag
            if (!playerRef.playerHasFlag() && DistanceCalculations.distanceBetweenTwoPlayers(userLatLng, playerRef.getPlayerLatLng())) {
                userThatStoleFlag = FirebaseDatabase.getInstance().getReference().child("users").child(otherPlayerId);

                //Get snap shot of the players data to increment
                userThatStoleFlag.addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Map<String, Object> playerStolenMap = (Map<String, Object>) dataSnapshot.getValue();
                                int playerFlagsStolen = Integer.parseInt(playerStolenMap.get("flags stolen").toString());
                                playerFlagsStolen +=1;
                                userThatStoleFlag.child("flags stolen").setValue(playerFlagsStolen);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                //handle databaseError
                            }
                        });
                return true;
            }
        }
        return false;
    }

}