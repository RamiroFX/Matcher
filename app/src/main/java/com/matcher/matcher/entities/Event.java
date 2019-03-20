package com.matcher.matcher.entities;

import com.matcher.matcher.Utils.DBContract;

public class Event {
    private String uid;
    private String eventType;
    private String placeName;
    private double longitude;
    private double latitude;
    private long scheduledTime;

    public Event() {
    }

    public Event(String uid, String eventType, String placeName, double longitude, double latitude, long scheduledTime) {
        this.uid = uid;
        this.eventType = eventType;
        this.placeName = placeName;
        this.longitude = longitude;
        this.latitude = latitude;
        this.scheduledTime = scheduledTime;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public long getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(long scheduledTime) {
        this.scheduledTime = scheduledTime;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        Event itemCompare = (Event) obj;
        if (itemCompare.getUid().equals(this.getUid()))
            return true;

        return false;
    }

    @Override
    public String toString() {
        return "Event{" +
                DBContract.EventsTable.COL_NAME_UID + ":" + getUid() + "," +
                DBContract.EventsTable.COL_NAME_EVENT_TYPE + ":" + getEventType() + "," +
                DBContract.EventsTable.COL_NAME_PLACE_NAME + ":" + getPlaceName() + "," +
                DBContract.EventsTable.COL_NAME_LATITUDE + ":" + getLatitude() + "," +
                DBContract.EventsTable.COL_NAME_LONGITUDE + ":" + getLongitude() + "," +
                DBContract.EventsTable.COL_NAME_SCHEDULED_TIME + ":" + getScheduledTime() + "," +
                "}";
    }
}
