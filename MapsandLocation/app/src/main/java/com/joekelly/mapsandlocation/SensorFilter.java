package com.joekelly.mapsandlocation;

/**
 This class is used for the sensor readers of the Pedometer
 This will be used in all classes to record the users steps taken
 Tutorial followed: http://www.gadgetsaint.com/android/create-pedometer-step-counter-android/  October 2017
 **/


//This class filters the values that are steps
public class SensorFilter {

    private SensorFilter() {
    }

    public static float sum(float[] array) {
        float retval = 0;
        for (int i = 0; i < array.length; i++) {
            retval += array[i];
        }
        return retval;
    }
    
    //method for normalising the the values of the acceleration
    public static float norm(float[] array) {
        float retval = 0;
        for (int i = 0; i < array.length; i++) {
            retval += array[i] * array[i];
        }
        return (float) Math.sqrt(retval);
    }

    //find current z based on current acceleration and normalisng factor
    public static float dot(float[] a, float[] b) {
        float retval = a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
        return retval;
    }

}