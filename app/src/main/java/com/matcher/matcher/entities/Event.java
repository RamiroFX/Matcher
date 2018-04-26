package com.matcher.matcher.entities;


import com.google.firebase.database.Exclude;
import com.matcher.matcher.Utils.DBContract;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Event {
    private String uid;
    private String ownerUid;
    private double longitude;
    private double latitude;
    private long scheduledTime;
    private String eventName;
    private String description;
    private String placeName;
    @Exclude
    private Map<String, String> invitedFriends;
    /*private ArrayList<Long> confirmedGuests;
    private ArrayList<Long> invitedGuests;
    private ArrayList<Long> declinedGuests;*/

    public Event() {
        invitedFriends = new HashMap<>();
    }

    public Event(String uid, String ownerUid, long scheduledTime, String eventName,String eventDescription,
                 String placeName, double latitude, double longitude) {
        this.uid = uid;
        this.ownerUid = ownerUid;
        this.placeName = placeName;
        this.scheduledTime = scheduledTime;
        this.eventName = eventName;
        this.description = eventDescription;
        this.latitude = latitude;
        this.longitude = longitude;
        this.invitedFriends = new HashMap<>();
    }

    @Exclude
    public String getUid() {
        return uid;
    }

    @Exclude
    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getOwnerUid() {
        return ownerUid;
    }

    public void setOwnerUid(String ownerUid) {
        this.ownerUid = ownerUid;
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

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    @Exclude
    public Map<String, String> getInvitedFriends() {
        return invitedFriends;
    }

    @Exclude
    public void setInvitedFriends(Map<String, String> invitedFriends) {
        this.invitedFriends = invitedFriends;
    }

    @Exclude
    public void addFriends(Friend friend) {
        this.invitedFriends.put(friend.getUid(), friend.getUsername());
    }

    @Exclude
    public String eventDate() {
        return new SimpleDateFormat("dd/MM/yyyy").format(new Date(getScheduledTime()));
    }

    @Override
    public String toString() {
        return "Event{" +
                "uid = "+getUid()+
                ", eventName = "+getEventName()+
                ", eventDescription = "+getDescription()+
                ", eventSchedule = "+getScheduledTime()+
                ", ownerUid = "+getOwnerUid()+
                ", placeName = "+getPlaceName()+
                ", placeLatitude = "+getLatitude()+
                ", placeLongitude = "+getLongitude()+
                "}";
    }

    public String toJsonString() {
        return "{" +
                "\""+ DBContract.EventsTable.COL_NAME_NAME+"\": \"" + getEventName() + "\"" +
                ", \""+ DBContract.EventsTable.COL_NAME_SCHEDULED_TIME+"\": \"" + getScheduledTime() + "\"" +
                "}";
    }
}