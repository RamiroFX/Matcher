package com.matcher.matcher.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.matcher.matcher.R;
import com.matcher.matcher.Utils.Constants;
import com.matcher.matcher.Utils.DBContract;
import com.matcher.matcher.Utils.SharedPreferenceHelper;
import com.matcher.matcher.adapters.ChallengeRequestAdapter;
import com.matcher.matcher.entities.Challenge;

public class ChallengeRequestActivity extends AppCompatActivity implements ChallengeRequestAdapter.OnChallengeRequestAdapterListener {

    private static final String TAG = "ChallengeReqActivity";

    private RecyclerView challengeReqRV;
    private DatabaseReference mDatabaseReference;
    private ChallengeRequestAdapter challengeRequestAdapter;
    private String myUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_request);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        SharedPreferenceHelper sharedPreferenceHelper = SharedPreferenceHelper.getInstance(getApplicationContext());
        myUID = sharedPreferenceHelper.getUser().getUid();
        mDatabaseReference = mDatabase.getReference();
        challengeReqRV = this.findViewById(R.id.rvChallengeList);
        challengeRequestAdapter = new ChallengeRequestAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        challengeReqRV.setAdapter(challengeRequestAdapter);
        challengeReqRV.setLayoutManager(layoutManager);
        getChallengeRequest();
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_invite_friends, menu);
        return super.onCreateOptionsMenu(menu);
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public boolean onNavigateUp() {
        this.finish();
        return super.onNavigateUp();
    }

    @Override
    public boolean onSupportNavigateUp() {
        this.finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        this.finish();
        super.onBackPressed();
    }

    private void getChallengeRequest() {
        mDatabaseReference.child(DBContract.ChallengeRequestsTable.TABLE_NAME).child(myUID).
                addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Log.d(TAG,"");
                        Challenge challenge = dataSnapshot.getValue(Challenge.class);
                        challenge.setUid(dataSnapshot.getKey());
                        challengeRequestAdapter.onRequestAdded(challenge);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        Challenge challenge = dataSnapshot.getValue(Challenge.class);
                        challenge.setUid(dataSnapshot.getKey());
                        challengeRequestAdapter.onRequestChanged(challenge);
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        Challenge challenge = dataSnapshot.getValue(Challenge.class);
                        challenge.setUid(dataSnapshot.getKey());
                        challengeRequestAdapter.onRequestRemoved(challenge);
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onImageViewInteraction(Challenge challenge) {
        viewFriendProfile(challenge);
    }

    @Override
    public void onTextViewInteraction(Challenge challenge) {
        displayChallengeDialog(challenge);
    }

    private void displayChallengeDialog(Challenge challengeReq) {
        String message = challengeReq.getChallenger().getFullName() + " " +
                getString(R.string.challenge_request_message) + " " +
                challengeReq.getSport().getName() + " " +
                getString(R.string.challenge_request_message_schedule) + " " +
                challengeReq.challengeDate();
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        Intent i = new Intent(this.getApplicationContext(), CreateDuelActivity.class);
        i.putExtra(DBContract.ChallengeTable.COL_NAME_CHALLENGE_UID, challengeReq.getUid());
        i.putExtra(DBContract.ChallengeTable.COL_NAME_CHALLENGER_UID, challengeReq.getChallenger().getUid());
        i.putExtra(DBContract.ChallengeTable.COL_NAME_CHALLENGER, challengeReq.getChallenger().getFullName());
        i.putExtra(DBContract.ChallengeTable.COL_NAME_SPORT, challengeReq.getSport().getName());
        i.putExtra(DBContract.ChallengeTable.COL_NAME_SCHEDULED_TIME, challengeReq.getScheduledTime());
        i.putExtra(Constants.CHALLENGE_ACTIVITY_TYPE, Constants.CHALLENGE_ACTIVITY_TYPE_ACCEPT);
        startActivity(i);
    }

    private void viewFriendProfile(Challenge challengeReq) {
        String friendUID = challengeReq.getChallenger().getUid();
        Query query = mDatabaseReference.child(DBContract.UserTable.TABLE_NAME).
                orderByChild(DBContract.UserTable.COL_NAME_UID).
                equalTo(friendUID).limitToFirst(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String uid = snapshot.getKey();
                        String fullName = snapshot.child(DBContract.UserTable.COL_NAME_FULLNAME).getValue(String.class);
                        String nickName = snapshot.child(DBContract.UserTable.COL_NAME_NICKNAME).getValue(String.class);
                        String aboutUser = snapshot.child(DBContract.UserTable.COL_NAME_ABOUT).getValue(String.class);
                        String facebookId = snapshot.child(DBContract.UserTable.COL_NAME_FACEBOOK_ID).getValue(String.class);
                        if (snapshot.child(DBContract.UserTable.COL_NAME_SPORTS).getValue() != null) {
                            int[] favoriteSports;
                            long childrenSport = dataSnapshot.child(DBContract.UserTable.COL_NAME_SPORTS).getChildrenCount();
                            long childrenCount = 1;
                        }
                        Intent i = new Intent(getApplicationContext(), ViewProfile.class);
                        i.putExtra(DBContract.UserTable.COL_NAME_FULLNAME, fullName);
                        i.putExtra(DBContract.UserTable.COL_NAME_NICKNAME, nickName);
                        i.putExtra(DBContract.UserTable.COL_NAME_ABOUT, aboutUser);
                        i.putExtra(DBContract.UserTable.COL_NAME_FACEBOOK_ID, facebookId);
                        i.putExtra(DBContract.UserTable.COL_NAME_UID, uid);
                        startActivity(i);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

}
