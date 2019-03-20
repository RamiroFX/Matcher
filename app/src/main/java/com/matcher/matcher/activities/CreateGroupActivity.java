package com.matcher.matcher.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matcher.matcher.R;
import com.matcher.matcher.Utils.Constants;
import com.matcher.matcher.Utils.DBContract;
import com.matcher.matcher.Utils.RequestCode;
import com.matcher.matcher.Utils.SharedPreferenceHelper;
import com.matcher.matcher.adapters.FriendRequestAdapter;
import com.matcher.matcher.entities.ChatHeader;
import com.matcher.matcher.entities.FriendRequest;
import com.matcher.matcher.entities.Group;
import com.matcher.matcher.entities.Users;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CreateGroupActivity extends AppCompatActivity implements View.OnClickListener, FriendRequestAdapter.OnFriendRequestAdapterListener {

    private static final int INVITED_FRIENDS_REQUEST = 5;
    private static final String TAG = "CreateGroupActivity";
    private static final String CREATE_EVENT_NAME_REQUIRED = "Oye, amigo. El nombre de evento es importante...";
    private static final String CREATE_EVENT_NAME_TOO_LONG = "Máximo 25 caracteres";
    private static final String CREATE_EVENT_DESCRIPTION_TOO_LONG = "Máximo 50 caracteres";
    private static final String CREATE_EVENT_ON_COMPLETE_ERROR = "Ha ocurrido un error inesperado al crear el evento. Intenta nuevamente :S";
    private static final String CREATE_EVENT_ON_COMPLETE_SUCCESS = "Enhorabuena! Grupo creado :)";

    private EditText etGroupName, etGroupDescription, etGroupOwner;
    private TextView tvGroupOwner;
    private Button btnInviteFriends, btnCreateGroup;
    private RecyclerView rvMembers;

    private FriendRequestAdapter adapter;
    private Group group;
    private boolean isGroupOwner;
    private String myUID;

    private DatabaseReference mDatabaseRef, mGroupRef;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        Toolbar toolbar = findViewById(R.id.tb_create_group);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SharedPreferenceHelper sharedPreferenceHelper = SharedPreferenceHelper.getInstance(getApplicationContext());
        this.myUID = sharedPreferenceHelper.getUser().getUid();
        this.isGroupOwner = false;
        this.etGroupName = findViewById(R.id.et_create_group_name_content);
        this.etGroupDescription = findViewById(R.id.et_create_group_description_content);
        this.btnInviteFriends = findViewById(R.id.create_group_invite_friends_button);
        this.btnInviteFriends.setOnClickListener(this);
        this.btnCreateGroup = findViewById(R.id.create_group_ok_button);
        this.btnCreateGroup.setOnClickListener(this);
        this.adapter = new FriendRequestAdapter(this);
        this.rvMembers = findViewById(R.id.rv_create_group_members);
        this.rvMembers.setAdapter(adapter);
        this.rvMembers.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        this.mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mGroupRef = mDatabaseRef.child(DBContract.GroupTable.TABLE_NAME);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int type = extras.getInt(Constants.GROUP_ACTIVITY_TYPE, -1);
            String uid = extras.getString(DBContract.GroupTable.COL_NAME_UID, "");
            String description = extras.getString(DBContract.GroupTable.COL_NAME_DESCRIPTION, "");
            String name = extras.getString(DBContract.GroupTable.COL_NAME_NAME, "");
            String ownerName = extras.getString(DBContract.GroupTable.COL_NAME_OWNER_NAME, "");
            String ownerUid = extras.getString(DBContract.GroupTable.COL_NAME_OWNER_UID, "");
            this.group = new Group();
            this.group.setUid(uid);
            this.group.setGroupName(name);
            this.group.setDescription(description);
            this.group.setOwnerName(ownerName);
            this.group.setOwnerUid(ownerUid);
            this.etGroupName.setText(name);
            this.etGroupName.setTextColor(Color.BLACK);
            this.etGroupDescription.setText(description);
            this.etGroupDescription.setTextColor(Color.BLACK);
            switch (type) {
                case Constants.GROUP_ACTIVITY_TYPE_VIEW: {
                    this.etGroupOwner = findViewById(R.id.et_create_group_owner_content);
                    this.etGroupOwner.setText(ownerName);
                    this.etGroupOwner.setEnabled(false);
                    this.etGroupOwner.setTextColor(Color.BLACK);
                    this.etGroupOwner.setVisibility(View.VISIBLE);
                    this.tvGroupOwner = findViewById(R.id.create_group_owner_textview);
                    this.tvGroupOwner.setVisibility(View.VISIBLE);
                    this.etGroupName.setEnabled(false);
                    this.etGroupDescription.setEnabled(false);
                    this.btnCreateGroup.setVisibility(View.GONE);
                    this.btnInviteFriends.setVisibility(View.GONE);
                    break;
                }
                case Constants.GROUP_ACTIVITY_TYPE_CREATE: {

                    break;
                }
            }
            if (!ownerUid.isEmpty()) {
                if (ownerUid.equals(myUID)) {
                    this.isGroupOwner = true;
                    this.etGroupName.setEnabled(true);
                    this.etGroupDescription.setEnabled(true);
                } else {
                    this.btnInviteFriends.setVisibility(View.GONE);
                }
                getMembers(uid);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_group, menu);
        MenuItem editGroup = menu.findItem(R.id.item_create_group_save);
        if (isGroupOwner) {
            editGroup.setVisible(true);
        } else {
            editGroup.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.item_create_group_save) {
            saveGroupChanges();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveGroupChanges() {
        final String newGroupName = String.valueOf(etGroupName.getText());
        final String newGroupDescription = String.valueOf(etGroupDescription.getText());
        final TaskCompletionSource<DataSnapshot> dbGroupMemberSource = new TaskCompletionSource<>();
        final Task dbGroupMemberList = dbGroupMemberSource.getTask();
        DatabaseReference mGroupMembersRef = mDatabaseRef.child(DBContract.GroupMemberTable.TABLE_NAME).child(group.getUid());
        mGroupMembersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dbGroupMemberSource.setResult(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                dbGroupMemberSource.setException(databaseError.toException());
            }
        });
        ///
        Task<Void> allTask = Tasks.whenAll(dbGroupMemberList);
        allTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Map<String, Object> groupUpdates = new HashMap<>();
                groupUpdates.put("/" + DBContract.GroupTable.TABLE_NAME + "/" + group.getUid() + "/" + DBContract.GroupTable.COL_NAME_NAME, newGroupName);
                groupUpdates.put("/" + DBContract.GroupTable.TABLE_NAME + "/" + group.getUid() + "/" + DBContract.GroupTable.COL_NAME_DESCRIPTION, newGroupDescription);
                DataSnapshot data = (DataSnapshot) dbGroupMemberList.getResult();
                for (DataSnapshot snapshot : data.getChildren()) {
                    groupUpdates.put("/" + DBContract.UserGroupsTable.TABLE_NAME + "/" + snapshot.getKey() + "/" + group.getUid(), newGroupName);
                }
                for (DataSnapshot snapshot : data.getChildren()) {
                    groupUpdates.put("/" + DBContract.ChatsTable.TABLE_NAME + "/" + snapshot.getKey() + "/" + group.getUid() + "/" + DBContract.ChatsTable.COL_NAME_FULLNAME, newGroupName);
                }
                mDatabaseRef.updateChildren(groupUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        String message = getString(R.string.save_changes_complete_message);
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                        Intent intent = new Intent();
                        intent.putExtra(DBContract.GroupTable.COL_NAME_NAME, newGroupName);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });
            }
        });
        allTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // apologize profusely to the user!
            }
        });
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.create_group_invite_friends_button: {
                showInviteFriendsDialog();
                break;
            }
            case R.id.create_group_ok_button: {
                createGroup();
                break;
            }
        }
    }

    private void createGroup() {
        Log.d(TAG, "createGroup");
        if (isFormValid()) {
            Log.d(TAG, "Form is valid");
            String groupName = etGroupName.getText().toString();
            String groupDescription = etGroupDescription.getText().toString();

            Group group = new Group();
            group.setGroupName(groupName);
            group.setDescription(groupDescription);
            group.setOwnerUid(myUID);
            SharedPreferenceHelper sharedPreferenceHelper = SharedPreferenceHelper.getInstance(getApplicationContext());
            Users anUser = sharedPreferenceHelper.getUser();
            group.setOwnerName(anUser.getFullName());

            String newGroupKey = mGroupRef.push().getKey();
            Map<String, Object> childUpdates = new HashMap<>();
            //Create groupOwner
            FriendRequest currentUser = new FriendRequest();
            currentUser.setUid(myUID);
            currentUser.setFullName(anUser.getFullName());
            group.addMember(currentUser);
            //Create welcome message for members
            String newpMemberMessage = getString(R.string.create_group_new_member_message) + " " + group.getGroupName();
            Map chatValue = new ChatHeader(group.getGroupName(), newpMemberMessage, true).toMap();
            //ADD members to the group
            for (FriendRequest friend : adapter.getmValues()) {
                Log.d(TAG, "Adding member to group: " + friend);
                group.addMember(friend);
                childUpdates.put("/" + DBContract.UserGroupsTable.TABLE_NAME + "/" + friend.getUid() + "/" + newGroupKey, group.getGroupName());
                childUpdates.put("/" + DBContract.ChatsTable.TABLE_NAME + "/" + friend.getUid() + "/" + newGroupKey, chatValue);
            }
            //ADD current user to the group
            childUpdates.put("/" + DBContract.UserGroupsTable.TABLE_NAME + "/" + group.getOwnerUid() + "/" + newGroupKey, group.getGroupName());
            childUpdates.put("/" + DBContract.ChatsTable.TABLE_NAME + "/" + group.getOwnerUid() + "/" + newGroupKey, chatValue);
            Log.d(TAG, "Group: " + group);
            Log.d(TAG, "Members: " + group.getGroupMembers());
            //CREATE GROUP
            childUpdates.put("/" + DBContract.GroupTable.TABLE_NAME + "/" + newGroupKey, group);
            //ADD MEMBERS TO THE GROUP
            childUpdates.put("/" + DBContract.GroupMemberTable.TABLE_NAME + "/" + newGroupKey, group.getGroupMembers());

            Log.d(TAG, "Saving Group");
            logAnalyticEvent(Constants.CHAT_CREATE_GROUP_EVENT);
            mDatabaseRef.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Toast.makeText(getApplicationContext(), CREATE_EVENT_ON_COMPLETE_ERROR, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), CREATE_EVENT_ON_COMPLETE_SUCCESS, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });
        }
    }

    private void showInviteFriendsDialog() {
        Intent i = new Intent(this, FriendsActivity.class);
        i.putExtra(RequestCode.INVITED_FRIENDS_REQUEST.getDescription(), RequestCode.INVITED_FRIENDS_REQUEST.getCode());
        startActivityForResult(i, INVITED_FRIENDS_REQUEST);
    }

    private boolean isFormValid() {
        Log.d(TAG, "isFormValid");
        boolean result = true;
        //VALIDATE EVENT NAME
        if (TextUtils.isEmpty(etGroupName.getText().toString())) {
            etGroupName.requestFocus();
            etGroupName.setError(CREATE_EVENT_NAME_REQUIRED);
            result = false;
        } else {
            etGroupName.requestFocus();
            etGroupName.setError(null);
        }
        if (etGroupName.getText().toString().length() > 25) {
            etGroupName.requestFocus();
            etGroupName.setError(CREATE_EVENT_NAME_TOO_LONG);
            result = false;
        } else {
            etGroupName.requestFocus();
            etGroupName.setError(null);
        }
        //VALIDATE EVENT DESCRIPTION
        if (etGroupDescription.getText().toString().length() > 50) {
            etGroupDescription.requestFocus();
            etGroupDescription.setError(CREATE_EVENT_DESCRIPTION_TOO_LONG);
            result = false;
        } else {
            etGroupDescription.requestFocus();
            etGroupDescription.setError(null);
        }
        return result;
    }

    private void getMembers(String groupUid) {
        mDatabaseRef.child(DBContract.GroupMemberTable.TABLE_NAME).child(groupUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FriendRequest member = new FriendRequest(snapshot);
                    adapter.onRequestAdded(member);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void logAnalyticEvent(String name) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, DBContract.ChatsTable.TABLE_NAME);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case INVITED_FRIENDS_REQUEST: {
                if (resultCode == RESULT_OK) {
                    String json = data.getStringExtra(RequestCode.INVITED_FRIENDS_REQUEST.getDescription());
                    try {
                        JSONArray jsonFriendArray = new JSONArray(json);
                        adapter.clearList();
                        for (int i = 0; i < jsonFriendArray.length(); i++) {
                            JSONObject jsonFriend = jsonFriendArray.getJSONObject(i);
                            String uid = jsonFriend.getString(jsonFriend.keys().next());
                            String name = jsonFriend.getString(DBContract.UserTable.COL_NAME_FULLNAME);
                            adapter.onRequestAdded(new FriendRequest(uid, name));
                        }
                    } catch (Throwable ignored) {
                    }
                }
                break;
            }
        }
    }

    @Override
    public void onImageViewInteraction(FriendRequest friendRequest) {

    }

    @Override
    public void onTextViewInteraction(FriendRequest friendRequest) {

    }

}

