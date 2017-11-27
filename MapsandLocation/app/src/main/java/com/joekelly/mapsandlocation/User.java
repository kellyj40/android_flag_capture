package com.joekelly.mapsandlocation;

import android.util.Log;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

/*
 * Created by danieljordan on 15/11/2017.
 * This class is incharge of all other users on the database
 */

public class User {
    private String playerId;
    private Marker userMarkerRef;
    private DatabaseReference playerRef;
    private GoogleMap mMap;

    // ------------------------- Constructor -------------------------- \\
    public User(String playerId, GoogleMap mMap) {
        // Each user is its own object that will be displayed on the map
        this.playerId = playerId;
        this.mMap = mMap;
        makePlayerRef();
    }

    // ------------------ makePlayerReferenceListener to update location on map ------------------- \\
    public void makePlayerRef() {
        // Reference the database
        playerRef = FirebaseDatabase.getInstance().getReference("usersPlaying").child("userIds").child(playerId);
        //Listener for all users
        playerRef.addValueEventListener(new ValueEventListener() {
            //Create listener for changing data in child
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("             playerId ", playerId);

                if(dataSnapshot.exists()){
                    //Get snapshot of change
                    Map<String, Object> userMap = (Map<String, Object>) dataSnapshot.getValue();
                    boolean hasFlag = false;
                    // Issue with updating the flags, needs to be set to false
                    try {
                        hasFlag =(boolean) userMap.get("hasFlag");
                    }catch (Exception e){
                        hasFlag = false;
                    }


                    // Get the lat and lng position of user
                    List<Object> map = (List<Object>) userMap.get("l");
                    double locationLat = 0;
                    double locationLng = 0;
                    if (map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng playerLatLng = new LatLng(locationLat, locationLng);

                    //Remove all marker of user
                    if(userMarkerRef != null){
                        userMarkerRef.remove();
                    }
                    // Check if the user has a flag or not
                    if(hasFlag){
                        userMarkerRef = mMap.addMarker(new MarkerOptions().position(playerLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.userwithflag)));
                    }else{
                        userMarkerRef = mMap.addMarker(new MarkerOptions().position(playerLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.otherusers)));
                    }

                    Log.d("             playerId ", playerId);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void removePlayerFromMap() {
        userMarkerRef.remove();
    }

}
