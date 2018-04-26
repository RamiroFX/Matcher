package com.matcher.matcher.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.matcher.matcher.R;
import com.matcher.matcher.Utils.DBContract;
import com.matcher.matcher.Utils.RequestCode;
import com.matcher.matcher.adapters.MyFriendsRecyclerViewAdapter;
import com.matcher.matcher.adapters.SelectableFriendAdapter;
import com.matcher.matcher.adapters.SelectableFriendViewHolder;
import com.matcher.matcher.entities.Friend;
import com.matcher.matcher.entities.FriendItemData;
import com.matcher.matcher.entities.SelectableFriend;
import com.matcher.matcher.fragments.FriendsFragment;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FriendsActivity extends AppCompatActivity implements SelectableFriendViewHolder.OnFriendSelectedListener {

    private static final String TAG = "FriendsActivity";
    private static final String ON_USER_PROFILE_NOT_FOUND = "Es posible que el usuario no tenga perfil todavía";
    private static final int INVITED_FRIENDS_REQUEST = 5;
    private static final int CHAT_FRIEND_REQUEST = 6;

    private RecyclerView friendsRV;
    private MenuItem itemToHide;
    private DatabaseReference mDatabaseReference;
    private int requestType;

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
        mDatabaseReference = mDatabase.getReference();
        friendsRV = this.findViewById(R.id.rvFriendList);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            requestType = extras.getInt(RequestCode.INVITED_FRIENDS_REQUEST.getDescription(), 0);
            if (requestType == RequestCode.INVITED_FRIENDS_REQUEST.getCode()) {
                selectedItems = new ArrayList<>();
                selectableItems = new ArrayList<>();
                selectableFriendAdapter = new SelectableFriendAdapter(this, selectableItems, true);
                LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                friendsRV.setAdapter(selectableFriendAdapter);
                friendsRV.setLayoutManager(layoutManager);
                getFriends();
                return;
            }
            requestType = extras.getInt(RequestCode.CHAT_FRIENDS_REQUEST.getDescription(), 0);
            if (requestType == RequestCode.CHAT_FRIENDS_REQUEST.getCode()) {
                selectedItems = new ArrayList<>();
                selectableItems = new ArrayList<>();
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
        if (requestType == RequestCode.CHAT_FRIENDS_REQUEST.getCode()) {
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
                Snackbar.make(this.friendsRV, "Selected friend is " + item.getUsername() +
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
        }
    }

    private void getFriends() {
        Log.d(TAG, "getFriends");
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabaseReference.child(DBContract.FriendshipTable.TABLE_NAME).child(uid).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String uid = snapshot.getKey();
                            String name = snapshot.getValue() + "";
                            selectableFriendAdapter.onFriendAdded(new SelectableFriend(new Friend(uid, name), false));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        throw databaseError.toException();
                    }
                });
    }

    private void ViewFriendProfile(SelectableFriend item) {
        String friendUID= item.getUid();
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

    private void chatActivity(SelectableFriend item) {
        String uid = item.getUid();
        String fullName = item.getUsername();
        Intent i = new Intent();
        i.putExtra(DBContract.UserTable.COL_NAME_FULLNAME, fullName);
        i.putExtra(DBContract.UserTable.COL_NAME_UID, uid);
        setResult(RESULT_OK, i);
        finish();
    }

    private void seleccionarAmigos() {
        if (!selectedItems.isEmpty()) {
            ArrayList<String> jsonArraylist = new ArrayList<String>();
            for (Friend friend : selectedItems) {
                jsonArraylist.add(friend.toJsonString());
            }
            Intent intent = new Intent();
            intent.putExtra(RequestCode.INVITED_FRIENDS_REQUEST.getDescription(), jsonArraylist.toString());
            setResult(RESULT_OK, intent);
            finish();
        } else {
            Snackbar.make(this.friendsRV, "Seleccione al menos un amigo", Snackbar.LENGTH_LONG).show();
        }
    }
}
