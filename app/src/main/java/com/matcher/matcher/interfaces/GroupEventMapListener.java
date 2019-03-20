package com.matcher.matcher.interfaces;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.matcher.matcher.entities.EventGroup;

public interface GroupEventMapListener {
    void addMarker(String uid, MarkerOptions markerOptions);

    void updateMarker(String uid, LatLng latLng);

    void removeMarker(String uid);

    void updateMap(EventGroup eventGroup);

    void removeAllMarker();

    boolean isMakerPresent(String uid);
}
