package com.joekelly.mapsandlocation;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by pigna on 13/11/2017.
 */

public class SensorObject implements SensorEventListener, StepListener {
    //Step Sensor variables, need to fix textview
    public StepDetector simpleStepDetector;
    public SensorManager sensorManager;
    public Sensor accel;


    public static String TEXT_NUM_STEPS;
    private int extraStep;

    public int numSteps;
    int saveSteps;
    public TextView StepsTaken;
    Context contextNotification;
    TextView txtView;
    boolean textPresent;

    public void initialiseStepSensor(Context context) {
        // Get an instance of the SensorManager
        //txtView =new TextView(null);
        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);
        textPresent=false;
        //StepsTaken = (TextView) ((Activity)context).findViewById(R.id.tv_steps);
        numSteps = 0;
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
        //v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        contextNotification=context;


    } public void initialiseStepSensor(Context context, String text, TextView textView, int additionalSteps) {
        TEXT_NUM_STEPS = text;
        extraStep = additionalSteps;
        // Get an instance of the SensorManager
        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);
        //StepsTaken = (TextView) ((Activity)context).findViewById(R.id.tv_steps);
        numSteps = 0;
        textPresent=true;
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
        //v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        contextNotification=context;
        txtView = textView;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccel(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
    }

    @Override
    public void step(long timeNs) {
        numSteps++;

        //need to get the notifier working as there are issues with textview
//        updateUi(StepsTaken);

//        StepsTaken = (TextView) findViewById(R.id.tv_steps);
        if (textPresent)
            txtView.setText(String.format(TEXT_NUM_STEPS, extraStep+numSteps));

        if (numSteps == 110) {
            Notification.notifier(contextNotification);

        }
    }



    public void passTextView(TextView updateText){

        StepsTaken= updateText;
    }


    public void updateUi(TextView txt){

        txt.setText(TEXT_NUM_STEPS + numSteps);

    }

}