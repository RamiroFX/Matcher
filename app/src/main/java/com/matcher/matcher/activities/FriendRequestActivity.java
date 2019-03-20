package com.matcher.matcher.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.matcher.matcher.R;
import com.matcher.matcher.Utils.DBContract;
import com.matcher.matcher.Utils.SharedPreferenceHelper;
import com.matcher.matcher.adapters.FriendRequestAdapter;
import com.matcher.matcher.dialogs.ConfirmLogoutDialog;
import com.matcher.matcher.entities.FriendRequest;
import com.matcher.matcher.entities.ScoredFriend;

import java.util.HashMap;
import java.util.Map;

public class FriendRequestActivity extends AppCompatActivity implements FriendRequestAdapter.OnFriendRequestAdapterListener, ConfirmLogoutDialog.confirmLogoutDialogListener {

    private static final String TAG = "FriendRequestActivity";
    private static final String CONFIRM_FRIENDSHIP_FRAG = "confirm_friendship";

    private RecyclerView friendReqRV;
    private DatabaseReference mDatabaseReference, mFriendReqRef;
    private ChildEventListener mChilFriendshipListener;

    private String friendUID, myUID;
    private FriendRequestAdapter friendReqAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SharedPreferenceHelper sharedPreferenceHelper = SharedPreferenceHelper.getInstance(getApplicationContext());
        this.myUID = sharedPreferenceHelper.getUser().getUid();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFriendReqRef = mDatabaseReference.child(DBContract.FriendRequestTable.TABLE_NAME).child(myUID);
        friendReqRV = this.findViewById(R.id.rvFriendList);
        friendReqAdapter = new FriendRequestAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        friendReqRV.setAdapter(friendReqAdapter);
        friendReqRV.setLayoutManager(layoutManager);
        getFriendRequests();
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_invite_friends, menu);
        return super.onCreateOptionsMenu(menu);
    }*/

    @Override
    protected void onStop() {
        super.onStop();
        if (mChilFriendshipListener != null) {
            mFriendReqRef.removeEventListener(mChilFriendshipListener);
        }
    }

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

    private void getFriendRequests() {
        mChilFriendshipListener = mFriendReqRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildAdded: " + dataSnapshot);
                friendReqAdapter.onRequestAdded(new FriendRequest(dataSnapshot));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildChanged: " + dataSnapshot);
                friendReqAdapter.onRequestChanged(new FriendRequest(dataSnapshot));

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved: " + dataSnapshot);
                friendReqAdapter.onRequestRemoved(new FriendRequest(dataSnapshot));
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
    public void onImageViewInteraction(FriendRequest friendRequest) {
        Log.d(TAG, "onImageViewInteraction: " + friendRequest);
        viewFriendProfile(friendRequest);
    }

    @Override
    public void onTextViewInteraction(FriendRequest friendRequest) {
        Log.d(TAG, "onTextViewInteraction: " + friendRequest);
        friendUID = friendRequest.getUid();
        displayConfirmationDialog(friendRequest);
    }

    private void displayConfirmationDialog(FriendRequest friendRequest) {
        String message = friendRequest.getFullName() + " " + getString(R.string.FRIEND_REQUEST_MESSAGE);
        DialogFragment newFragment = ConfirmLogoutDialog.newInstance(this, message);
        newFragment.show(getSupportFragmentManager(), CONFIRM_FRIENDSHIP_FRAG);
    }

    private void viewFriendProfile(FriendRequest friendRequest) {
        String friendUID = friendRequest.getUid();
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

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        addFriend(friendUID);
        friendUID = "";
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        removeFriendRequest(friendUID);
        friendUID = "";
    }

    /*
    Obtiene los datos del amigo seleccionado en la comunidad, si es que existe llama al metodo getCurrentUserData.
     */
    private void addFriend(String friendUID) {
        DatabaseReference userRef = mDatabaseReference.child(DBContract.UserTable.TABLE_NAME).child(friendUID);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String friendUID = dataSnapshot.getKey();
                    String friendName = dataSnapshot.child(DBContract.UserTable.COL_NAME_FULLNAME).getValue(String.class);
                    int friendScore = dataSnapshot.child(DBContract.UserTable.COL_NAME_SCORE).getValue(Integer.class);
                    ScoredFriend scoredFriend = new ScoredFriend(friendUID, friendName, friendScore);
                    getCurrentUserData(scoredFriend);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    /*
    Obtiene los datos actuales del usuario para agregarlos a la tabla friendship
     */
    private void getCurrentUserData(final ScoredFriend scoredFriend) {
        final DatabaseReference friendShipRef = mDatabaseReference.child(DBContract.FriendshipTable.TABLE_NAME);
        DatabaseReference userRef = mDatabaseReference.child(DBContract.UserTable.TABLE_NAME).child(myUID);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //get the data
                    final String myUid = dataSnapshot.getKey();
                    String myName = dataSnapshot.child(DBContract.UserTable.COL_NAME_FULLNAME).getValue(String.class);
                    int myScore = dataSnapshot.child(DBContract.UserTable.COL_NAME_SCORE).getValue(Integer.class);

                    final String newFriendUID = scoredFriend.getUid();
                    final String newFriendName = scoredFriend.getFullName();
                    final int newFriendScore = scoredFriend.getScore();
                    //set the values
                    ScoredFriend me = new ScoredFriend(null, myName, myScore);//we pass null uid bc the root is the uid
                    ScoredFriend newFriend = new ScoredFriend(null, newFriendName, newFriendScore);
                    //set the map
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("/" + newFriendUID + "/" + myUid, me);
                    childUpdates.put("/" + myUid + "/" + newFriendUID, newFriend);
                    //update the map-values
                    friendShipRef.updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            removeFriendRequest(newFriendUID);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void removeFriendRequest(String friendUID) {
        mFriendReqRef.child(friendUID).setValue(null);
    }
}
