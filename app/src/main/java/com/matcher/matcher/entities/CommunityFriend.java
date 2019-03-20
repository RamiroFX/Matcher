package com.matcher.matcher.entities;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ServerValue;
import com.matcher.matcher.Utils.DBContract;

import org.json.JSONObject;

public class CommunityFriend extends Friend {

    private Double latitude, longitude, distFromUser;

    public CommunityFriend() {
        super();
    }

    public CommunityFriend(String uid, String alias, double latitude, double longitude) {
        super(uid, alias);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public CommunityFriend(DataSnapshot dataSnapshot) {
        setUid(dataSnapshot.getKey());
        setFullName(dataSnapshot.child(DBContract.UserTable.COL_NAME_FULLNAME).getValue(String.class));
        setLatitude(dataSnapshot.child(DBContract.UserTable.COL_NAME_LATITUDE).getValue(Double.class));
        setLongitude(dataSnapshot.child(DBContract.UserTable.COL_NAME_LONGITUDE).getValue(Double.class));
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getDistanceFromUser() {
        return distFromUser;
    }

    public void setDistanceFromUser(Double distFromUser) {
        this.distFromUser = distFromUser;
    }

    /*public String toJsonString() {
        return "{" +
                "\"" + DBContract.UserTable.COL_NAME_NICKNAME + "\": \"" + getFullName() + "\"" +
                ", \"" + DBContract.UserTable.COL_NAME_LATITUDE + "\": " + getLatitude() + "" +
                ", \"" + DBContract.UserTable.COL_NAME_LONGITUDE + "\": " + getLongitude() + "" +
                ", \"" + DBContract.UserTable.COL_NAME_TIMESTAMP + "\": " + ServerValue.TIMESTAMP + "" +
                "}";
    }*/

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        CommunityFriend itemCompare = (CommunityFriend) obj;
        if (itemCompare.getUid().equals(this.getUid()))
            return true;

        return false;
    }
}
