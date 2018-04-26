package com.matcher.matcher.entities;

import com.google.firebase.database.Exclude;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatHeader {
    private String uid;
    private String name;
    private String lastMessage;
    private Long timeStamp;

    public ChatHeader() {
    }

    public ChatHeader(String name, String lastMessage) {
        this.name = name;
        this.lastMessage = lastMessage;
        this.timeStamp = new Date().getTime();
    }

    @Exclude
    public String getUid() {
        return uid;
    }

    @Exclude
    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("lastMessage", lastMessage);
        result.put("timeStamp", timeStamp);
        return result;
    }

    public String getParseTimeStamp() {
        return new SimpleDateFormat("hh:mm MM/dd/yyyy").format(new Date(getTimeStamp()));
    }

    @Override
    public String toString() {
        return "chats{" +
                "uid: " +getUid()+
                ", name: " +getName()+
                ", lastMessage: " +getLastMessage()+
                ", timeStamp: " +getTimeStamp()+
                "}";
    }
}
