package com.matcher.matcher.entities;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.matcher.matcher.Utils.DBContract;

public class EventParticipant extends Friend {

    private static final String TAG = "EventParticipant";

    private String status;
    private Double latitude, longitude;
    private long lastUpdated;


    public EventParticipant() {
        super();
    }

    public EventParticipant(@NonNull String uid, String username, String status, double latitude, double longitude, long lastUpdated) {
        super(uid, username);
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.lastUpdated = lastUpdated;
    }

    public EventParticipant(DataSnapshot snapshot) {
        setUid(snapshot.getKey());
        setFullName(snapshot.child(DBContract.EventsParticipantsTable.COL_NAME_PARTICIPANT_NAME).getValue(String.class));
        setStatus(snapshot.child(DBContract.EventsParticipantsTable.COL_NAME_PARTICIPANT_STATUS).getValue(String.class));
        setLatitude(snapshot.child(DBContract.EventsParticipantsTable.COL_NAME_PARTICIPANT_LATITUDE).getValue(Double.class));
        setLongitude(snapshot.child(DBContract.EventsParticipantsTable.COL_NAME_PARTICIPANT_LONGITUDE).getValue(Double.class));
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    @Override
    public String toString() {
        return "EventParticipant{" +
                "uid = " + getUid() +
                ", name = " + getFullName() +
                ", status = " + getStatus() +
                ", latitude = " + getLatitude() +
                ", longitude = " + getLongitude() +
                "}";
    }

    /*public String toJsonString() {
        return "{" +
                "\"" + DBContract.EventsParticipantsTable.COL_NAME_PARTICIPANT_NAME + "\": \"" + getFullName() + "\"" +
                ", \"" + DBContract.EventsParticipantsTable.COL_NAME_PARTICIPANT_STATUS + "\": \"" + getStatus() + "\"" +
                ", \"" + DBContract.EventsParticipantsTable.COL_NAME_PARTICIPANT_LATITUDE + "\": " + getLatitude() + "" +
                ", \"" + DBContract.EventsParticipantsTable.COL_NAME_PARTICIPANT_LONGITUDE + "\": " + getLongitude() + "" +
                "}";
    }*/


    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        EventParticipant itemCompare = (EventParticipant) obj;
        if (itemCompare.getUid().equals(this.getUid()))
            return true;

        return false;
    }
}
