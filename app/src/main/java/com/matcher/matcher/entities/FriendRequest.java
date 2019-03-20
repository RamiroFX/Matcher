package com.matcher.matcher.entities;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;

public class FriendRequest {

    private static final String TAG = "FriendRequest";
    private String uid, fullName;

    public FriendRequest() {
    }

    public FriendRequest(DataSnapshot ds) {
        String uid = ds.getKey();
        String name = ds.getValue() + "";
        setUid(uid);
        setFullName(name);
    }

    public FriendRequest(String uid, String name) {
        this.uid = uid;
        this.fullName = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String name) {
        this.fullName = name;
    }

    @Override
    public String toString() {
        return "FriendRequest{" +
                "uid= " + getUid() +
                ", name= " + getFullName() +
                "}";
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        FriendRequest itemCompare = (FriendRequest) obj;
        if (itemCompare.getUid().equals(this.getUid()))
            return true;

        return false;
    }
}
