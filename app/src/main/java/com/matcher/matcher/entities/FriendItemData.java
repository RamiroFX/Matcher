package com.matcher.matcher.entities;

import android.os.Bundle;
import android.text.TextUtils;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Ramiro on 18/02/2018.
 */

public class FriendItemData {

    private String id;
    private String name;
    private String picture;

    public FriendItemData(JSONObject friend) {
        try {
            this.name = friend.getString("name");
            this.id = friend.getString("id");
        } catch (JSONException e) {
            this.name = "";
            this.id = "";
            e.printStackTrace();
        }
    }

    public FriendItemData(String name, String picture) {
        this.name = name;
        this.picture = picture;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }


    public static String getProfilePictureURL(String userId) {
        return "https://graph.facebook.com/" + userId + "/picture";
    }

    public String getFacebookName(long userid) {
        if (TextUtils.isEmpty(name)) {
            Bundle parameters = new Bundle();
            parameters.putString("fields", "name");
            GraphRequest req = GraphRequest.newGraphPathRequest(
                    AccessToken.getCurrentAccessToken(),
                    Long.toString(userid),
                    new GraphRequest.Callback() {
                        @Override
                        public void onCompleted(GraphResponse graphResponse) {
                            try {
                                JSONObject response = graphResponse.getJSONObject();
                                name = response.getString("name");
                            } catch (Exception e) {
                            }
                        }
                    });
            req.setParameters(parameters);
            req.executeAndWait();
        }
        return name;
    }

    public String toString() {
        return String.format("%s: %s", this.name, this.id);
    }
}