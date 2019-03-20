package com.matcher.matcher.activities;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.matcher.matcher.R;
import com.matcher.matcher.Utils.DBContract;

public class ViewChallengeMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private double latitude, longitude;
    private String placeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_view_challenge_map);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_view_challenge_map);
        mapFragment.getMapAsync(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            latitude = extras.getDouble(DBContract.EventsTable.COL_NAME_LATITUDE, 0);
            longitude = extras.getDouble(DBContract.EventsTable.COL_NAME_LONGITUDE, 0);
            placeName = extras.getString(DBContract.EventsTable.COL_NAME_PLACE_NAME, "");
        } else {
            finish();
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we
     * just add a marker near Africa.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        LatLng eventLatLng = new LatLng(latitude, longitude);
        map.addMarker(new MarkerOptions().position(eventLatLng).title(placeName));
        map.moveCamera(CameraUpdateFactory.newLatLng(eventLatLng));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(eventLatLng, 12.0f));
    }
}