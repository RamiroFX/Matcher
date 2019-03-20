package com.matcher.matcher.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
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
import com.matcher.matcher.Utils.RequestCode;
import com.matcher.matcher.Utils.SharedPreferenceHelper;
import com.matcher.matcher.adapters.MessageAdapter;
import com.matcher.matcher.dialogs.ConfirmLogoutDialog;
import com.matcher.matcher.entities.Chat;
import com.matcher.matcher.entities.ChatHeader;
import com.matcher.matcher.entities.Friend;
import com.matcher.matcher.entities.Group;
import com.matcher.matcher.entities.Users;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity implements
        ConfirmLogoutDialog.confirmLogoutDialogListener {

    private static final String TAG = "ChatActivity";
    private static final int ADD_MEMBER_REQUEST = 5;
    private static final int REMOVE_MEMBER_REQUEST = 6;
    private DatabaseReference mDatabaseRef;
    private DatabaseReference groupMembersRef;
    private ChildEventListener groupMemberListener, chatListener;
    private Query chatQuery;
    private Friend friend;
    private Group group;
    private String myUID;
    private MessageAdapter messageAdapter;
    private boolean isGroup, isGroupOwner;
    private List<Friend> groupMembersList;

    private RecyclerView chatList;
    private EditText etMessage;
    private TextView tvTitle, tvSubtitle;
    private ImageButton btnSend;
    private LinearLayoutManager linearLayoutManager;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        toolbar = findViewById(R.id.chatActivityToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        SharedPreferenceHelper sharedPreferenceHelper = SharedPreferenceHelper.getInstance(getBaseContext());
        this.isGroup = false;
        this.isGroupOwner = false;
        this.group = new Group();
        this.myUID = sharedPreferenceHelper.getUser().getUid();
        this.linearLayoutManager = new LinearLayoutManager(this);
        this.chatList = findViewById(R.id.rvChatList);
        this.etMessage = findViewById(R.id.etMessage);
        this.btnSend = findViewById(R.id.btnSend);
        this.btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
        this.mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        this.tvTitle = findViewById(R.id.toolbar_title_chat);
        this.tvTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewProfile();
            }
        });
        this.tvSubtitle = findViewById(R.id.toolbar_subtitle_chat);
        this.friend = new Friend();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String friendUid = "", username = "";
            if (!extras.getString(DBContract.UserTable.COL_NAME_UID, "").isEmpty()) {
                friendUid = extras.getString(DBContract.UserTable.COL_NAME_UID);
                this.friend.setUid(friendUid);
            }
            if (!extras.getString(DBContract.UserTable.COL_NAME_FULLNAME, "").isEmpty()) {
                username = extras.getString(DBContract.UserTable.COL_NAME_FULLNAME);
                this.tvTitle.setText(username);
                this.friend.setFullName(username);
            }
            isGroup = extras.getBoolean(DBContract.GroupTable.COL_NAME_NAME, false);
            if (isGroup) {
                groupMembersRef = mDatabaseRef.child(DBContract.GroupMemberTable.TABLE_NAME).child(friendUid);
                groupMembersList = new ArrayList<>();
                group.setUid(friendUid);
                group.setGroupName(username);
                retrieveGroupInformation();
                checkIfNotMember();
            }
            retrieveChats();
            Users user = new Users();
            user.setUid(myUID);
            this.messageAdapter = new MessageAdapter(user, friend, Constants.CHAT_VIEW_TYPE_GROUP_MESSAGE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat_activity, menu);
        //check if the user is the event owner to show only the Delete event item
        // if not show the Leave event item for invited user
        MenuItem deleteEvent = menu.findItem(R.id.item_chat_delete_group);
        MenuItem leaveEvent = menu.findItem(R.id.item_chat_leave_group);
        MenuItem addMember = menu.findItem(R.id.item_chat_add_member);
        MenuItem removeMember = menu.findItem(R.id.item_chat_remove_member);
        if (!isGroup) {
            String itemTitle = getString(R.string.item_chat_delete_chat);
            leaveEvent.setTitle(itemTitle);
        }
        if (isGroupOwner) {
            leaveEvent.setVisible(false);
            deleteEvent.setVisible(true);
        } else {
            leaveEvent.setVisible(true);
            deleteEvent.setVisible(false);
            addMember.setVisible(false);
            removeMember.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.item_chat_delete_group: {
                confirmDeleteGroup();
                return true;
            }
            case R.id.item_chat_leave_group: {
                confirmLeaveGroup();
                return true;
            }
            case R.id.item_chat_add_member: {
                showFriendsDialog(ADD_MEMBER_REQUEST);
                return true;
            }
            case R.id.item_chat_remove_member: {
                showFriendsDialog(REMOVE_MEMBER_REQUEST);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkIfNotMember() {
        Log.d(TAG, "checkIfNotMember");
        final TaskCompletionSource dbSource = new TaskCompletionSource<>();
        final Task dbFriendList = dbSource.getTask();
        DatabaseReference mGroupMemberRef = mDatabaseRef.child(DBContract.GroupMemberTable.TABLE_NAME).child(group.getUid());
        mGroupMemberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dbSource.setResult(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                dbSource.setException(databaseError.toException());
            }
        });

        Task<Void> allTask = Tasks.whenAll(dbFriendList);
        allTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                boolean isMember = false;
                DataSnapshot data = (DataSnapshot) dbFriendList.getResult();
                for (DataSnapshot snapshot : data.getChildren()) {
                    if (snapshot.getKey().equals(myUID)) {
                        isMember = true;
                        break;
                    }
                }
                if (!isMember) {
                    etMessage.setEnabled(false);
                    btnSend.setEnabled(false);
                } else {

                }
            }
        });
        allTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // apologize profusely to the user!
            }
        });
    }

    private void confirmLeaveGroup() {
        String message = "";
        if (isGroup) {
            message = getString(R.string.confirm_leave_group);
        } else {
            message = getString(R.string.confirm_delete_chat);
        }
        DialogFragment dialog = ConfirmLogoutDialog.newInstance(this, message);
        dialog.show(getSupportFragmentManager(), TAG);
    }

    private void confirmDeleteGroup() {
        String message = getString(R.string.confirm_delete_group);
        DialogFragment dialog = ConfirmLogoutDialog.newInstance(this, message);
        dialog.show(getSupportFragmentManager(), TAG);
    }

    private void setMenuOptions(Group group) {
        String ownerUID = group.getOwnerUid();
        if (ownerUID.equals(myUID)) {
            isGroupOwner = true;
        } else {
            isGroupOwner = false;
        }
        invalidateOptionsMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        Log.d(TAG, "onResume.isGroup: " + isGroup);
        Log.d(TAG, "onResume.groupMemberListener: " + groupMemberListener);
        retrieveChats();
        listenChats();
        if (isGroup) {
            listenMemberGroup();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        if (groupMembersRef != null) {
            Log.d(TAG, "groupMembersRef.removeEventListene");
            groupMembersRef.removeEventListener(groupMemberListener);
        }
        if (chatQuery != null) {
            Log.d(TAG, "chatQuery.removeEventListener");
            chatQuery.removeEventListener(chatListener);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        this.finish();
        return true;
    }

    private void listenChats() {
        messageAdapter.clearList();
        chatQuery = mDatabaseRef.child(DBContract.MessageTable.TABLE_NAME).child(myUID).child(friend.getUid()).
                orderByChild(DBContract.MessageTable.COL_NAME_TIMESTAMP);
        chatListener = chatQuery.addChildEventListener(
                new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Chat chat = new Chat();
                        for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                            if (itemSnapshot.getKey().equals(DBContract.MessageTable.COL_NAME_MESSAGE)) {
                                chat.setMessage(itemSnapshot.getValue() + "");
                                continue;
                            }
                            if (itemSnapshot.getKey().equals(DBContract.MessageTable.COL_NAME_TIMESTAMP)) {
                                chat.setTimeStamp((Long) itemSnapshot.getValue());
                                continue;
                            }
                            if (itemSnapshot.getKey().equals(DBContract.MessageTable.COL_NAME_USER)) {
                                Friend friend = itemSnapshot.getValue(Friend.class);
                                chat.setFriend(friend);
                            }
                        }
                        messageAdapter.onChatAdded(chat);
                        chatList.scrollToPosition(messageAdapter.getItemCount() - 1);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );

    }

    private void listenMemberGroup() {
        Log.d(TAG, "listenMemberGroup");
        groupMembersList.clear();
        groupMemberListener = groupMembersRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "listenMemberGroup.onChildAdded: " + dataSnapshot);
                Friend friend = new Friend(dataSnapshot.getKey(), dataSnapshot.getValue() + "");
                groupMembersList.add(friend);
                updateMemberListUI();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "listenMemberGroup.onChildChanged: " + dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "listenMemberGroup.onChildRemoved: " + dataSnapshot);
                Friend friend = new Friend(dataSnapshot.getKey(), dataSnapshot.getValue() + "");
                groupMembersList.remove(friend);
                checkForGroupDeleted(friend);
                checkForKickedMember(friend);
                updateMemberListUI();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "listenMemberGroup.onChildMoved: " + dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void checkForGroupDeleted(Friend friend) {
        String ownerUid = group.getOwnerUid();
        if (friend.getUid().equals(ownerUid)) {
            String message = getString(R.string.group_deleted);
            Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void checkForKickedMember(Friend friend) {
        if (friend.getUid().equals(myUID)) {
            String message = getString(R.string.create_group_removed_member_message);
            Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void updateMemberListUI() {
        StringBuilder members = new StringBuilder();
        for (Friend friend : groupMembersList) {
            members.append(friend.getFullName()).append(" - ");
        }
        int size = members.length();
        members.replace(size - 2, size, "");
        tvSubtitle.setVisibility(View.VISIBLE);
        tvSubtitle.setText(members.toString());
        tvSubtitle.setSelected(true);
    }

    private void retrieveGroupInformation() {
        mDatabaseRef.child(DBContract.GroupTable.TABLE_NAME).child(friend.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                group = dataSnapshot.getValue(Group.class);
                group.setUid(dataSnapshot.getKey());
                setMenuOptions(group);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void retrieveChats() {
        chatList.setAdapter(messageAdapter);
        chatList.setLayoutManager(linearLayoutManager);
    }

    public void sendMessage() {
        String content = etMessage.getText().toString();
        etMessage.setText("");
        /*InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);*/
        if (!content.trim().isEmpty()) {
            if (isGroup) {
                setGroupChat(content);
            } else {
                setChat(content);
            }
        }
    }

    public void setChat(String content) {
        SharedPreferenceHelper sharedPreferenceHelper = SharedPreferenceHelper.getInstance(getBaseContext());
        Users anUser = sharedPreferenceHelper.getUser();
        Friend me = new Friend(myUID, anUser.getFullName());
        Map messageValue = new Chat(me, content).toMap();
        Map chatValue = new ChatHeader(friend.getFullName(), content, false).toMap();
        Map chatValue2 = new ChatHeader(anUser.getFullName(), content, false).toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        //String uuid = UUID.randomUUID().toString();
        String uuid = FirebaseDatabase.getInstance().getReference().push().getKey();
        //Message detail
        childUpdates.put("/" + DBContract.MessageTable.TABLE_NAME + "/" + myUID + "/" + friend.getUid() + "/" + uuid, messageValue);
        childUpdates.put("/" + DBContract.MessageTable.TABLE_NAME + "/" + friend.getUid() + "/" + myUID + "/" + uuid, messageValue);
        //Message header
        childUpdates.put("/" + DBContract.ChatsTable.TABLE_NAME + "/" + myUID + "/" + friend.getUid(), chatValue);
        childUpdates.put("/" + DBContract.ChatsTable.TABLE_NAME + "/" + friend.getUid() + "/" + myUID, chatValue2);

        mDatabaseRef.updateChildren(childUpdates);
    }

    private void setGroupChat(String content) {
        SharedPreferenceHelper sharedPreferenceHelper = SharedPreferenceHelper.getInstance(getBaseContext());
        Users user = sharedPreferenceHelper.getUser();
        Friend me = new Friend(myUID, user.getFullName());
        Map messageValue = new Chat(me, content).toMap();
        Map chatHeaderValue1 = new ChatHeader(friend.getFullName(), me.getFullName() + ": " + content, true).toMap();
        //Map chatHeaderVaue2 = new ChatHeader(user.getFullName(), content, true).toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        String key = FirebaseDatabase.getInstance().getReference().push().getKey();
        //Message detail
        childUpdates.put("/" + DBContract.MessageTable.TABLE_NAME + "/" + myUID + "/" + friend.getUid() + "/" + key, messageValue);
        for (Friend groupMember : groupMembersList) {
            childUpdates.put("/" + DBContract.MessageTable.TABLE_NAME + "/" + groupMember.getUid() + "/" + friend.getUid() + "/" + key, messageValue);
        }
        //Message header
        childUpdates.put("/" + DBContract.ChatsTable.TABLE_NAME + "/" + myUID + "/" + friend.getUid(), chatHeaderValue1);
        for (Friend groupMember : groupMembersList) {
            childUpdates.put("/" + DBContract.ChatsTable.TABLE_NAME + "/" + groupMember.getUid() + "/" + friend.getUid(), chatHeaderValue1);
        }

        mDatabaseRef.updateChildren(childUpdates);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        handlerMenuAction();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }

    private void handlerMenuAction() {
        if (isGroupOwner) {
            deleteGroup();
        } else {
            leaveGroup();
        }
    }

    private void deleteGroup() {
        /*
        MULTI PATH DELETE
         */
        Map<String, Object> updates = new HashMap();
        //DELETE CURRENT GROUP FROM ALL USERS
        for (Friend friend : groupMembersList) {
            Log.d(TAG, "member: " + friend);
            updates.put("/" + DBContract.UserTable.TABLE_NAME + "/" + friend.getUid() + "/" + DBContract.UserTable.COL_NAME_GROUPS + "/" + group.getUid(), null);
            updates.put("/" + DBContract.ChatsTable.TABLE_NAME + "/" + friend.getUid() + "/" + group.getUid(), null);
            updates.put("/" + DBContract.MessageTable.TABLE_NAME + "/" + friend.getUid() + "/" + group.getUid(), null);
        }
        //DELETE CURRENT GROUP FROM GROUP MEMBER TABLE
        updates.put("/" + DBContract.GroupMemberTable.TABLE_NAME + "/" + group.getUid(), null);
        //DELETE CURRENT GROUP FROM GROUP TABLE
        updates.put("/" + DBContract.GroupTable.TABLE_NAME + "/" + group.getUid(), null);
        this.mDatabaseRef.updateChildren(updates);
        finish();
    }

    private void leaveGroup() {
        if (isGroup) {
            /*
            MULTI PATH DELETE
            */
            Map<String, Object> updates = new HashMap();
            //DELETE CURRENT GROUP FROM CURRENT USER
            updates.put("/" + DBContract.UserTable.TABLE_NAME + "/" + myUID + "/" + DBContract.UserTable.COL_NAME_GROUPS + "/" + group.getUid(), null);
            updates.put("/" + DBContract.ChatsTable.TABLE_NAME + "/" + myUID + "/" + group.getUid(), null);
            updates.put("/" + DBContract.MessageTable.TABLE_NAME + "/" + myUID + "/" + group.getUid(), null);
            //DELETE CURRENT GROUP FROM GROUP MEMBER TABLE
            updates.put("/" + DBContract.GroupMemberTable.TABLE_NAME + "/" + group.getUid() + "/" + myUID, null);
            this.mDatabaseRef.updateChildren(updates);
            finish();
        } else {
            /*
            MULTI PATH DELETE
            */
            Map<String, Object> updates = new HashMap();
            //DELETE CURRENT CHAT FROM CURRENT USER
            updates.put("/" + DBContract.ChatsTable.TABLE_NAME + "/" + myUID + "/" + friend.getUid(), null);
            updates.put("/" + DBContract.MessageTable.TABLE_NAME + "/" + myUID + "/" + friend.getUid(), null);
            this.mDatabaseRef.updateChildren(updates);
            finish();
        }
    }

    private void viewProfile() {
        if (isGroup) {
            String friendUID = friend.getUid();
            Query query = mDatabaseRef.child(DBContract.GroupTable.TABLE_NAME).
                    orderByKey().
                    equalTo(friendUID).limitToFirst(1);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "Querying group: " + dataSnapshot);
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String uid = snapshot.getKey();
                            String description = snapshot.child(DBContract.GroupTable.COL_NAME_DESCRIPTION).getValue(String.class);
                            String name = snapshot.child(DBContract.GroupTable.COL_NAME_NAME).getValue(String.class);
                            String ownerName = snapshot.child(DBContract.GroupTable.COL_NAME_OWNER_NAME).getValue(String.class);
                            String ownerUid = snapshot.child(DBContract.GroupTable.COL_NAME_OWNER_UID).getValue(String.class);
                            Intent i = new Intent(getApplicationContext(), CreateGroupActivity.class);
                            i.putExtra(Constants.GROUP_ACTIVITY_TYPE, Constants.GROUP_ACTIVITY_TYPE_VIEW);
                            i.putExtra(DBContract.GroupTable.COL_NAME_UID, uid);
                            i.putExtra(DBContract.GroupTable.COL_NAME_NAME, name);
                            i.putExtra(DBContract.GroupTable.COL_NAME_DESCRIPTION, description);
                            i.putExtra(DBContract.GroupTable.COL_NAME_OWNER_NAME, ownerName);
                            i.putExtra(DBContract.GroupTable.COL_NAME_OWNER_UID, ownerUid);
                            startActivityForResult(i, Constants.GROUP_ACTIVITY_TYPE_EDIT);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        } else {
            String friendUID = friend.getUid();
            Query query = mDatabaseRef.child(DBContract.UserTable.TABLE_NAME).
                    orderByKey().
                    equalTo(friendUID).limitToFirst(1);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "Querying profile: " + dataSnapshot);
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String uid = snapshot.getKey();
                            String favoriteSports = "";
                            String fullName = snapshot.child(DBContract.UserTable.COL_NAME_FULLNAME).getValue(String.class);
                            String nickName = snapshot.child(DBContract.UserTable.COL_NAME_NICKNAME).getValue(String.class);
                            String aboutUser = snapshot.child(DBContract.UserTable.COL_NAME_ABOUT).getValue(String.class);
                            String facebookId = snapshot.child(DBContract.UserTable.COL_NAME_FACEBOOK_ID).getValue(String.class);
                            if (dataSnapshot.child(DBContract.UserTable.COL_NAME_SPORTS).getValue() != null) {
                                long childrenSport = dataSnapshot.child(DBContract.UserTable.COL_NAME_SPORTS).getChildrenCount();
                                long childrenCount = 1;
                                for (DataSnapshot childSnapshot : dataSnapshot.child(DBContract.UserTable.COL_NAME_SPORTS).getChildren()) {
                                    Long sportIndex = (Long) childSnapshot.getValue();
                                    List<String> myArrayList = Arrays.asList(getResources().getStringArray(R.array.sports_array));
                                    for (int i = 0; i <= myArrayList.size(); i++) {
                                        if (sportIndex == i) {
                                            if (childrenCount < childrenSport) {
                                                favoriteSports = favoriteSports + myArrayList.get(i) + " - ";
                                                childrenCount++;
                                            } else {
                                                favoriteSports = favoriteSports + myArrayList.get(i);
                                                childrenCount++;
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                            Intent i = new Intent(getApplicationContext(), ViewProfile.class);
                            i.putExtra(DBContract.UserTable.COL_NAME_FULLNAME, fullName);
                            i.putExtra(DBContract.UserTable.COL_NAME_NICKNAME, nickName);
                            i.putExtra(DBContract.UserTable.COL_NAME_ABOUT, aboutUser);
                            i.putExtra(DBContract.UserTable.COL_NAME_SPORTS, favoriteSports);
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

    private void showFriendsDialog(int option) {
        Intent i = new Intent(this, FriendsActivity.class);
        i.putExtra(RequestCode.INVITED_FRIENDS_REQUEST.getDescription(), RequestCode.INVITED_FRIENDS_REQUEST.getCode());
        switch (option) {
            case ADD_MEMBER_REQUEST: {
                startActivityForResult(i, ADD_MEMBER_REQUEST);
                break;
            }
            case REMOVE_MEMBER_REQUEST: {
                i.putExtra(DBContract.GroupTable.COL_NAME_UID, group.getUid());
                i.putExtra(DBContract.GroupTable.COL_NAME_OWNER_UID, group.getOwnerUid());
                startActivityForResult(i, REMOVE_MEMBER_REQUEST);
                break;
            }
        }
    }

    private void addMember(ArrayList<Friend> newMembers) {
        newMembers.removeAll(groupMembersList);
        //Create welcome message for members
        String newpMemberMessage = getString(R.string.create_group_new_member_message) + " " + group.getGroupName();
        Map<String, Object> childUpdates = new HashMap<>();
        Map chatValue = new ChatHeader(group.getGroupName(), newpMemberMessage, true).toMap();
        //ADD members to the group
        for (Friend friend : newMembers) {
            childUpdates.put("/" + DBContract.UserGroupsTable.TABLE_NAME + "/" + friend.getUid() + "/" + group.getUid(), group.getGroupName());
            childUpdates.put("/" + DBContract.ChatsTable.TABLE_NAME + "/" + friend.getUid() + "/" + group.getUid(), chatValue);
            childUpdates.put("/" + DBContract.GroupMemberTable.TABLE_NAME + "/" + group.getUid() + "/" + friend.getUid(), friend.getFullName());
        }
        mDatabaseRef.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    String message = getString(R.string.add_new_groupmember_message_failed);
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                } else {
                    String message = getString(R.string.add_new_groupmember_message_succes);
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void removeMember(ArrayList<Friend> membersToRemove) {
        Map<String, Object> childUpdates = new HashMap<>();
        String removedMemberMessage = getString(R.string.create_group_removed_member_message);
        Map chatValue = new ChatHeader(group.getGroupName(), removedMemberMessage, true).toMap();
        //ADD members to the group
        for (Friend friend : membersToRemove) {
            childUpdates.put("/" + DBContract.UserGroupsTable.TABLE_NAME + "/" + friend.getUid() + "/" + group.getUid(), null);
            childUpdates.put("/" + DBContract.ChatsTable.TABLE_NAME + "/" + friend.getUid() + "/" + group.getUid(), chatValue);
            childUpdates.put("/" + DBContract.GroupMemberTable.TABLE_NAME + "/" + group.getUid() + "/" + friend.getUid(), null);
        }
        mDatabaseRef.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    String message = getString(R.string.remove_groupmember_message_failed);
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                } else {
                    String message = getString(R.string.remove_groupmember_message_succes);
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.GROUP_ACTIVITY_TYPE_EDIT: {
                if (resultCode == RESULT_OK) {
                    String title = data.getExtras().getString(DBContract.GroupTable.COL_NAME_NAME);
                    this.tvTitle.setText(title);
                }
                break;
            }
            case ADD_MEMBER_REQUEST: {
                if (resultCode == RESULT_OK) {
                    String json = data.getStringExtra(RequestCode.INVITED_FRIENDS_REQUEST.getDescription());
                    try {
                        JSONArray jsonFriendArray = new JSONArray(json);
                        ArrayList<Friend> newMembers = new ArrayList<>();
                        for (int i = 0; i < jsonFriendArray.length(); i++) {
                            JSONObject jsonFriend = jsonFriendArray.getJSONObject(i);
                            String uid = jsonFriend.getString(jsonFriend.keys().next());
                            String name = jsonFriend.getString(DBContract.UserTable.COL_NAME_FULLNAME);
                            newMembers.add(new Friend(uid, name));
                        }
                        addMember(newMembers);
                    } catch (Throwable ignored) {
                    }
                }
                break;
            }
            case REMOVE_MEMBER_REQUEST: {
                if (resultCode == RESULT_OK) {
                    String json = data.getStringExtra(RequestCode.INVITED_FRIENDS_REQUEST.getDescription());
                    try {
                        JSONArray jsonFriendArray = new JSONArray(json);
                        ArrayList<Friend> selectedMembers = new ArrayList<>();
                        for (int i = 0; i < jsonFriendArray.length(); i++) {
                            JSONObject jsonFriend = jsonFriendArray.getJSONObject(i);
                            String uid = jsonFriend.getString(jsonFriend.keys().next());
                            String name = jsonFriend.getString(DBContract.UserTable.COL_NAME_FULLNAME);
                            selectedMembers.add(new Friend(uid, name));
                        }
                        removeMember(selectedMembers);
                    } catch (Throwable ignored) {
                    }
                }
                break;
            }
        }
    }
}
