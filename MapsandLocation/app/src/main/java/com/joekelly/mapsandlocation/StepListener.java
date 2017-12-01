package com.joekelly.mapsandlocation;

/**
 Interface that listens step alerts
 Tutorial followed: http://www.gadgetsaint.com/android/create-pedometer-step-counter-android/  October 2017
 **/

public interface StepListener {
    public void step(long timeNs);
}

