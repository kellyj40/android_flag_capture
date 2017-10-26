//package com.joekelly.mapsandlocation;
//
//import android.Manifest;
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
//import android.os.Build;
//import android.support.v4.app.ActivityCompat;
//import android.os.Bundle;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;
//
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.SupportMapFragment;
//import com.google.android.gms.maps.model.BitmapDescriptor;
//import com.google.android.gms.maps.model.BitmapDescriptorFactory;
//import com.google.android.gms.maps.model.GroundOverlayOptions;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.Marker;
//import com.google.android.gms.maps.model.MarkerOptions;
//import java.util.ArrayList;
//
//public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
//
//    private GoogleMap mMap;
//
//    private double[][] arrFlags;
//
//    LocationManager locationManager;
//    LocationListener locationListener;
//    private Marker objectReference;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_maps);
//        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
//    }
//
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//
//        // Set the map
//        mMap = googleMap;
//        // Used for getting access to the systems location service
//        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
//
//
//
//
//        // Make flag request and plot onto the map
//        arrFlags = FlagRequest.requestFlags();
//
//        for (double[] flag : arrFlags) {
//            LatLng position = new LatLng(flag[0], flag[1]);
//            objectReference = mMap.addMarker(new MarkerOptions().position(position).title("Flag").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
//        }
//
//
//        // Add listener for GPS movement
//        locationListener = new LocationListener() {
//
//            public void onLocationChanged(Location location) {
//
//                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
//
//                // Calculate the distance to all the flags
//                int value = DistanceCalculations.checkFlagDistances(userLocation, arrFlags);
//
//                if (value>=0) {
//
//                    int radiusM = 100;
//
//                    double latitude = location.getLatitude();
//                    double longitude = location.getLongitude();
//                    LatLng latLng = new LatLng(latitude,longitude);
//
//                    // drawing circle
//                    int d = 500; // diameter
//                    Bitmap bm = Bitmap.createBitmap(d, d, Bitmap.Config.ARGB_8888);
//                    Canvas c = new Canvas(bm);
//                    Paint p = new Paint();
//                    p.setColor(Color.WHITE);
//
//                    c.drawCircle(d/2, d/2, d/2, p);
//
//                    // generate BitmapDescriptor from circle Bitmap
//                    BitmapDescriptor bmD = BitmapDescriptorFactory.fromBitmap(bm);
//
//                    //Add the circle
//                    mMap.addGroundOverlay(new GroundOverlayOptions().
//                            image(bmD).
//                            position(latLng,radiusM*2,radiusM*2).transparency(0.4f));
//
//                    // Remove the flag
//                    objectReference.remove();
//                }
//                // Keep camera on the user
//                mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
//            }
//
//            @Override
//            public void onStatusChanged(String s, int i, Bundle bundle) {
//
//            }
//
//            @Override
//            public void onProviderEnabled(String s) {
//
//            }
//
//            @Override
//            public void onProviderDisabled(String s) {
//
//            }
//
//        };
//
//        // Must be after the location listener is made
//        // Once the map is ready put the location onto the map
//        if (Build.VERSION.SDK_INT < 23) {
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
//        } else {
//
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != getPackageManager().PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
//            } else {
//                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
//
//                //Use this for when opening the map
//                Location lastKnownLocation = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
//
//                LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
//                // Move camera to the location of the user
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 17));
//                mMap.setMyLocationEnabled(true);
//            }
//
//        }
//
//    }
//
//}







package com.joekelly.mapsandlocation;
        import android.Manifest;
        import android.content.Context;
        import android.graphics.Bitmap;
        import android.graphics.Canvas;
        import android.graphics.Color;
        import android.graphics.Paint;
        import android.location.Location;
        import android.location.LocationListener;
        import android.location.LocationManager;
        import android.os.Build;
        import android.support.v4.app.ActivityCompat;
        import android.os.Bundle;
        import android.support.v4.content.ContextCompat;
        import android.support.v7.app.AppCompatActivity;
        import com.google.android.gms.maps.CameraUpdateFactory;
        import com.google.android.gms.maps.GoogleMap;
        import com.google.android.gms.maps.OnMapReadyCallback;
        import com.google.android.gms.maps.SupportMapFragment;
        import com.google.android.gms.maps.model.BitmapDescriptor;
        import com.google.android.gms.maps.model.BitmapDescriptorFactory;
        import com.google.android.gms.maps.model.GroundOverlayOptions;
        import com.google.android.gms.maps.model.LatLng;
        import com.google.android.gms.maps.model.Marker;
        import com.google.android.gms.maps.model.MarkerOptions;
        import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private double[][] arrFlags;

    LocationManager locationManager;
    LocationListener locationListener;
    private Marker objectReference;


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
    public void onMapReady(GoogleMap googleMap) {

        // Set the map
        mMap = googleMap;
        // Used for getting access to the systems location service
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);


        // Make flag request and plot onto the map
        arrFlags = FlagRequest.requestFlags();
        int countFlags=0;
        double[] flag;
//        objectReference = new Marker[arrFlags.length];
        while(countFlags<arrFlags.length){
            flag = arrFlags[countFlags];
            LatLng position = new LatLng(flag[0], flag[1]);

            objectReference = mMap.addMarker(new MarkerOptions().position(position).title("Flag").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
            countFlags++;
        }


        // Add listener for GPS movement
        locationListener = new LocationListener() {

            public void onLocationChanged(Location location) {

                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

                // Calculate the distance to all the flags
                int value = DistanceCalculations.checkFlagDistances(userLocation, arrFlags);


                if (value>=0) {

                    int radiusM = 100;

                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    LatLng latLng = new LatLng(latitude,longitude);

                    // drawing circle
                    int d = 500; // diameter
                    Bitmap bm = Bitmap.createBitmap(d, d, Bitmap.Config.ARGB_8888);
                    Canvas c = new Canvas(bm);
                    Paint p = new Paint();
                    p.setColor(Color.WHITE);

                    c.drawCircle(d/2, d/2, d/2, p);

                    // generate BitmapDescriptor from circle Bitmap
                    BitmapDescriptor bmD = BitmapDescriptorFactory.fromBitmap(bm);

                    //Add the circle
                    mMap.addGroundOverlay(new GroundOverlayOptions().
                            image(bmD).
                            position(latLng,radiusM*2,radiusM*2).transparency(0.4f));

                    // Remove the flag
                    objectReference.remove();

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
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } else {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != getPackageManager().PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                //Use this for when opening the map
                Location lastKnownLocation = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);

                LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                // Move camera to the location of the user
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 17));
                mMap.setMyLocationEnabled(true);
            }

        }

    }

}


//    @Override //Dont know if really need this?
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        // Ask for permission to get the users location
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if (requestCode == 1) {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != getPackageManager().PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
//            } else {
//                if (grantResults.length > 0 && grantResults[0] == getPackageManager().PERMISSION_GRANTED) {
//                    locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, locationListener);
//                    mMap.setMyLocationEnabled(true);
//                }
//            }
//        }
//
//    }
