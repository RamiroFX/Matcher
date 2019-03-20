package com.matcher.matcher.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.matcher.matcher.R;
import com.matcher.matcher.Utils.DBContract;
import com.matcher.matcher.Utils.RequestCode;
import com.matcher.matcher.Utils.SharedPreferenceHelper;
import com.matcher.matcher.adapters.SelectableFriendAdapter;
import com.matcher.matcher.adapters.SelectableFriendViewHolder;
import com.matcher.matcher.entities.Friend;
import com.matcher.matcher.entities.ScoredFriend;
import com.matcher.matcher.entities.SelectableFriend;

import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity implements SelectableFriendViewHolder.OnFriendSelectedListener {

    private static final String TAG = "FriendsActivity";
    private static final int INVITED_FRIENDS_REQUEST = 5;
    private static final int CHAT_FRIEND_REQUEST = 6;
    private static final int CHALLENGE_FRIEND_REQUEST = 7;

    private RecyclerView friendsRV;
    private MenuItem itemToHide;
    private DatabaseReference mDatabaseReference;
    private int requestType;
    private String groupUid, groupOwnerUid, myUID;

    //Selectable adapter
    private SelectableFriendAdapter selectableFriendAdapter;
    private List<SelectableFriend> selectableItems;
    private List<Friend> selectedItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        SharedPreferenceHelper sharedPreferenceHelper = SharedPreferenceHelper.getInstance(getApplicationContext());
        this.myUID = sharedPreferenceHelper.getUser().getUid();
        mDatabaseReference = mDatabase.getReference();
        friendsRV = this.findViewById(R.id.rvFriendList);
        selectedItems = new ArrayList<>();
        selectableItems = new ArrayList<>();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            requestType = extras.getInt(RequestCode.INVITED_FRIENDS_REQUEST.getDescription(), 0);
            groupUid = extras.getString(DBContract.GroupTable.COL_NAME_UID, "");
            groupOwnerUid = extras.getString(DBContract.GroupTable.COL_NAME_OWNER_UID, "");
            if (requestType == RequestCode.INVITED_FRIENDS_REQUEST.getCode()) {
                selectableFriendAdapter = new SelectableFriendAdapter(this, selectableItems, true);
                LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                friendsRV.setAdapter(selectableFriendAdapter);
                friendsRV.setLayoutManager(layoutManager);
                if (!groupUid.isEmpty() && !groupOwnerUid.isEmpty()) {
                    getGroupMembers();
                } else {
                    getFriends();
                }
                return;
            }
            requestType = extras.getInt(RequestCode.CHAT_FRIENDS_REQUEST.getDescription(), 0);
            if (requestType == RequestCode.CHAT_FRIENDS_REQUEST.getCode()) {
                selectableFriendAdapter = new SelectableFriendAdapter(this, selectableItems, false);
                LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                friendsRV.setAdapter(selectableFriendAdapter);
                friendsRV.setLayoutManager(layoutManager);
                getFriends();
                return;
            }
            requestType = extras.getInt(RequestCode.CHALLENGE_FRIENDS_REQUEST.getDescription(), 0);
            if (requestType == RequestCode.CHALLENGE_FRIENDS_REQUEST.getCode()) {
                selectableFriendAdapter = new SelectableFriendAdapter(this, selectableItems, false);
                LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                friendsRV.setAdapter(selectableFriendAdapter);
                friendsRV.setLayoutManager(layoutManager);
                getFriends();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_invite_friends, menu);
        if (requestType == RequestCode.CHAT_FRIENDS_REQUEST.getCode() || requestType == RequestCode.CHAT_FRIENDS_REQUEST.getCode()) {
            itemToHide = menu.findItem(R.id.select_invited_friends);
            itemToHide.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.select_invited_friends:
                seleccionarAmigos();
                break;
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

    @Override
    public void onFriendSelected(SelectableFriend item, final View view) {
        switch (requestType) {
            case INVITED_FRIENDS_REQUEST: {
                selectedItems = selectableFriendAdapter.getSelectedItems();
                Snackbar.make(this.friendsRV, "Selected friend is " + item.getFullName() +
                        ", Totally  selectem item count is " + selectedItems.size(), Snackbar.LENGTH_LONG).show();
                break;
            }
            case CHAT_FRIEND_REQUEST: {
                if (view instanceof ImageView) {
                    ViewFriendProfile(item);
                } else if (view instanceof TextView) {
                    chatActivity(item);
                }
                break;
            }
            case CHALLENGE_FRIEND_REQUEST: {
                if (view instanceof ImageView) {
                    ViewFriendProfile(item);
                } else if (view instanceof TextView) {
                    challengeFriend(item);
                }
                break;
            }
        }
    }

    private void getFriends() {
        Log.d(TAG, "getFriends");
        mDatabaseReference.child(DBContract.FriendshipTable.TABLE_NAME).child(myUID).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "getFriends.onDataChange: " + dataSnapshot);
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            ScoredFriend scoredFriend = snapshot.getValue(ScoredFriend.class);
                            scoredFriend.setUid(snapshot.getKey());
                            selectableFriendAdapter.onFriendAdded(new SelectableFriend(scoredFriend, false));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        throw databaseError.toException();
                    }
                });
    }

    private void getGroupMembers() {
        Log.d(TAG, "getFriends");
        mDatabaseReference.child(DBContract.GroupMemberTable.TABLE_NAME).child(groupUid).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "DataSnapshot: " + dataSnapshot);
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            if (!snapshot.getKey().equals(groupOwnerUid)) {
                                ScoredFriend scoredFriend = new ScoredFriend(snapshot.getKey(), snapshot.getValue() + "", 0);
                                selectableFriendAdapter.onFriendAdded(new SelectableFriend(scoredFriend, false));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        throw databaseError.toException();
                    }
                });
    }

    private void ViewFriendProfile(SelectableFriend item) {
        String friendUID = item.getUid();
        Query query = mDatabaseReference.child(DBContract.UserTable.TABLE_NAME).
                orderByChild(DBContract.UserTable.COL_NAME_UID).
                equalTo(friendUID).limitToFirst(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        //ViewFriendProfile(snapshot);
                        String uid = snapshot.getKey();
                        String fullName = snapshot.child(DBContract.UserTable.COL_NAME_FULLNAME).getValue(String.class);
                        String nickName = snapshot.child(DBContract.UserTable.COL_NAME_NICKNAME).getValue(String.class);
                        String aboutUser = snapshot.child(DBContract.UserTable.COL_NAME_ABOUT).getValue(String.class);
                        int userScore = snapshot.child(DBContract.UserTable.COL_NAME_SCORE).getValue(Integer.class);
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
                        i.putExtra(DBContract.UserTable.COL_NAME_SCORE, userScore);
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

    private void chatActivity(SelectableFriend item) {
        String uid = item.getUid();
        String fullName = item.getFullName();
        Intent i = new Intent();
        i.putExtra(DBContract.UserTable.COL_NAME_FULLNAME, fullName);
        i.putExtra(DBContract.UserTable.COL_NAME_UID, uid);
        setResult(RESULT_OK, i);
        finish();
    }

    private void seleccionarAmigos() {
        if (!selectedItems.isEmpty()) {
            ArrayList<String> jsonArraylist = new ArrayList<>();
            for (Friend friend : selectedItems) {
                jsonArraylist.add(friend.toJsonString());
            }
            Intent intent = new Intent();
            intent.putExtra(RequestCode.INVITED_FRIENDS_REQUEST.getDescription(), jsonArraylist.toString());
            setResult(RESULT_OK, intent);
            finish();
        } else {
            String message = getString(R.string.friend_activity_on_empty_list);
            Snackbar.make(this.friendsRV, message, Snackbar.LENGTH_LONG).show();
        }
    }

    private void challengeFriend(SelectableFriend friend) {
        String uid = friend.getUid();
        String fullName = friend.getFullName();
        Intent i = new Intent();
        i.putExtra(DBContract.UserTable.COL_NAME_FULLNAME, fullName);
        i.putExtra(DBContract.UserTable.COL_NAME_UID, uid);
        setResult(RESULT_OK, i);
        finish();
    }
}
