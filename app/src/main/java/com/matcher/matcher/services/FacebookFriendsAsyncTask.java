package com.matcher.matcher.services;

import android.os.AsyncTask;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.matcher.matcher.Utils.DBContract;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Map;

public class FacebookFriendsAsyncTask extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = "FacebookFriendsTask";
    private DatabaseReference mDatabaseReference;
    private String uid;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        uid = null;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        setFriendsFromFacebook();
        return true;
    }

    private void setFriendsFromFacebook() {
        try {
            GraphRequest request = GraphRequest.newMyFriendsRequest(
                    AccessToken.getCurrentAccessToken(),
                    new GraphRequest.GraphJSONArrayCallback() {
                        @Override
                        public void onCompleted(JSONArray array, GraphResponse response) {
                            if (response != null && response.getJSONObject() != null) {
                                try {
                                    JSONArray friendslist = response.getJSONObject().getJSONArray("data");
                                    for (int l = 0; l < friendslist.length(); l++) {
                                        String facebookId = friendslist.getJSONObject(l).getString("id");
                                        final Query query = mDatabaseReference.child(DBContract.UserTable.TABLE_NAME).
                                                orderByChild(DBContract.UserTable.COL_NAME_FACEBOOK_ID).
                                                equalTo(facebookId);
                                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                        String friendUID = snapshot.getKey();
                                                        String fullName = snapshot.child(DBContract.UserTable.COL_NAME_FULLNAME).getValue(String.class);
                                                        mDatabaseReference.child(DBContract.FriendshipTable.TABLE_NAME).child(uid).child(friendUID).setValue(fullName);
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                Log.d(TAG, "onCancelled: " + databaseError);
                                            }
                                        });

                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Log.d(TAG, "response = null && response.getJSONObject() = null");
                            }
                        }
                    });
            request.executeAndWait();
        } catch (FacebookException e) {
            e.printStackTrace();
        }
    }
}
