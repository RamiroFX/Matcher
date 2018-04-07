package com.matcher.matcher.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import com.matcher.matcher.R;
import com.matcher.matcher.Utils.DBContract;
import com.matcher.matcher.adapters.ChatAdapter;
import com.matcher.matcher.entities.Chat;
import com.matcher.matcher.entities.Friend;
import com.matcher.matcher.entities.Users;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    private DatabaseReference databaseRef;
    private ChildEventListener chatsListRef;
    private Friend friend;

    private RecyclerView chatList;
    private EditText etMessage;
    private Button btnSend;
    private LinearLayoutManager linearLayoutManager;
    private ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        databaseRef = FirebaseDatabase.getInstance().getReference();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String uid="", username="";
            if(!extras.getString(DBContract.UserTable.COL_NAME_UID,"").isEmpty()){
                uid=extras.getString(DBContract.UserTable.COL_NAME_UID);
            }
            if(!extras.getString(DBContract.UserTable.COL_NAME_FULLNAME,"").isEmpty()){
                username=extras.getString(DBContract.UserTable.COL_NAME_FULLNAME);
                setTitle(username);
            }
            this.friend = new Friend(username,uid);
            retrieveChats();
            Users user= new Users();
            user.setFullName("test");
            this.chatAdapter = new ChatAdapter(user);
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
        if(chatsListRef != null) {
            databaseRef.removeEventListener(chatsListRef);
        }
    }

    private void listenChats() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        chatsListRef = databaseRef.child("chats").child(uid).child(friend.getUid()).addChildEventListener(
                new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Chat chat = dataSnapshot.getValue(Chat.class);
                        chatAdapter.onChatAdded(chat);
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

    private void retrieveChats(){
        chatList.setAdapter(chatAdapter);
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
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map chatValues = new Chat(uid, content).toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        String uuid = UUID.randomUUID().toString();
        childUpdates.put("/chats/" + uid + "/" + friend.getUid() + "/" + uuid, chatValues);
        childUpdates.put("/chats/" + friend.getUid() + "/" + uid + "/" + uuid, chatValues);

        databaseRef.updateChildren(childUpdates);
    }
}
