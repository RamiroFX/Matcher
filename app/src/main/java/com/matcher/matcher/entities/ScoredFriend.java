package com.matcher.matcher.entities;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.matcher.matcher.Utils.DBContract;

public class ScoredFriend extends Friend {

    private int score;

    public ScoredFriend() {
    }

    public ScoredFriend(@NonNull String uid, String fullName, int score) {
        super(uid, fullName);
        this.score = score;
    }

    public ScoredFriend(DataSnapshot ds, int score) {
        super(ds);
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "ScoredFriend{" +
                DBContract.UserTable.COL_NAME_UID + ": " + getUid() + ", " +
                DBContract.UserTable.COL_NAME_FULLNAME + ": " + getFullName() + ", " +
                DBContract.UserTable.COL_NAME_SCORE + ": " + getScore() +
                "}";
    }
}
