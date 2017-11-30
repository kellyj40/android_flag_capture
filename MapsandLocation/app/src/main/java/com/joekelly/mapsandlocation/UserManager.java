package com.joekelly.mapsandlocation;

import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 *  Manages all users - making their objects for updating them on the map and to firebase
 */

public class UserManager {
    Map<String, User> userMap = new HashMap<String, User>();

    // These deal with the user of the app - initiate their details
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

    }

    // sets user location in firebase
    public void setUserLocation(LatLng userLocation) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("usersPlaying").child("userIds");

        // Telling GeoFire where we want to store it
        GeoFire geoFire = new GeoFire(ref);
        geoFire.setLocation(userId, new GeoLocation(userLocation.latitude, userLocation.longitude));
        ref.child(userId).child("hasFlag").setValue(hasFlag);
    }

    //When finish playing, update firebase that the user has stop playing to sync to all users
    public void removeUserFromPlaying() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("usersPlaying").child("userIds");

        // Telling GeoFire where we want to store it
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);
        ref.child(userId).removeValue();
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
    // This will update the users database with the number of flags captured
    public void capturedFlagUpdate(){
        numberOfFlagsCollected++;
        // Update Database of new flag
        DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        current_user_db.child("flags collected").setValue(numberOfFlagsCollected);

    }

    // This checks to see if the user stole a flag
    public boolean checkIfUserStoleFlag(){
        if (numberOfFlagsStolen != numberOfFlagsStolenChecker){
            numberOfFlagsStolenChecker = numberOfFlagsStolen;
            return true;
        }
        return false;
    }

    // This checks to see if another player has stole the users flag when they have one
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

    // Getters and setters for the private variables

    // Set the has flag varaible
    public void setHasFlag(boolean value){
        hasFlag = value;
    }
    // Get the has flag variable
    public boolean getHasFlag(){
        return hasFlag;
    }
    // Get the number of flags captured
    public int getNumberOfFlagsCollected(){
        return numberOfFlagsCollected;
    }

}