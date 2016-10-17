package com.footprint.locsaver.locsaver;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

import static android.R.attr.data;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private float latitude = 0;
    private float longitude = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();

        latitude = Float.parseFloat(intent.getStringExtra("latitude"));
        longitude = Float.parseFloat(intent.getStringExtra("longitude"));
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
        mMap.setMinZoomPreference(6.0f);
        mMap.setMaxZoomPreference(30.0f);

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //LatLng currentPos = new LatLng(latitude, longitude);
        //mMap.addMarker(new MarkerOptions().position(currentPos).title("Current Marker"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPos));

        loadLocations();
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude))      // Sets the center of the map to location user
                .zoom(30)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        //mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        //TO DO: calculate center location and zoom level to get maximum number of footprints within
        //certain range limit
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16));

    }

    private void loadLocations() {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://foot-print.herokuapp.com/allFootprints";

        final GoogleMap map = mMap;
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        //Convert response to Json Object
                        try {
                            JSONArray responseArray = new JSONArray(response);
//                            Toast.makeText(MapsActivity.this, responseArray.toString(),
//                                    Toast.LENGTH_LONG).show();
                            for (int i = 0, size = responseArray.length(); i < size; i++) {
                                JSONObject footprint = responseArray.getJSONObject(i);
                                LatLng currentPos = new LatLng(BigDecimal.valueOf(footprint.getDouble("lat")).floatValue(), BigDecimal.valueOf(footprint.getDouble("lon")).floatValue());
                                String title = "Marker " + i;
                                if (footprint.has("content")) {
                                    title = footprint.getString("content");
                                }
                                map.addMarker(new MarkerOptions().position(currentPos).title(title));
                                map.moveCamera(CameraUpdateFactory.newLatLng(currentPos));
                                latitude = BigDecimal.valueOf(footprint.getDouble("lat")).floatValue();
                                longitude = BigDecimal.valueOf(footprint.getDouble("lon")).floatValue();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }



                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MapsActivity.this, error.toString(),
                        Toast.LENGTH_LONG).show();
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMap != null) {
            mMap.clear();
            loadLocations();
        }

    }
}
