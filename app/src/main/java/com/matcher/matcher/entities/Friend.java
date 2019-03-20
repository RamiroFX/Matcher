package com.matcher.matcher.entities;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;
import com.matcher.matcher.Utils.DBContract;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Friend {
    @NonNull
    String fullName;
    @NonNull
    String uid;

    public Friend() {
    }

    public Friend(DataSnapshot ds) {
        try {
            JSONObject eventDetail = new JSONObject(ds.getValue() + "");
            String uid = ds.getKey();
            String name = eventDetail.getString(DBContract.ChallengeTable.COL_NAME_CHALLENGER);
            setUid(uid);
            setFullName(name);
        } catch (Throwable t) {
            Log.e("Friend", "Could not parse malformed JSON: \"" + ds.getValue() + "\"");
        }
    }

    public Friend(@NonNull String uid, String fullName) {
        this.fullName = fullName;
        this.uid = uid;
    }

    @NonNull
    public String getFullName() {
        return fullName;
    }

    public void setFullName(@NonNull String fullName) {
        this.fullName = fullName;
    }

    @NonNull
    public String getUid() {
        return uid;
    }

    public void setUid(@NonNull String uid) {
        this.uid = uid;
    }


    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(DBContract.UserTable.COL_NAME_UID, getUid());
        result.put(DBContract.UserTable.COL_NAME_FULLNAME, getFullName());
        return result;
    }

    @Override
    public String toString() {
        return "friend:{" +
                DBContract.UserTable.COL_NAME_UID + ": " + getUid() +
                ", " + DBContract.UserTable.COL_NAME_FULLNAME + ": " + getFullName() +
                "}";
    }

    public String toJsonString() {
        return "{" +
                "\"" + DBContract.UserTable.COL_NAME_UID + "\": \"" + getUid() + "\"" +
                ", \"" + DBContract.UserTable.COL_NAME_FULLNAME + "\": \"" + getFullName() + "\"" +
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
}