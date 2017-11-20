package com.joekelly.mapsandlocation;

import android.widget.Toast;

import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by joekelly on 26/10/2017.
 * This class calculates the distance between different locations, such as the user an a flag
 */

public class DistanceCalculations {

    // Radians formula
    public static double rad(double x) {
        return x * Math.PI / 180;
    }
    // Loop through all flags and calculate distance
    public static int checkFlagDistances(LatLng userLocation, ArrayList<double[]> arrFlags) {
        double R = 6378137; // Earth’s mean radius in meter
        int count = 0;
        for(double[] flag: arrFlags){
            // Calculate the distance for each flag against the user location
            double dLat = rad(flag[0] - userLocation.latitude);
            double dLong = rad(flag[1] - userLocation.longitude);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(rad(userLocation.latitude)) * Math.cos(rad(flag[0])) *
                            Math.sin(dLong / 2) * Math.sin(dLong / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double d = R * c;

            if (d < 15) {
                // If within the distance, then remove from the linkedList
                arrFlags.remove(flag);
                // Return the index of what was removed
                return count;
            }
            count++;
        }

        return -1;
    }

    public static String checkFlagDistancesPublic(LatLng userLocation, Map<String, GeoLocation> flagLocations) {
        double R = 6378137; // Earth’s mean radius in meter
        int count = 0;

        Iterator flags = flagLocations.entrySet().iterator();
        while (flags.hasNext()) {
            Map.Entry flag = (Map.Entry) flags.next();

            // Get key
            Object key = flag.getKey();

            // Calculate distance from flag
            double dLat = rad(flagLocations.get(key.toString()).latitude - userLocation.latitude);
            double dLong = rad(flagLocations.get(key.toString()).longitude - userLocation.longitude);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(rad(userLocation.latitude)) * Math.cos(rad(flagLocations.get(key.toString()).latitude)) *
                            Math.sin(dLong / 2) * Math.sin(dLong / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double d = R * c;

            // Check if within distance
            if (d < 15) {
                // Retrun key of the flag
                return key.toString();
            }
        }
        return null;
    }

}
