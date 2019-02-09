package com.edsel.mbtatracker;

import android.content.Intent;
import android.graphics.Color;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class stopInfoActivity  extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback{

    mbtaStops currentStop;
    private GoogleMap map;
    Geocoder geocoder = null;
    private LatLng latLng;
    private double lat,lon;

    TextView stopName;
    TextView stopDescription;
    TextView stopDirection;
    ImageView mbtaLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stop_info_xml);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Stop Information");
        actionBar.setDisplayHomeAsUpEnabled(true);

        geocoder = new Geocoder(this);

        // This fetches the addresses from a bundle and places them in an ArrayList
        // ArrayList will be used later by GeoCoder
        Intent arts = getIntent();
        Bundle bundle = arts.getExtras();

        currentStop = (mbtaStops)bundle.getSerializable("stop");

        lat = currentStop.getLat();
        lon = currentStop.getLon();

        stopName = (TextView)findViewById(R.id.stopInfoName);
        stopDescription = (TextView)findViewById(R.id.stopInfoDescription);
        stopDirection = (TextView)findViewById(R.id.stopInfoDirection);
        mbtaLogo = (ImageView)findViewById(R.id.imageView);

        stopName.setBackgroundColor(Color.parseColor(currentStop.getColour()));
        mbtaLogo.setBackgroundColor(Color.parseColor(currentStop.getColour()));
        stopName.setText(currentStop.getName());
        stopDescription.setText(currentStop.getDescription());
        stopDirection.setText("Direction: "+currentStop.getPlatform());

        //gets the maps to load
        MapFragment mf = (MapFragment) getFragmentManager().findFragmentById(R.id.stop_map);
        mf.getMapAsync(this);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //this will catch the <- arrow
        //and return to MainActivity
        //needed since we use fragments to map sites
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {    // map is loaded but not laid out yet
        this.map = map;
        map.setOnMapLoadedCallback(this);      // calls onMapLoaded when layout done
        UiSettings mapSettings;
        mapSettings = map.getUiSettings();
        mapSettings.setZoomControlsEnabled(true);
    }

    @Override
    public void onMapLoaded() {
        // code to run when the map has loaded
        latLng = new LatLng(lat,lon);
        Geocoder gcd = new Geocoder(this);

        // puts marker icon at location
        map.addMarker(new MarkerOptions()
                .position(latLng)
                .title(currentStop.getName())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
    }


}
