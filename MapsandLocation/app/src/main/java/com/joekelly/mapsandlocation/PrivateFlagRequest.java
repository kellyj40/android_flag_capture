package com.joekelly.mapsandlocation;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Random;

/**
 * This class will create the position of flags within a killometer distance randomly
 */

// Creates array of flags based on user location
// To be used in private game (PrivateMap)x
public class PrivateFlagRequest {

    public ArrayList<double[]> arrFlags = new ArrayList<double[]>();
    public double[][] placeholder;

    public ArrayList<double[]> requestFlags(LatLng point) {
        int radius = 500;
        int numberPins = 5;

        //This is to generate 10 random points
        for(int i = 0; i<numberPins; i++) {

            double x0 = point.latitude;
            double y0 = point.longitude;

            Random random = new Random();

            // Convert radius from meters to degrees
            double radiusInDegrees = radius / 111000f;

            double u = random.nextDouble();
            double v = random.nextDouble();
            double w = radiusInDegrees * Math.sqrt(u);
            double t = 2 * Math.PI * v;
            double x = w * Math.cos(t);
            double y = w * Math.sin(t);

            // Adjust the x-coordinate for the shrinking of the east-west distances
            double new_x = x / Math.cos(y0);

            double foundLatitude = new_x + x0;
            double foundLongitude = y + y0;
            double[]placeholder = {foundLatitude,foundLongitude};

            //Add to the Linked List so can remove when collected.
            arrFlags.add(placeholder);

        }


        //Get nearest point to the centre
        return arrFlags;
    }

}
