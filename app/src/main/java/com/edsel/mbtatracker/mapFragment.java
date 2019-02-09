/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.edsel.mbtatracker;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class mapFragment extends Fragment implements
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback{

    private View rootView;
    MapView mapView;
    private GoogleMap googleMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    private CameraPosition mCameraPosition;
    private static final int DEFAULT_ZOOM = 14;
    private Location mLastKnownLocation;
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;
    boolean firstRender;
    private Button searchAreaBt;
    private ArrayList<mbtaStops> stopsList;
    private FloatingActionButton fab;
    private mbtaStops currentStop = null;
    ImageButton minimizeBt;
    ImageButton maximizeBt;
    CardView mapCardView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        rootView = inflater.inflate(R.layout.map_fragment_layout, container, false);
        setRetainInstance(true);
        firstRender = true;

        mapView = (MapView) rootView.findViewById(R.id.the_map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(rootView.getContext());


        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mapView.getMapAsync(this);

        stopsList = new ArrayList<mbtaStops>();

        searchAreaBt = (Button)rootView.findViewById(R.id.searchThisArea);
        searchAreaBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               LatLng camPos = googleMap.getCameraPosition().target;

//                Toast.makeText(rootView.getContext(),
//                        "Current Camera location: "+ camPos.latitude + ", " + camPos.longitude,
//                        Toast.LENGTH_SHORT).show();

                new grabStops(camPos).execute();
            }
        });

        fab = (FloatingActionButton) rootView.findViewById(R.id.floatingBt);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(currentStop != null){
                    new addStop(rootView.getContext()).execute(currentStop);
                    Toast.makeText(rootView.getContext(),
                        "Subscribed to Stop: " + currentStop.getName(),
                        Toast.LENGTH_SHORT).show();
                }
            }
        });

        maximizeBt = (ImageButton)rootView.findViewById(R.id.maximizeBt);
        minimizeBt = (ImageButton) rootView.findViewById(R.id.minimizeBt);
        mapCardView = (CardView) rootView.findViewById(R.id.mapCardview);

        minimizeBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapCardView.setVisibility(View.GONE);
                maximizeBt.setVisibility(View.VISIBLE);
            }
        });

        maximizeBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapCardView.setVisibility(View.VISIBLE);
                maximizeBt.setVisibility(View.GONE);
            }
        });

        return rootView;
    }


    @Override
    public void onMapReady(GoogleMap mMap) {
        googleMap = mMap;

        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableMyLocation();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(rootView.getContext(),R.raw.style_json));
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // Retrieve the data from the marker.
                currentStop = (mbtaStops) marker.getTag();

                TextView nameField = (TextView)rootView.findViewById(R.id.nameField);
                TextView dirField = (TextView)rootView.findViewById(R.id.directionField);
                TextView etaField = (TextView)rootView.findViewById(R.id.etaField);
                TextView nextField = (TextView) rootView.findViewById(R.id.nextField);

                nameField.setText("" + currentStop.getName());
                dirField.setText("Direction: " + currentStop.getPlatform());

                new grabETA(etaField, nextField, currentStop.getId()).execute();
                new grabColour(nameField).execute();

                // Return false to indicate that we have not consumed the event and that we wish
                // for the default behavior to occur (which is for the camera to move such that the
                // marker is centered and for the marker's info window to open, if it has one).
                return false;
            }
        });

        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                Intent map = new Intent(rootView.getContext(), stopInfoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("stop", currentStop);
                map.putExtras(bundle);
                rootView.getContext().startActivity(map);
            }
        });
    }

    private void updateLocationUI() {
        if (googleMap == null) {
            return;
        }
        try {
            if (!mPermissionDenied) {
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                googleMap.setMyLocationEnabled(false);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (!mPermissionDenied) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            LatLng location = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());

                            if(firstRender){

                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        location, DEFAULT_ZOOM));
                                firstRender = false;
                            }
                            else{

                                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, DEFAULT_ZOOM));
                            }

                            new grabStops(location).execute();

//                            Toast.makeText(rootView.getContext(),
//                                    "Current device location: "+ mLastKnownLocation.getLatitude() + ", " + mLastKnownLocation.getLongitude(),
//                                    Toast.LENGTH_SHORT).show();

                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            googleMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(rootView.getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission((AppCompatActivity) rootView.getContext(), LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);

        } else if (googleMap != null) {
            // Access to the location has been granted to the app.
            googleMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        //Toast.makeText(rootView.getContext(), "My Location button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        getDeviceLocation();
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        //Toast.makeText(rootView.getContext(), "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }

        updateLocationUI();
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getFragmentManager(), "dialog");
    }

    public class grabStops extends AsyncTask<String, Integer, String> {

        private LatLng currentLocation;

        public grabStops(LatLng currentLocation){
            this.currentLocation = currentLocation;
        }


        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String mbtaJSON = null;

            try{
                final String MBTA_URL_API = "https://api-v3.mbta.com/stops?" +
                        "filter[radius]=0.03&filter[route_type]=0,1";

                Uri buildURI = Uri.parse(MBTA_URL_API).buildUpon()
                        .appendQueryParameter("filter[latitude]", Double.toString(currentLocation.latitude))
                        .appendQueryParameter("filter[longitude]", Double.toString(currentLocation.longitude))
                        .appendQueryParameter("api_key", "40b53b82791340e692ec1230aa8d3dc3")
                        .build();

                URL requestURL = new URL(buildURI.toString());

                urlConnection = (HttpURLConnection) requestURL.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();

                StringBuilder builder = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;

                while((line = reader.readLine()) != null){

                    builder.append(line + "\n");
                    publishProgress();
                }

                if(builder.length() == 0){
                    return null;
                }

                mbtaJSON = builder.toString();

            }catch(IOException e){
                e.printStackTrace();
            }finally{

                if(urlConnection != null){
                    urlConnection.disconnect();
                }
                if(reader != null){
                    try{
                        reader.close();
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }

            return mbtaJSON;
        }

        @Override
        protected void onPostExecute(String s){
            super.onPostExecute(s);

            stopsList = new ArrayList<mbtaStops>();

            try{

                JSONObject jsonObject = new JSONObject(s);
                JSONArray jsonArray = jsonObject.getJSONArray("data");

                int i = 0;

                String id = null;
                String name = null;
                String description = null;
                String platform = null;
                String parentId = null;
                Double lat = null;
                Double lon = null;

                while(i < jsonArray.length()){

                    JSONObject stop = jsonArray.getJSONObject(i);
                    JSONObject attributes = stop.getJSONObject("attributes");

                    try {
                        id = stop.getString("id");
                        name = attributes.getString("name");
                        description = attributes.getString("description");
                        platform = attributes.getString("platform_name");

                        if (stop.getJSONObject("relationships").has("parent_station")) {
                            parentId = stop.getJSONObject("relationships")
                                    .getJSONObject("parent_station")
                                    .getJSONObject("data")
                                    .getString("id");
                        }

                        lat = attributes.getDouble("latitude");
                        lon = attributes.getDouble("longitude");

                        stopsList.add(new mbtaStops(id, name, description, platform, parentId, lat, lon));
                    }catch(Exception e){
                        e.printStackTrace();
                    }

                    Marker stopMarker =
                            googleMap.addMarker( new MarkerOptions()
                                    .position(new LatLng(lat,lon))
                                    .title(name)
                                    .snippet("Direction: " + platform));
                    stopMarker.setTag(stopsList.get(i));
                    dropMarker(stopMarker,googleMap);
                    i++;
                }
            }catch(Exception e){
                e.printStackTrace();
            }

        }

    }


    public class grabColour extends AsyncTask<String, Integer, String> {

        private TextView nameField;
        private String stopID;
        private String colour =null;

        public grabColour(TextView nameField){
            this.nameField = nameField;
            this.stopID = currentStop.getId();
        }

        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String mbtaJSON = null;

            try{
                final String MBTA_URL_API = "https://api-v3.mbta.com/routes?filter[type]=0,1";

                Uri buildURI = Uri.parse(MBTA_URL_API).buildUpon()
                        .appendQueryParameter("filter[stop]", stopID)
                        .appendQueryParameter("api_key", "40b53b82791340e692ec1230aa8d3dc3")
                        .build();

                URL requestURL = new URL(buildURI.toString());

                urlConnection = (HttpURLConnection) requestURL.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();

                StringBuilder builder = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;

                while((line = reader.readLine()) != null){

                    builder.append(line + "\n");
                    publishProgress();
                }

                if(builder.length() == 0){
                    return null;
                }

                mbtaJSON = builder.toString();

            }catch(IOException e){
                e.printStackTrace();
            }finally{

                if(urlConnection != null){
                    urlConnection.disconnect();
                }
                if(reader != null){
                    try{
                        reader.close();
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }

            return mbtaJSON;
        }

        @Override
        protected void onPostExecute(String s){
            super.onPostExecute(s);

            try{

                JSONObject jsonObject = new JSONObject(s);
                JSONArray jsonArray = jsonObject.getJSONArray("data");

                int i = 0;

                while(i < jsonArray.length() || i < 3){

                    JSONObject stop = jsonArray.getJSONObject(i);
                    JSONObject attributes = stop.getJSONObject("attributes");

                    try {
                        colour = attributes.getString("color");

                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    i++;
                }
            }catch(Exception e){
                e.printStackTrace();
            }

            currentStop.setColour("#" + colour);
            nameField.setBackgroundColor(Color.parseColor(currentStop.getColour()));
        }

    }

    void dropMarker(final Marker marker, GoogleMap map) {
        final LatLng finalPosition = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);

        Projection projection = map.getProjection();
        Point startPoint = projection.toScreenLocation(finalPosition);
        startPoint.y = 0;
        final LatLng startLatLng = projection.fromScreenLocation(startPoint);
        final Interpolator interpolator = new BounceInterpolator(0.11, 4.6);

        TypeEvaluator<LatLng> typeEvaluator = new TypeEvaluator<LatLng>() {
            @Override
            public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
                float t = interpolator.getInterpolation(fraction);
                double lng = t * finalPosition.longitude + (1 - t) * startLatLng.longitude;
                double lat = t * finalPosition.latitude + (1 - t) * startLatLng.latitude;
                return new LatLng(lat, lng);
            }
        };

        Property<Marker, LatLng> property = Property.of(Marker.class, LatLng.class, "position");
        ObjectAnimator animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, finalPosition);
        animator.setDuration(400);
        animator.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


}
