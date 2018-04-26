package com.matcher.matcher.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matcher.matcher.R;
import com.matcher.matcher.Utils.DBContract;
import com.matcher.matcher.entities.Event;

public class ViewEventActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "ViewEventActivity";
    private DatabaseReference databaseRef;
    private String eventUID;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);
        this.databaseRef = FirebaseDatabase.getInstance().getReference();
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){
            eventUID = bundle.getString(DBContract.EventsTable.COL_NAME_UID);
            Log.d(TAG,"uidEvent: "+eventUID);
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.event_map);
        mapFragment.getMapAsync(this);
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
        retrieveEventData();
        // Add a marker in Sydney and move the camera
    }

    private void retrieveEventData() {
        databaseRef.child(DBContract.EventsTable.TABLE_NAME).child(eventUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildAdded: " + dataSnapshot);
                Event event = dataSnapshot.getValue(Event.class);
                LatLng eventLatLng = new LatLng(event.getLatitude(), event.getLongitude());
                mMap.addMarker(new MarkerOptions().position(eventLatLng).title(event.getEventName()));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(eventLatLng));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(eventLatLng, 12.0f));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
