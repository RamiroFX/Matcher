package com.matcher.matcher.entities;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;
import com.matcher.matcher.Utils.DBContract;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EventGroup extends Event {

    //private String ownerUid;
    //private String ownerName;
    private Friend owner;
    private String eventName;
    private String description;
    @Exclude
    private Map<String, EventParticipant> invitedFriends;

    public EventGroup() {
        owner = new Friend();
        invitedFriends = new HashMap<>();
        setEventType(DBContract.EventsTable.COL_NAME_GROUP_EVENT);
    }

    public EventGroup(DataSnapshot dataSnapshot) {
        /*try {
            JSONObject eventDetail = new JSONObject(dataSnapshot.getValue() + "");
            String eventUID = dataSnapshot.getKey();
            String eventName = eventDetail.getString(DBContract.EventsTable.COL_NAME_NAME);
            String eventSchedule = eventDetail.getString(DBContract.EventsTable.COL_NAME_SCHEDULED_TIME);
            setUid(eventUID);
            setEventName(eventName);
            setScheduledTime(Long.valueOf(eventSchedule));
            setEventType(DBContract.EventsTable.COL_NAME_GROUP_EVENT);
        } catch (Throwable t) {
            Log.e("EventGroup", "Could not parse malformed JSON: \"" + dataSnapshot.getValue() + "\"");
        }*/
    }

    public EventGroup(String uid, String ownerUid, String ownerName, long scheduledTime, String eventName, String eventDescription,
                      String placeName, double latitude, double longitude) {
        super(uid, DBContract.EventsTable.COL_NAME_GROUP_EVENT, placeName, longitude, latitude, scheduledTime);
        //this.ownerUid = ownerUid;
        //this.ownerName = ownerName;
        this.owner = new Friend(ownerUid, ownerName);
        this.eventName = eventName;
        this.description = eventDescription;
        this.invitedFriends = new HashMap<>();
    }

    public Friend getOwner() {
        return owner;
    }

    public void setOwner(Friend owner) {
        this.owner = owner;
    }

    /*
        public String getOwnerUid() {
            return ownerUid;
        }

        public String getOwnerName() {
            return ownerName;
        }

        public void setOwnerName(String ownerName) {
            this.ownerName = ownerName;
        }

        public void setOwnerUid(String ownerUid) {
            this.ownerUid = ownerUid;
        }
    */
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

    @Exclude
    public Map<String, EventParticipant> getInvitedFriends() {
        return invitedFriends;
    }

    @Exclude
    public void setInvitedFriends(Map<String, EventParticipant> invitedFriends) {
        this.invitedFriends = invitedFriends;
    }

    @Exclude
    public void addParticipant(EventParticipant eventParticipant) {
        String uid = eventParticipant.getUid();
        eventParticipant.setUid(null);
        this.invitedFriends.put(uid, eventParticipant);
    }

    @Exclude
    public String eventDate() {
        return new SimpleDateFormat("dd/MM/yyyy").format(new Date(getScheduledTime()));
    }

    @Exclude
    public String eventTime() {
        return new SimpleDateFormat("HH:mm").format(new Date(getScheduledTime()));
    }

    @Exclude
    public String eventSchedule() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(getScheduledTime()));
    }

    @Override
    public String toString() {
        return "EventGroup{" +
                "uid = " + getUid() +
                ", eventName = " + getEventName() +
                ", eventDescription = " + getDescription() +
                ", eventSchedule = " + getScheduledTime() +
                //", ownerUid = " + getOwnerUid() +
                //", ownerName = " + getOwnerName() +
                ", placeName = " + getPlaceName() +
                ", placeLatitude = " + getLatitude() +
                ", placeLongitude = " + getLongitude() +
                "}";
    }

    public String toJsonString() {
        return "{" +
                "\"" + DBContract.EventsTable.COL_NAME_NAME + "\": \"" + getEventName() + "\"" +
                ", \"" + DBContract.EventsTable.COL_NAME_SCHEDULED_TIME + "\": \"" + getScheduledTime() + "\"" +
                ", \"" + DBContract.EventsTable.COL_NAME_EVENT_TYPE + "\": \"" + DBContract.EventsTable.COL_NAME_GROUP_EVENT + "\"" +
                "}";
    }

/*
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        EventGroup itemCompare = (EventGroup) obj;
        if (itemCompare.getUid().equals(this.getUid()))
            return true;

        return false;
    }*/
}