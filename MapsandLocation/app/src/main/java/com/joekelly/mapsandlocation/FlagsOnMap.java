package com.joekelly.mapsandlocation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.ArrayList;
/**
 * Created by joekelly on 14/11/2017.
 * This calss is used for putting the and handling the private map flags
 * In this class there are methods for putting flags on map,
 * removing flags from map and drawing the layovers for the distance of picking up flags
 */

public class FlagsOnMap {

    GoogleMap mMap;

    public Marker[] flagObjectReference;
    public GroundOverlay[] overLayReferenceFlags;
    public int numberOfFlagsRemaining = 0;

    public FlagsOnMap(GoogleMap mMap){
        this.mMap = mMap;
    }

    public Marker[] getFlagMarkerObject(){
        return flagObjectReference;
    }
    public GroundOverlay[] getOverLayReferenceFlags(){
        return overLayReferenceFlags;
    }


    public void drawFlagsToMap(ArrayList<double[]> arrFlags){

        //------- FLAG LOGIC -------
        // drawing the flags onto the map
        int flagIndex = 0; // initiate flag index, loop through all and draw to the map
        numberOfFlagsRemaining = arrFlags.size();

        // Overlay references for flagObjects and radius of pick up area
        flagObjectReference = new Marker[arrFlags.size()];
        overLayReferenceFlags = new GroundOverlay[arrFlags.size()];

        // loops over each flagPosition and put onto the map


        for(double[] arrFlag: arrFlags){
            LatLng position = new LatLng(arrFlag[0], arrFlag[1]);

            // Reference array for the markers of the flags
            flagObjectReference[flagIndex] = mMap.addMarker(new MarkerOptions().position(position).title("Flag").icon(BitmapDescriptorFactory.fromResource(R.drawable.mapicon)));

            // Radius of the circle
            int radiusGet = 15;

            //Circle
            Bitmap bm = drawCircle();

            // generate BitmapDescriptor from circle Bitmap
            BitmapDescriptor bmD = BitmapDescriptorFactory.fromBitmap(bm);

            //Add the circle to the map
            overLayReferenceFlags[flagIndex] =  mMap.addGroundOverlay(new GroundOverlayOptions().
                    image(bmD).
                    position(position,radiusGet*2,radiusGet*2).transparency(0.4f));

            // Increment the number of flags
            flagIndex++;
        }




    }
    public void removeFlagFromMap(int indexOfCapturedFlag){

        // Remove the flag and overlay
        flagObjectReference[indexOfCapturedFlag].remove();
        overLayReferenceFlags[indexOfCapturedFlag].remove();

        // move objects down so they are in the right position...
        for (int i = indexOfCapturedFlag; i<flagObjectReference.length-1; i++){
            flagObjectReference[i] = flagObjectReference[i+1];
            overLayReferenceFlags[i] = overLayReferenceFlags[i+1];
        }
        // Number of flags left to collect is decremented
        numberOfFlagsRemaining--;

    }

    public Bitmap drawCircle(){

        // drawing circle
        int d = 15; // diameter
        Bitmap bm = Bitmap.createBitmap(d, d, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        Paint p = new Paint();
        p.setColor(Color.GREEN);

        c.drawCircle(d/2, d/2, d/2, p);
        return bm;
    }


}
