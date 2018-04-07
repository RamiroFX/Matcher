package com.matcher.matcher.entities;
import android.support.annotation.NonNull;

public class Friend {
    @NonNull String username;
    @NonNull String uid;

    public Friend() {
    }

    public Friend(@NonNull String username, String uid) {
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
}