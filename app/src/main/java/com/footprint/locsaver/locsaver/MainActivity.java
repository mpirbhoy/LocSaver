package com.footprint.locsaver.locsaver;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity
        implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleApiClient mGoogleClient;
    private Location mLastLocation;
    private TextView mLatitudeText;
    private TextView mLongitudeText;
    private LocationRequest mLocationRequest;
    private TextView mLastUpdateTime;
    private RequestQueue requestQueue;
    private static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "", Snackbar.LENGTH_LONG)
                        .setAction("Add footprint!", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                try {
                                    addFootprint(view);
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            }

                        }).show();
            }
        });

        //Initializing TextViews
        mLatitudeText = (TextView) findViewById(R.id.latView);
        mLongitudeText = (TextView) findViewById(R.id.longView);
        mLastUpdateTime = (TextView) findViewById(R.id.lastUpdateTimeView);


        //Initializing Google Client which provides location info
        mGoogleClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        //Initializing requestQueue for sending http requests
        requestQueue = Volley.newRequestQueue(getApplicationContext());

    }

    @Override
    protected void onStart() {
        if (mGoogleClient != null) {
            mGoogleClient.connect();
        }

        createLocationRequest();
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.
                // PERMISSION_REQUEST_ACCESS_FINE_LOCATION can be any unique int
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
            }
        }
    }

    @Override
    protected void onStop() {
        if (mGoogleClient != null) {
            mGoogleClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleClient);
            if (mLastLocation != null) {
                mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
                mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
            } else {
                System.out.println("Last location value is initially null!");
            }
        }
        catch (SecurityException e) {
            System.out.println("Security Exception 1!");
        }

        startLocationUpdates();

    }

    //(Called in OnStart)
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        //
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleClient,
                        builder.build());

    }

    //(Called in OnConnected after getting last location)
    protected void startLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleClient, mLocationRequest, this);
        } catch (SecurityException e) {
            System.out.println("Security Exception 2!");
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        System.out.println("Connection suspended!");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        System.out.println("Connection failed!");
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
        mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
        mLastUpdateTime.setText(DateFormat.getTimeInstance().format(new Date()));
    }

    //Going to Map Activity
    public void goToMapActivity(View view) {

        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("latitude", mLatitudeText.getText());
        intent.putExtra("longitude", mLongitudeText.getText());
        System.out.println(mLatitudeText.getText());
        System.out.println(mLongitudeText.getText());
        startActivity(intent);

    }

    //Add Footprint
    public void addFootprint(View view) throws UnsupportedEncodingException {
        final Float lat = Float.parseFloat(mLatitudeText.getText().toString());
        final Float lon = Float.parseFloat(mLongitudeText.getText().toString());
        final String URL = "https://foot-print.herokuapp.com/footprintAnon";

        if (lat != 0 && lon != 0) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Toast.makeText(MainActivity.this,response,Toast.LENGTH_LONG).show();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(MainActivity.this,error.toString(),Toast.LENGTH_LONG).show();
                        }
                    }){
                @Override
                protected Map<String,String> getParams(){
                    Map<String,String> params = new HashMap<String, String>();
                    params.put("lat", lat.toString());
                    params.put("lon" , lon.toString());
                    return params;
                }

            };

            requestQueue.add(stringRequest);

        } else {
            Toast.makeText(MainActivity.this, "Can't access current location",
                    Toast.LENGTH_LONG).show();
        }


    }

    //Getting location manually
    public void getLocationManual(View view) {
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleClient);
            if (mLastLocation != null) {
                mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
                mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
            } else {
                System.out.println("Last location value is still null!");
            }
        }
        catch (SecurityException e) {
            System.out.println("Security Exception 3!");
        }



    }

    //Delete all footprints
    public void delAllFootprints(View view) {
        System.out.println("hey");
        String URL = "https://foot-print.herokuapp.com/allFootprints";
        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(MainActivity.this,response,Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this,error.toString(),Toast.LENGTH_LONG).show();
                    }
                });

        //Add it to the requestqueue
        requestQueue.add(stringRequest);
    }

    //Called when don't have permissions
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
