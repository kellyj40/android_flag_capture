package com.joekelly.mapsandlocation;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private double[][] arrFlags = {{53.305, -6.2210},
            {53.306, -6.2210},
            {53.304, -6.2210},
            {53.303, -6.2210524},
            {53.305, -6.222},
            {53.305, -6.223},
            {53.305, -6.220},
            {53.305, -6.219}};
    LocationManager locationManager;
    LocationListener locationListener;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

//        getMenuInflater().inflate(R.menu.main_menu, menu);
//        MenuItem item = menu.findItem(R.id.action_search);
////        searchView.setMenuItem(item);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != getPackageManager().PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                if (grantResults.length > 0 && grantResults[0] == getPackageManager().PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, locationListener);
                }
            }
        }

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;



        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {

                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                boolean value = checkFlagDistances(userLocation);
                if (value) {
                    mMap.addMarker(new MarkerOptions().position(userLocation).title("User location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                }else{
//                    mMap.addMarker(new MarkerOptions().position(userLocation).title("User location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                }
//                mMap.setMyLocationEnabled(true);
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 17));
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


        // CHECK HAVE PERMISSIONS
        if (Build.VERSION.SDK_INT < 23) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        } else {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != getPackageManager().PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                //USE this for when opening the map
                Location lastKnownLocation = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);

                LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());

//                LatLng userLocation = new LatLng(53.3052852, -6.2210524);

//                mMap.addMarker(new MarkerOptions().position(userLocation).title("User location"));

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 17));
                mMap.setMyLocationEnabled(true);
            }

        }

//         Four flags

        for (double[] flag : arrFlags) {
            LatLng position = new LatLng(flag[0], flag[1]);
            mMap.addMarker(new MarkerOptions().position(position).title("Flag").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
        }

    }

    public double rad(double x) {
        return x * Math.PI / 180;
    }

    public boolean checkFlagDistances(LatLng userLocation) {
        double R = 6378137; // Earth’s mean radius in meter
        for (double[] flag : arrFlags) {
            double dLat = rad(flag[0] - userLocation.latitude);
            double dLong = rad(flag[1] - userLocation.longitude);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(rad(userLocation.latitude)) * Math.cos(rad(flag[0])) *
                            Math.sin(dLong / 2) * Math.sin(dLong / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double d = R * c;

            if (d < 10) {
                Toast.makeText(MapsActivity.this, "Collected flag" + d, Toast.LENGTH_LONG).show();
                return true;
            }
        }

        return false;
    }
}
