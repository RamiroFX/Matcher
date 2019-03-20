package com.matcher.matcher.entities;

/**
 * Created by Ramiro on 27/02/2018.
 */

import com.google.firebase.database.Exclude;
import com.matcher.matcher.Utils.DBContract;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Chat {
    private Friend friend;
    private String message;
    private Long timeStamp;

    public Chat() {
    }

    public Chat(Friend friend, String message) {
        this.friend = friend;
        this.message = message;
        this.timeStamp = new Date().getTime();
    }

    public Chat(Friend friend, String message, Long timeStamp) {
        this.friend = friend;
        this.message = message;
        this.timeStamp = timeStamp;
    }

    public Friend getFriend() {
        return friend;
    }

    public void setFriend(Friend friend) {
        this.friend = friend;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getFormatedDate() {
        return new SimpleDateFormat("dd/MM/yyyy").format(new Date(getTimeStamp()));
    }

    public String getFormatedTimestamp() {
        return new SimpleDateFormat("hh:mm MM/dd/yyyy").format(new Date(getTimeStamp()));
    }

    public String getFormatedTime() {
        return new SimpleDateFormat("hh:mm").format(new Date(getTimeStamp()));
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(DBContract.MessageTable.COL_NAME_USER, friend);
        result.put(DBContract.MessageTable.COL_NAME_MESSAGE, message);
        result.put(DBContract.MessageTable.COL_NAME_TIMESTAMP, timeStamp);
        return result;
    }

    @Override
    public String toString() {
        return "chat{" +
                "user: " + getFriend() +
                " ,message: " + getMessage() +
                " ,timeStamp: " + getTimeStamp() +
                "}";
    }
}