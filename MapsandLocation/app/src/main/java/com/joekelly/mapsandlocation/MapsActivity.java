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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private boolean hasFlag = false;
    private double[] locationFlagCaptured = new double[2];
    private GoogleMap mMap;
    private GroundOverlay overLayReference;
    private int flagsCaptured = 0;


    private double[][] arrFlags;

    LocationManager locationManager;
    LocationListener locationListener;
    private Marker[] objectReference;
    private DataBaseManagement referenceDataBase;
    private Cursor c;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
        FlagRequest getFlagsObject = new FlagRequest();

        // Make flag request and plot onto the map
        arrFlags = getFlagsObject.requestFlags();


    }
    @Override //Dont know if really need this?
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // Ask for permission to get the users location
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != getPackageManager().PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                if (grantResults.length > 0 && grantResults[0] == getPackageManager().PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, locationListener);
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(true);
                }
            }
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Set the map
        mMap = googleMap;
        // Used for getting access to the systems location service
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);


        int countFlags = 0;
        double[] flag;
        objectReference = new Marker[arrFlags.length];
        while(countFlags<arrFlags.length){
            flag = arrFlags[countFlags];
            LatLng position = new LatLng(flag[0], flag[1]);

            objectReference[countFlags] = mMap.addMarker(new MarkerOptions().position(position).title("Flag").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
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
                    objectReference[value].remove();
                    locationFlagCaptured[0] = latitude;
                    locationFlagCaptured[1] = longitude;
                    hasFlag = true;
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
            Toast.makeText(MapsActivity.this, "UPdate", Toast.LENGTH_SHORT).show();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            mMap.setMyLocationEnabled(true);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        } else {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != getPackageManager().PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            }
            //Use this for when opening the map
            Location lastKnownLocation = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);

            LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
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
                startActivity(new Intent(this, Abouter.class));
                return true;
            case R.id.help:
                startActivity(new Intent(this, Abouter.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

}