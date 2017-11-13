package com.joekelly.mapsandlocation;

import com.google.android.gms.maps.model.LatLng;

import java.util.Random;

/**
 * Created by jakek on 06/11/2017.
 */

// Creates array of flags based on user location
// To be used in private game (PrivateMap)
public class PrivateFlagRequest {


    public double[][] requestFlags(LatLng point) {
        int radius = 100;
        int numberPins = 10;
        double[][] arrFlags = new double[numberPins][2];

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
            arrFlags[i][0] = foundLatitude;
            arrFlags[i][1] = foundLongitude;
        }
        //Get nearest point to the centre
        return arrFlags;
    }

}
