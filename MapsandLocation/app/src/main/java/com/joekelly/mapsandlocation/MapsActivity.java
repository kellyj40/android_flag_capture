package com.joekelly.mapsandlocation;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

// Activity for the private game
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,SensorEventListener, StepListener{
    private boolean hasFlag = false;
    private double[] locationFlagCaptured = new double[2];
    private GoogleMap mMap;
    private GroundOverlay overLayReference;
    private int flagsCaptured = 0;
    Vibrator v;
    // Step instances
    private StepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accel;
    private static final String TEXT_NUM_STEPS = "Number of steps taken:";
    private int numSteps;
    int saveSteps;
    private TextView StepsTaken;

    //stats db
    Databasehelperclass myDb;


    private double[][] arrFlags;

    LocationManager locationManager;
    LocationListener locationListener;
    private GroundOverlay[] overLayReferenceFlags;
    private Marker[] objectReferenceFlags;
    private DataBaseManagement referenceDataBase;
    private Cursor c;
    private LatLng userLocation;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        createDatabaseTable();
        getLocation();
        initialiseStepSensor();

        //Set up stats page db
        myDb = new Databasehelperclass(this);
    }

    public void createDatabaseTable() {
        //Making table to store all the values of the flags collected by user
        try{
            SQLiteDatabase userDatabase = this.openOrCreateDatabase("UserData", MODE_PRIVATE, null);
            referenceDataBase = new DataBaseManagement(userDatabase);

            flagsCaptured= referenceDataBase.makeLocalFlagTable();

            TextView textView = (TextView) findViewById(R.id.distance);
            textView.setText("Captured: " + Integer.toString(flagsCaptured));
        }catch (Exception e){
            Toast.makeText(MapsActivity.this, "Error in Database", Toast.LENGTH_SHORT).show();
        }
    }

    public void getLocation() {
        // getting location
        Intent intent = getIntent();
        Double startingLat = intent.getDoubleExtra("LAT", 0.0);
        Double startingLon = intent.getDoubleExtra("LON", 0.0);

        userLocation = new LatLng(startingLat, startingLon);
        showToast(userLocation.toString());

        // initialising flags
        PrivateRequest getFlagsObject = new PrivateRequest();
        arrFlags = getFlagsObject.requestFlags(userLocation);
    }

    public void initialiseStepSensor() {
        // Get an instance of the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);
        StepsTaken = (TextView) findViewById(R.id.tv_steps);
        numSteps = 0;
        sensorManager.registerListener(MapsActivity.this, accel, SensorManager.SENSOR_DELAY_FASTEST);
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Set the map
        mMap = googleMap;
        // Used for getting access to the systems location service
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);


        int countFlags = 0;
        double[] flag;
        objectReferenceFlags = new Marker[arrFlags.length];
        overLayReferenceFlags = new GroundOverlay[arrFlags.length];

        while(countFlags<arrFlags.length){
            flag = arrFlags[countFlags];
            LatLng position = new LatLng(flag[0], flag[1]);

            objectReferenceFlags[countFlags] = mMap.addMarker(new MarkerOptions().position(position).title("Flag").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));


            int radiusGet = 15;
            // drawing circle
            int d = 15; // diameter
            Bitmap bm = Bitmap.createBitmap(d, d, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bm);
            Paint p = new Paint();
            p.setColor(Color.GREEN);

            c.drawCircle(d/2, d/2, d/2, p);

            // generate BitmapDescriptor from circle Bitmap
            BitmapDescriptor bmD = BitmapDescriptorFactory.fromBitmap(bm);

            //Add the circle
            overLayReferenceFlags[countFlags] =  mMap.addGroundOverlay(new GroundOverlayOptions().
                    image(bmD).
                    position(position,radiusGet*2,radiusGet*2).transparency(0.4f));

            countFlags++;

        }


        // Add listener for GPS movement
        locationListener = new LocationListener() {


            public void onLocationChanged(Location location) {

                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

                // Calculate the distance to all the flags
                int value = DistanceCalculations.checkFlagDistances(userLocation, arrFlags);


                if (value>=0 && !hasFlag) {

                    int radiusM = 100;

                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    LatLng latLng = new LatLng(latitude,longitude);

                    // drawing circle
                    int d = 500; // diameter
                    Bitmap bm = Bitmap.createBitmap(d, d, Bitmap.Config.ARGB_8888);
                    Canvas c = new Canvas(bm);
                    Paint p = new Paint();
                    p.setColor(Color.RED);

                    c.drawCircle(d/2, d/2, d/2, p);

                    // generate BitmapDescriptor from circle Bitmap
                    BitmapDescriptor bmD = BitmapDescriptorFactory.fromBitmap(bm);

                    //Add the circle
                    overLayReference = mMap.addGroundOverlay(new GroundOverlayOptions().
                            image(bmD).
                            position(latLng,radiusM*2,radiusM*2).transparency(0.4f));

                    // Remove the flag
                    objectReferenceFlags[value].remove();
                    overLayReferenceFlags[value].remove();
                    locationFlagCaptured[0] = latitude;
                    locationFlagCaptured[1] = longitude;
                    hasFlag = true;
                    v.vibrate(500);
                }
                if (hasFlag){
                    boolean successCapture = DistanceCalculations.checkCapturedDistance(userLocation, locationFlagCaptured);
                    if (successCapture){

                        overLayReference.remove();
                        hasFlag = false;
                        Toast.makeText(MapsActivity.this, R.string.flag_collected, Toast.LENGTH_SHORT).show();
//                        Add captured flag
                        referenceDataBase.updateLocalFlagTable();
                        flagsCaptured++;

                        TextView textView = (TextView) findViewById(R.id.distance);
                        textView.setText("Captured: " + Integer.toString(flagsCaptured));
                        v.vibrate(1000);

                    }
                }
                // Keep camera on the user
                mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }

        };


        // Must be after the location listener is made
        // Once the map is ready put the location onto the map
        if (Build.VERSION.SDK_INT < 23) {
            Toast.makeText(MapsActivity.this, "Update", Toast.LENGTH_SHORT).show();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            mMap.setMyLocationEnabled(true);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        } else {
//            Toast.makeText(MapsActivity.this, "Last Location", Toast.LENGTH_SHORT).show();
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != getPackageManager().PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
//                Toast.makeText(this, "Map initialized", Toast.LENGTH_SHORT).show();
            }
            //Use this for when opening the map
//            Location lastKnownLocation = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);

//            LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());

            // Move camera to the location of the user
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 16));
            mMap.setMyLocationEnabled(true);
        }



    }

    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                // passes steps to stats page
                Intent registerIntent = new Intent(this, StatsActivity.class);
                int x = numSteps;
                //adds current steps to db
                //myDb.addSteps(new Steps(x));
                registerIntent.putExtra("numSteps", x);
                startActivity(registerIntent);
                return true;
            case R.id.help:
                startActivity(new Intent(this, Abouter.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    // Steps Here
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
        StepsTaken.setText(TEXT_NUM_STEPS + numSteps);
    }
    protected void onStart() {

        super.onStart();
    }
    protected void onPause() {
        super.onPause();
        //saveSteps = numSteps;
        //Toast.makeText(MapsActivity.this, saveSteps+"Pause", Toast.LENGTH_SHORT).show();
        //myDb.addSteps(new Steps(numSteps));
    }
    protected void onResume() {
        super.onResume();
       // Toast.makeText(MapsActivity.this, saveSteps+"Resume", Toast.LENGTH_SHORT).show();
        //numSteps = saveSteps;
        //myDb.addSteps(new Steps(numSteps));
    }
    protected void onStop() {
        super.onStop();
        //Toast.makeText(MapsActivity.this, saveSteps+"Saving to database", Toast.LENGTH_SHORT).show();
        myDb.addSteps(new Steps(numSteps));
    }

    public void showToast(String message) {
        Context context = getApplicationContext();
        CharSequence text = message;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}