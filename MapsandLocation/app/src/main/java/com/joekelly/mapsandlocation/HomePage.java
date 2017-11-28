package com.joekelly.mapsandlocation;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

public class HomePage extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    protected Location mLastLocation;
    protected GoogleApiClient mGoogleApiClient;
    protected static final String TAG = "Homepage";

    protected Double mLatitudeText;
    protected Double mLongitudeText;

    protected Boolean hasLocation;
    private String locationErrorMessage = "Error! Can't find location. Please try turning on location services.";

    //Variable with the current steps
    private int numSteps;
    //Object which contains the step listeners;
    private SensorObject stepObject;

    //stats db
    Databasehelperclass myDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        getLocationPermission(this, this);
        buildGoogleApiClient();

        //Step stuff

        stepObject = new SensorObject();

        numSteps = stepObject.numSteps;
        stepObject.initialiseStepSensor(this);

        myDb = new Databasehelperclass(this);

        final JSONObject weather = GetWeather.getJSON(this, "Dublin, IE");
//        Toast.makeText(this, weather.toString(), Toast.LENGTH_LONG).show();

        stepWidget();
//        Spotify.setContext(this);
//        getCurrentlyPlaying();

    }

    private void stepWidget() {
        int today = myDb.todaysSteps() + numSteps;
        TextView stepsWidget = findViewById(R.id.step_widget);

        String getString = getResources().getString(R.string.motivation_widget);
        String setMessage = String.format(getString, today);
        stepsWidget.setText(setMessage);
        stepObject.initialiseStepSensor(this, getString, stepsWidget, today);


    }

    @Override
    public void onStart() {
        super.onStart();
        stepObject.numSteps = 0;
//        getLocationPermission(this, this);
//        buildGoogleApiClient();
    }


    public static void getLocationPermission(Context context, Activity activity) {
//         Checking for permission ???
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    8034);
        } else {
            // permission has been granted, continue as usual
//            showToast( "already have permission");
        }
    }

    public void getLocation() {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        if (mLastLocation != null) {
            mLatitudeText = mLastLocation.getLatitude();
            mLongitudeText = mLastLocation.getLongitude();
            hasLocation = true;
        } else {
//            Toast.makeText(this, R.string.no_location_detected, Toast.LENGTH_LONG).show();
            hasLocation = false;
        }
    }

    public void showToast(String message) {
        Context context = getApplicationContext();
        CharSequence text = message;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }


    public void publicGameLauncher(View view) {
        getLocation();
        Intent intent = new Intent(this, UserLoginActivity.class);
        intent.putExtra("LAT", mLatitudeText);
        intent.putExtra("LON", mLongitudeText);
        intent.putExtra("nextActivity", "PublicMap");
//        myDb.addSteps(new Steps(stepObject.numSteps));

        // check if we have successfully recieved user's location
        // if not, ask them to check their settings
        if (hasLocation) {
            startActivity(intent);
        } else {
            showToast(locationErrorMessage);
        }
    }

    public void privateGameLauncher(View view) {
        getLocation();
        Intent intent = new Intent(HomePage.this, PrivateMap.class);
        intent.putExtra("LAT", mLatitudeText);
        intent.putExtra("LON", mLongitudeText);
//        myDb.addSteps(new Steps(stepObject.numSteps));

        // check if we have successfully recieved user's location
        // if not, ask them to check their settings
        if (hasLocation) {
            startActivity(intent);
        } else {
            showToast(locationErrorMessage);
        }
    }

    public void statsLauncher(View view) {
        Intent intent = new Intent(this, StatsActivity.class);

        //Add steps to DB
//        myDb.addSteps(new Steps(stepObject.numSteps));
        startActivity(intent);
    }

    public void leaderboardLauncher(View view) {
        Intent intent = new Intent(this, UserLoginActivity.class);
//        myDb.addSteps(new Steps(stepObject.numSteps));
        intent.putExtra("nextActivity", "Leaderboard");
        startActivity(intent);

    }

    public void onConnected(Bundle connectionHint) {
        Log.i("count", "0");
        getLocation();
    }

    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    //Andrea
    protected void onStop() {
        super.onStop();
        myDb.addSteps(new Steps(stepObject.numSteps));

        //Toast.makeText(PrivateMap.this, saveSteps+"Saving to database", Toast.LENGTH_SHORT).show();
        //myDb.addSteps(new Steps(stepObject.numSteps));
    }

    protected void onPause() {
        super.onPause();
        //saveSteps = numSteps;
        //Toast.makeText(PrivateMap.this, saveSteps+"Pause", Toast.LENGTH_SHORT).show();
        //myDb.addSteps(new Steps(stepObject.numSteps));
    }

    protected void onResume() {
        super.onResume();
        stepWidget();
}

//    public void spotify(View view){
////        String uri = "spotify:track:0IcSLT53eE07Jmok64Ppo3";
////        Intent launcher = new Intent( Intent.ACTION_VIEW, Uri.parse(uri) );
////        launcher.addFlags(Intent.FLAG_FROM_BACKGROUND);
//
//        Intent launcher = new Intent("com.spotify.mobile.android.ui.widget.NEXT");
//
//        launcher.setPackage("com.spotify.music");
//
//        sendBroadcast(launcher);
//    }

//    public void spotifyPlay(View view) {
//        Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
//        i.setComponent(new ComponentName("com.spotify.music", "com.spotify.music.internal.receiver.MediaButtonReceiver"));
//        i.putExtra(Intent.EXTRA_KEY_EVENT,new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY));
//        sendOrderedBroadcast(i, null);
//
//        i = new Intent(Intent.ACTION_MEDIA_BUTTON);
//        i.setComponent(new ComponentName("com.spotify.music", "com.spotify.music.internal.receiver.MediaButtonReceiver"));
//        i.putExtra(Intent.EXTRA_KEY_EVENT,new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY));
//        sendOrderedBroadcast(i, null);
//    }

    public void spotifyPlayPause(View view){
        try{
//        String uri = "spotifyPause:track:0IcSLT53eE07Jmok64Ppo3";
//        Intent launcher = new Intent( Intent.ACTION_VIEW, Uri.parse(uri) );
////        launcher.addFlags(Intent.FLAG_FROM_BACKGROUND);
//
//        Intent launcher = new Intent("com.spotify.mobile.android.ui.widget.PAUSE");
//
//        launcher.setPackage("com.spotify.music");
        int keyCode = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
//
//            if (!mAudioManager.isMusicActive() && !isSpotifyRunning()) {
//            startMusicPlayer();
//            }

            Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
            i.setPackage("com.spotify.music");
    synchronized (this) {
            i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
            sendOrderedBroadcast(i, null);

            i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, keyCode));
            sendOrderedBroadcast(i, null);
            }
    }
        catch (Exception e) {
        Toast.makeText(this, "Spotify not installed", Toast.LENGTH_LONG).show();
    }

}

    public void spotifyNext(View view) {
        try {
            Intent launcher = new Intent("com.spotify.mobile.android.ui.widget.NEXT");

            launcher.setPackage("com.spotify.music");

            sendBroadcast(launcher);
        }
        catch (Exception e) {
            Toast.makeText(this, "Spotify not installed", Toast.LENGTH_LONG).show();
        }
    }

    public void openSpotify(View view) {
        try {
            String uri = "spotify:track:53qU2K55RJgfs9F50irpB3";
            Intent launcher = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
//        launcher.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(launcher);
        } catch (Exception e) {
            Toast.makeText(this, "Spotify not installed", Toast.LENGTH_LONG).show();
        }
    }

//    public void getCurrentlyPlaying(){
//        Intent launcher = new Intent("com.spotify.music.metadatachanged");
//        launcher.setPackage("com.spotify.music");
//
//        sendBroadcast(launcher);
//
//    }
}
