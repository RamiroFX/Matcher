package com.matcher.matcher.entities;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.matcher.matcher.Utils.DBContract;

import org.json.JSONObject;

public class GroupChatHeader extends ChatHeader{

    private String memberName;


    public GroupChatHeader() {
        super();
    }

    public GroupChatHeader(DataSnapshot dataSnapshot) {
        try {
            JSONObject eventDetail = new JSONObject(dataSnapshot.getValue() + "");
            String groupUID = dataSnapshot.getKey();
            String groupName = eventDetail.getString(DBContract.GroupTable.COL_NAME_NAME);
            setUid(groupUID);
            setName(groupName);
        } catch (Throwable t) {
            Log.e("GroupChatHeader", "Could not parse malformed JSON: \"" + dataSnapshot.getValue() + "\"");
        }
    }

    public GroupChatHeader(String memberName) {
        this.memberName = memberName;
    }

    public GroupChatHeader(String name, String lastMessage, String memberName) {
        super(name, lastMessage, true);
        this.memberName = memberName;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }
}
