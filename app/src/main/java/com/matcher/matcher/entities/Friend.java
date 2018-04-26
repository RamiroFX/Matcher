package com.matcher.matcher.entities;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Friend {
    @NonNull
    String username;
    @NonNull
    String uid;

    public Friend() {
    }

    public Friend(@NonNull String uid, String username) {
        this.username = username;
        this.uid = uid;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
    }

    @NonNull
    public String getUid() {
        return uid;
    }

    public void setUid(@NonNull String uid) {
        this.uid = uid;
    }

    @Override
    public String toString() {
        return "friend:{" +
                "uid: " + getUid() +
                ", fullName: " + getUsername() +
                "}";
    }

    public String toJsonString() {
        return "{" +
                "\"uid\": \"" + getUid() + "\"" +
                ", \"fullName\": \"" + getUsername() + "\"" +
                "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        Friend itemCompare = (Friend) obj;
        if (itemCompare.getUid().equals(this.getUid()))
            return true;

        return false;
    }

    public Map<String, Object> mapFriends(ArrayList<String> friendList) {
        HashMap<String, Object> result = new HashMap<>();
        for (String friendUID : friendList) {
            result.put(friendUID, true);
        }
        return result;
    }
}