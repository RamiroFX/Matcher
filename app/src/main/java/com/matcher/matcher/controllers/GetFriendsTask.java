package com.matcher.matcher.controllers;

import android.os.AsyncTask;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.matcher.matcher.entities.Friend;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class GetFriendsTask extends AsyncTask<Void, Void, List<Friend>> {

    private static final String TAG ="GetFriendsTask";
    public interface OnFriendInviteListener {
        void OnFriendInviteInteraction(List<Friend> friends);
    }
    private OnFriendInviteListener mListener;

    public GetFriendsTask(OnFriendInviteListener listener) {
        mListener = listener;
    }

    @Override
    protected List<Friend> doInBackground(Void... voids) {
        Log.d(TAG,"doInBackground");
        final List<Friend> friendList = new ArrayList<>();
        try {
            GraphRequest request = GraphRequest.newMyFriendsRequest(
                    AccessToken.getCurrentAccessToken(),
                    new GraphRequest.GraphJSONArrayCallback() {
                        @Override
                        public void onCompleted(JSONArray array, GraphResponse response) {
                            //jsonFriends = array.toString();
                        }
                    });
            request.setCallback(new GraphRequest.Callback() {
                @Override
                public void onCompleted(GraphResponse response) {
                    if (response != null && response.getJSONObject() != null) {
                        try {
                            JSONArray friendslist = response.getJSONObject().getJSONArray("data");
                            for (int l = 0; l < friendslist.length(); l++) {
                                String name = friendslist.getJSONObject(l).getString("name");
                                String facebookId = friendslist.getJSONObject(l).getString("id");
                                Friend friendItemData = new Friend(facebookId, name);
                                friendList.add(friendItemData);
                                Log.d(TAG,"callback: "+friendItemData);
                            }
                            //get next batch of results of exists
                            GraphRequest nextRequest = response.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
                            if (nextRequest != null) {
                                nextRequest.setCallback(this);
                                nextRequest.executeAndWait();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            request.executeAsync();
        } catch (FacebookException e) {
            return null;
        }
        return friendList;
    }

    @Override
    protected void onPostExecute(List<Friend> friends) {
        Log.d(TAG,"onPostExecute: "+friends.size());
        mListener.OnFriendInviteInteraction(friends);
    }
}