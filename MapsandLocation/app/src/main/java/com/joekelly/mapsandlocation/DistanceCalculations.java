package com.joekelly.mapsandlocation;

import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by joekelly on 26/10/2017.
 */

public class DistanceCalculations {

    public static double rad(double x) {
        return x * Math.PI / 180;
    }

    public static int checkFlagDistances(LatLng userLocation,double[][] arrFlags) {
        double R = 6378137; // Earth’s mean radius in meter
        int count = 0;
        double[] flag;
        while (count < arrFlags.length){
            flag = arrFlags[count];
            double dLat = rad(flag[0] - userLocation.latitude);
            double dLong = rad(flag[1] - userLocation.longitude);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(rad(userLocation.latitude)) * Math.cos(rad(flag[0])) *
                            Math.sin(dLong / 2) * Math.sin(dLong / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double d = R * c;

            if (d < 10) {
                return count;
            }
            count++;
        }

        return -1;
    }

}
