package com.matcher.matcher.entities;

/**
 * Created by Ramiro on 27/02/2018.
 */

import com.google.firebase.database.Exclude;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Chat {
    private String name;
    private String message;
    private Long timeStamp;

    public Chat() {
    }

    public Chat(String name, String message) {
        this.name = name;
        this.message = message;
        this.timeStamp = new Date().getTime();
    }

    public Chat(String name, String message, Long timeStamp) {
        this.name = name;
        this.message = message;
        this.timeStamp = timeStamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        result.put("name", name);
        result.put("message", message);
        result.put("timeStamp", timeStamp);
        return result;
    }

    @Override
    public String toString() {
        return "chat{" +
                "name: " + getName() +
                " ,message: " + getMessage() +
                " ,timeStamp: " + getTimeStamp() +
                "}";
    }
}