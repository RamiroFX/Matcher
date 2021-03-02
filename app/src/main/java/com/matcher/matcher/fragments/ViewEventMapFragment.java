package com.matcher.matcher.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.matcher.matcher.R;
import com.matcher.matcher.entities.EventGroup;
import com.matcher.matcher.interfaces.GroupEventMapListener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ViewEventMapFragment extends Fragment implements OnMapReadyCallback {


    public interface OnViewEventMapFragmentInteractionListener {
        void onViewMapFragmentInteraction();
    }

    private static final String TAG = "ViewEventMapFragment";
    private OnViewEventMapFragmentInteractionListener mListener;
    private GoogleMap mMap;
    private HashMap<String, Marker> mMarkers;

    public ViewEventMapFragment() {
    }

    public static ViewEventMapFragment newInstance(String param1, String param2) {
        return new ViewEventMapFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mMarkers = new HashMap<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.fragment_view_event_map, container, false);
        v.setId(R.id.view_event_frag_bot);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /*
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.event_map);
        mapFragment.getMapAsync(this);
         */
        MapFragment mapFragment = (MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.fragment_view_event_map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach: " + context);
        Log.d(TAG, "getTag: " + getTag());
        if (context instanceof OnViewEventMapFragmentInteractionListener) {
            mListener = (OnViewEventMapFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public GoogleMap getmMap() {
        return mMap;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        mMap = googleMap;
        /*
        //ask for permissions..
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);*/
        mListener.onViewMapFragmentInteraction();
    }

    public void updateMap(EventGroup eventGroup) {
        LatLng eventLatLng = new LatLng(eventGroup.getLatitude(), eventGroup.getLongitude());
        mMap.addMarker(new MarkerOptions().position(eventLatLng).title(eventGroup.getEventName()));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(eventLatLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(eventLatLng, 12.0f));
    }

    public void removeAllMarker() {
        Log.d(TAG, "removeAllMarker");
        Iterator it = mMarkers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Marker> pair = (Map.Entry) it.next();
            pair.getValue().remove();
            it.remove(); // avoids a ConcurrentModificationException
        }
    }

    public void addMarker(String uid, MarkerOptions markerOptions) {
        Log.d(TAG, "addMarker: " + markerOptions);
        /*MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title(marker.getTitle());
        markerOptions.position(marker.getPosition());*/
        Marker marker = mMap.addMarker(markerOptions);
        mMarkers.put(uid, marker);
    }

    public void removeMarker(String uid) {
        if (mMarkers.containsKey(uid)) {
            mMarkers.get(uid).remove();
            mMarkers.remove(uid);
        }
    }

    public void updateMarker(String uid, LatLng latLng) {
        if (mMarkers.containsKey(uid)) {
            mMarkers.get(uid).setPosition(latLng);
        }
    }

    public boolean isMakerPresent(String uid) {
        return mMarkers.containsKey(uid);
    }
}
