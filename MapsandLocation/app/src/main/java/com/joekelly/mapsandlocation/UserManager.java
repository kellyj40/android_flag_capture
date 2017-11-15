package com.joekelly.mapsandlocation;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

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
//    private ArrayList<String> users = new ArrayList<String>();
    Map<String, Object> users;
//    Map<String, DatabaseReference> playerRefMap = new HashMap<String, DatabaseReference>();
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


    public void getUsers() {
        //Get reference to database
        DatabaseReference playerRef = FirebaseDatabase.getInstance().getReference("usersPlaying").child("userIds");
        //Get snap shot of the database
        playerRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Get map of users in datasnapshot
//                        collectIDs((Map<String,Object>) dataSnapshot.getValue());
                        users = (Map<String, Object>) dataSnapshot.getValue();
                        makePlayerRefMap(users);
//                        Log.d("         playerRefMap: ", playerRefMap.toString());
//                        Log.d("                -- ", users.toString());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });

        // This is causing an error, because it's getting a null reference. Async issue?
//        return users.toString();
    }

    // iterates through map of users, collects their IDs
    public void collectIDs(Map<String, Object> users) {
        ArrayList<Object> userIds = new ArrayList<>();

        //iterate through each user, ignoring their UID
        for (Map.Entry<String, Object> entry : users.entrySet()) {

            //Get user map
            Map singleUser = (Map) entry.getValue();
            userIds.add(singleUser);
            //Get phone field and append to list
//            phoneNumbers.add((Boolean) singleUser.get("hasFlag"));
        }
    }

    public void makePlayerRefMap(Map<String, Object> users) {
//        ArrayList<Object> userIds = new ArrayList<>();

        //iterate through each user, making playerRef, and then adding them to database
        for (Map.Entry<String, Object> entry : users.entrySet()) {

            String playerId = entry.getKey();
//            DatabaseReference playerRef = makePlayerRef(playerId);
            User newUser = new User(playerId, mMap);

//            Log.d("              ---", playerId);
//            Log.d("              ---", this.userId);

//            playerRefMap.put(playerId, playerRef);
            userMap.put(playerId, newUser);
        }
    }

//     What if we make this the constructor of a player class?
//     Can store user id, userMarkerRef, eventlistener etc in the class, using this as the constructor
//     And then make a hashmap of User objects in usermanager?
    public DatabaseReference makePlayerRef(String playerId) {

//        String playerId = "w2O2eboiQnMOySIICEGBiQINBns2"; // use this??
//        String newPlayerID = "w2O2eboiQnMOySIICEGBiQINBns2";
//        DatabaseReference playerRef = FirebaseDatabase.getInstance().getReference("usersPlaying").child(newPlayerID).child("l");

        DatabaseReference playerRef = FirebaseDatabase.getInstance().getReference("usersPlaying").child(playerId).child("l");

        playerRef.addValueEventListener(new ValueEventListener() {

            // Not sure if this should be placed here.
            Marker userMarkerRef;

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
                    userMarkerRef = mMap.addMarker(new MarkerOptions().position(playerLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.otherusers)));;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return playerRef;
    }
}


// Why don't we see eachother on the map?
