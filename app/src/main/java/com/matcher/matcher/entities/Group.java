package com.matcher.matcher.entities;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;
import com.matcher.matcher.Utils.DBContract;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Group {
    private String uid;
    private String ownerUid;
    private String ownerName;
    private double longitude;
    private double latitude;
    private String name;
    private String description;
    @Exclude
    private Map<String, String> groupMembers;

    public Group() {
        groupMembers = new HashMap<>();
    }

    public Group(DataSnapshot dataSnapshot) {
        try {
            JSONObject eventDetail = new JSONObject(dataSnapshot.getValue() + "");
            String groupUID = dataSnapshot.getKey();
            String groupName = eventDetail.getString(DBContract.GroupTable.COL_NAME_NAME);
            setUid(groupUID);
            setGroupName(groupName);
        } catch (Throwable t) {
            Log.e("Group", "Could not parse malformed JSON: \"" + dataSnapshot.getValue() + "\"");
        }
    }

    public Group(String uid, String ownerUid, String ownerName, String groupName, String groupDescription,
                 double latitude, double longitude) {
        this.uid = uid;
        this.ownerUid = ownerUid;
        this.ownerName = ownerName;
        this.name = groupName;
        this.description = groupDescription;
        this.latitude = latitude;
        this.longitude = longitude;
        this.groupMembers = new HashMap<>();
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

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
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

    public String getGroupName() {
        return name;
    }

    public void setGroupName(String groupName) {
        this.name = groupName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String groupDescription) {
        this.description = groupDescription;
    }

    @Exclude
    public Map<String, String> getGroupMembers() {
        return groupMembers;
    }

    @Exclude
    public void setGroupMembers(Map<String, String> groupMembers) {
        this.groupMembers = groupMembers;
    }

    @Exclude
    public void addMember(FriendRequest friend) {
        /*
        String value = "{" +
                "\"" + DBContract.EventsParticipantsTable.COL_NAME_PARTICIPANT_NAME + "\": " + friend.getFullName() +
                "}";*/
        this.groupMembers.put(friend.getUid(), friend.getFullName());
    }

    @Override
    public String toString() {
        return "Group{" +
                "uid = " + getUid() +
                ", name = " + getGroupName() +
                ", description = " + getDescription() +
                ", ownerUid = " + getOwnerUid() +
                ", ownerName = " + getOwnerName() +
                ", latitude = " + getLatitude() +
                ", longitude = " + getLongitude() +
                "}";
    }

    public String toJsonString() {
        return "{" +
                "\"" + DBContract.GroupTable.COL_NAME_NAME + "\": \"" + getGroupName() + "\"" +
                "}";
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        Group itemCompare = (Group) obj;
        if (itemCompare.getUid().equals(this.getUid()))
            return true;

        return false;
    }
}
