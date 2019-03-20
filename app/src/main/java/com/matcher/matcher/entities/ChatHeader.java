package com.matcher.matcher.entities;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;
import com.matcher.matcher.Utils.DBContract;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatHeader {
    private String uid;
    private String fullName;
    private String lastMessage;
    private Long timeStamp;
    private boolean isGroup;

    public ChatHeader() {
    }


    public ChatHeader(DataSnapshot dataSnapshot) {
        try {
            JSONObject groupDetail = new JSONObject(dataSnapshot.getValue() + "");
            String uid = dataSnapshot.getKey();
            String name = groupDetail.getString(DBContract.ChatsTable.COL_NAME_FULLNAME);
            String timeStamp = groupDetail.getString(DBContract.ChatsTable.COL_NAME_TIMESTAMP);
            String lastMessage = groupDetail.getString(DBContract.ChatsTable.COL_NAME_LAST_MESSAGE);
            boolean isGroup =  groupDetail.getBoolean(DBContract.ChatsTable.COL_NAME_IS_GROUP);
            setUid(uid);
            setName(name);
            setTimeStamp(Long.valueOf(timeStamp));
            setLastMessage(lastMessage);
            setIsGroup(isGroup);
        } catch (Throwable t) {
            Log.e("ChatHeader", "Could not parse malformed JSON: \"" + dataSnapshot.getValue() + "\"");
        }
    }
    public ChatHeader(String fullName, String lastMessage, boolean isGroup) {
        this.fullName = fullName;
        this.lastMessage = lastMessage;
        this.timeStamp = new Date().getTime();
        this.isGroup = isGroup;
    }

    @Exclude
    public String getUid() {
        return uid;
    }

    @Exclude
    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFullName() {
        return fullName;
    }

    public void setName(String fullName) {
        this.fullName = fullName;
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

    public boolean getIsGroup() {
        return isGroup;
    }

    public void setIsGroup(boolean group) {
        isGroup = group;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(DBContract.ChatsTable.COL_NAME_FULLNAME, getFullName());
        result.put(DBContract.ChatsTable.COL_NAME_LAST_MESSAGE, getLastMessage());
        result.put(DBContract.ChatsTable.COL_NAME_TIMESTAMP, getTimeStamp());
        result.put(DBContract.ChatsTable.COL_NAME_IS_GROUP, getIsGroup());
        return result;
    }

    public String getParseTimeStamp() {
        return new SimpleDateFormat("hh:mm MM/dd/yyyy").format(new Date(getTimeStamp()));
    }

    @Override
    public String toString() {
        return "chats{" +
                DBContract.ChatsTable.COL_NAME_UID + ": " + getUid() +
                ", " + DBContract.ChatsTable.COL_NAME_FULLNAME + ": " + getFullName() +
                ", " + DBContract.ChatsTable.COL_NAME_LAST_MESSAGE + ": " + getLastMessage() +
                ", " + DBContract.ChatsTable.COL_NAME_TIMESTAMP + ": " + getTimeStamp() +
                "," + DBContract.ChatsTable.COL_NAME_IS_GROUP + ": " + getIsGroup() +
                "}";
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        ChatHeader itemCompare = (ChatHeader) obj;
        if (itemCompare.getUid().equals(this.getUid()))
            return true;

        return false;
    }
}
