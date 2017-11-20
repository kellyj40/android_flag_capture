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

/**
 * Created by danieljordan on 15/11/2017.
 */

public class User {
    private String playerId;
    private Marker userMarkerRef;
    private DatabaseReference playerRef;
    private GoogleMap mMap;

    public User(String playerId, GoogleMap mMap) {
        this.playerId = playerId;
        this.mMap = mMap;
        makePlayerRef();
    }

    public void makePlayerRef() {
        playerRef = FirebaseDatabase.getInstance().getReference("usersPlaying").child("userIds").child(playerId);
        playerRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("             playerId ", playerId);

                if(dataSnapshot.exists()){

                    Map<String, Object> userMap = (Map<String, Object>) dataSnapshot.getValue();
                    boolean hasFlag =(boolean) userMap.get("hasFlag");

                    List<Object> map = (List<Object>) userMap.get("l");
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


}
