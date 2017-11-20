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

    private static LatLng pickedUpFlagLocation;

    // Radians formula
    public static double rad(double x) {
        return x * Math.PI / 180;
    }

    public static double distance(double lat1, double lng1, double lat2, double lng2){

        double R = 6378137; // Earth’s mean radius in meter

        // Calculate the distance for each flag against the user location
        double dLat = rad(lat1 - lat2);
        double dLong = rad(lng1 - lng2);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(rad(lat2)) * Math.cos(rad(lat1)) *
                        Math.sin(dLong / 2) * Math.sin(dLong / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;

    }

    // Loop through all flags and calculate distance
    public static int checkFlagDistances(LatLng userLocation, ArrayList<double[]> arrFlags) {
        // Counter is for the array position to remove it
        int count = 0;
        for(double[] flag: arrFlags){

            double d = distance(flag[0], flag[1], userLocation.latitude, userLocation.longitude);

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

            // Distance
            double d = distance(flagLocations.get(key.toString()).latitude,flagLocations.get(key.toString()).longitude, userLocation.latitude,userLocation.longitude);

            // Check if within distance
            if (d < 30) {
                // Retrun key of the flag
                pickedUpFlagLocation = new LatLng(flagLocations.get(key.toString()).latitude, flagLocations.get(key.toString()).longitude);
                return key.toString();
            }
        }
        return null;
    }

    public static boolean checkedWalkedDistance(LatLng userLocation){

        double d = distance(pickedUpFlagLocation.latitude,pickedUpFlagLocation.longitude, userLocation.latitude,userLocation.longitude);

        // Check if within distance
        if (d > 200) {
            return true;
        }

        return false;
    }


}
