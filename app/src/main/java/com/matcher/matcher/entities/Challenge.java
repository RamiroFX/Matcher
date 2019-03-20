package com.matcher.matcher.entities;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;
import com.matcher.matcher.Utils.DBContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Challenge extends Event {
    private Friend challenger, challenged;
    private Sports sport;
    private String description;
    private String status;


    public Challenge() {
        this.challenged = new Friend();
        this.challenger = new Friend();
        this.sport = new Sports();
        setEventType(DBContract.ChallengeTable.COL_NAME_CHALLENGE_EVENT);
    }

    public Challenge(DataSnapshot dataSnapshot) {
        try {
            JSONObject jsonObject = new JSONObject(dataSnapshot.getValue() + "");
            String challengerName = jsonObject.getString(DBContract.ChallengeRequestsTable.COL_NAME_CHALLENGER);
            String challengerUid = jsonObject.getString(DBContract.ChallengeRequestsTable.COL_NAME_CHALLENGER_UID);
            String challengedName = jsonObject.getString(DBContract.ChallengeRequestsTable.COL_NAME_CHALLENGED);
            String challengedUid = jsonObject.getString(DBContract.ChallengeRequestsTable.COL_NAME_CHALLENGED_UID);
            String sport = jsonObject.getString(DBContract.ChallengeRequestsTable.COL_NAME_SPORT);
            String schedule = jsonObject.getString(DBContract.ChallengeRequestsTable.COL_NAME_SCHEDULED_TIME);
            setUid(dataSnapshot.getKey());
            setChallenger(new Friend(challengerUid, challengerName));
            setChallenged(new Friend(challengedUid, challengedName));
            setSport(new Sports(-1, sport));
            setScheduledTime(Long.valueOf(schedule));
            setEventType(DBContract.ChallengeTable.COL_NAME_CHALLENGE_EVENT);
        } catch (JSONException ignored) {
            Log.e("Challenge", "Could not parse malformed JSON: \"" + dataSnapshot.getValue() + "\"");
        }
    }

    public Challenge(String uid, Friend challenger, Friend challenged, Sports sport, String description, long scheduledTime, String placeName, double longitude, double latitude) {
        super(uid, DBContract.ChallengeTable.COL_NAME_CHALLENGE_EVENT, placeName, longitude, latitude, scheduledTime);
        this.challenger = challenger;
        this.challenged = challenged;
        this.sport = sport;
        this.description = description;
    }

    public Friend getChallenger() {
        return challenger;
    }

    public void setChallenger(Friend challenger) {
        this.challenger = challenger;
    }

    public Friend getChallenged() {
        return challenged;
    }

    public void setChallenged(Friend challenged) {
        this.challenged = challenged;
    }

    public Sports getSport() {
        return sport;
    }

    public void setSport(Sports sport) {
        this.sport = sport;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Exclude
    public String challengeDate() {
        return new SimpleDateFormat("dd/MM/yyyy").format(new Date(getScheduledTime()));
    }

    @Exclude
    public String challengeTime() {
        return new SimpleDateFormat("HH:mm").format(new Date(getScheduledTime()));
    }

    @Exclude
    public String challengeSchedule() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(getScheduledTime()));
    }

    @Override
    public String toString() {
        return "Challenge{" +
                "uid = " + getUid() +
                ", description = " + getDescription() +
                ", schedule = " + getScheduledTime() +
                ", challenger = " + getChallenger().getFullName() +
                ", challenged = " + getChallenged().getFullName() +
                ", placeName = " + getPlaceName() +
                ", placeLatitude = " + getLatitude() +
                ", placeLongitude = " + getLongitude() +
                "}";
    }

    public String toJsonString() {
        return "{" +
                "\"" + DBContract.ChallengeTable.COL_NAME_SPORT + "\": \"" + getSport().getName() + "\"" +
                ", \"" + DBContract.ChallengeTable.COL_NAME_EVENT_TYPE + "\": \"" + DBContract.ChallengeTable.COL_NAME_CHALLENGE_EVENT+ "\"" +
                ", \"" + DBContract.ChallengeTable.COL_NAME_CHALLENGER_UID + "\": \"" + getChallenger().getUid() + "\"" +
                ", \"" + DBContract.ChallengeTable.COL_NAME_CHALLENGER + "\": \"" + getChallenger().getFullName() + "\"" +
                ", \"" + DBContract.ChallengeTable.COL_NAME_CHALLENGED_UID + "\": \"" + getChallenged().getUid() + "\"" +
                ", \"" + DBContract.ChallengeTable.COL_NAME_CHALLENGED + "\": \"" + getChallenged().getFullName() + "\"" +
                ", \"" + DBContract.ChallengeTable.COL_NAME_SCHEDULED_TIME + "\": \"" + getScheduledTime() + "\"" +
                "}";
    }

/*
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        Challenge itemCompare = (Challenge) obj;
        if (itemCompare.getUid().equals(this.getUid()))
            return true;

        return false;
    }*/
}
