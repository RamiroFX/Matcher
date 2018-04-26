package com.matcher.matcher.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.matcher.matcher.R;
import com.matcher.matcher.Utils.DBContract;
import com.matcher.matcher.Utils.SharedPreferenceHelper;
import com.matcher.matcher.adapters.MessageAdapter;
import com.matcher.matcher.entities.Chat;
import com.matcher.matcher.entities.ChatHeader;
import com.matcher.matcher.entities.Friend;
import com.matcher.matcher.entities.Users;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";
    private DatabaseReference databaseRef;
    //private ChildEventListener chatsListRef; reemplazado por query para ordernar por tiempo los mensajes
    //private Query chatsListRef;
    private Friend friend;
    private boolean userAvatarRdy, friendAvatarRdy;

    private RecyclerView chatList;
    private EditText etMessage;
    private Button btnSend;
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = findViewById(R.id.chatActivityToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        this.databaseRef = FirebaseDatabase.getInstance().getReference();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String friendUid = "", username = "";
            if (!extras.getString(DBContract.UserTable.COL_NAME_UID, "").isEmpty()) {
                friendUid = extras.getString(DBContract.UserTable.COL_NAME_UID);
            }
            if (!extras.getString(DBContract.UserTable.COL_NAME_FULLNAME, "").isEmpty()) {
                username = extras.getString(DBContract.UserTable.COL_NAME_FULLNAME);
                setTitle(username);
            }
            String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            this.friend = new Friend(friendUid, username);
            retrieveChats();
            Users user = new Users();
            user.setUid(userUid);
            this.messageAdapter = new MessageAdapter(user, friend);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        retrieveChats();
        listenChats();
    }

    @Override
    protected void onStop() {
        super.onStop();
        /*if (chatsListRef != null) {
            //chatsListRef.removeEventListener(chatsListRef);
        }*/
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
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Query query = databaseRef.child(DBContract.MessageTable.TABLE_NAME).child(uid).child(friend.getUid()).
                orderByChild(DBContract.MessageTable.COL_NAME_TIMESTAMP);
        query.addChildEventListener(
                new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Log.d(TAG, "onChildAdded: " + dataSnapshot);
                        Chat chat = dataSnapshot.getValue(Chat.class);
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

    private void retrieveChats() {
        chatList.setAdapter(messageAdapter);
        chatList.setLayoutManager(linearLayoutManager);
    }

    public void sendMessage() {
        String content = etMessage.getText().toString();
        etMessage.setText("");
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
        setChat(content);
    }

    public void setChat(String content) {
        SharedPreferenceHelper sharedPreferenceHelper = SharedPreferenceHelper.getInstance(getBaseContext());
        Users anUser = sharedPreferenceHelper.getUser();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map messageValue = new Chat(uid, content).toMap();
        Map chatValue = new ChatHeader(friend.getUsername(), content).toMap();
        Map chatValue2 = new ChatHeader(anUser.getFullName(), content).toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        String uuid = UUID.randomUUID().toString();
        //Message detail
        childUpdates.put("/" + DBContract.MessageTable.TABLE_NAME + "/" + uid + "/" + friend.getUid() + "/" + uuid, messageValue);
        childUpdates.put("/" + DBContract.MessageTable.TABLE_NAME + "/" + friend.getUid() + "/" + uid + "/" + uuid, messageValue);
        //Message header
        childUpdates.put("/" + DBContract.ChatsTable.TABLE_NAME + "/" + uid + "/" + friend.getUid(), chatValue);
        childUpdates.put("/" + DBContract.ChatsTable.TABLE_NAME + "/" + friend.getUid() + "/" + uid, chatValue2);

        databaseRef.updateChildren(childUpdates);
    }

}
