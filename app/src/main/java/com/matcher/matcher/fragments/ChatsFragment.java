package com.matcher.matcher.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.matcher.matcher.R;
import com.matcher.matcher.Utils.Constants;
import com.matcher.matcher.Utils.DBContract;
import com.matcher.matcher.Utils.RequestCode;
import com.matcher.matcher.Utils.SharedPreferenceHelper;
import com.matcher.matcher.activities.ChatActivity;
import com.matcher.matcher.activities.CreateGroupActivity;
import com.matcher.matcher.activities.FriendsActivity;
import com.matcher.matcher.activities.MainActivity;
import com.matcher.matcher.adapters.ChatsAdapter;
import com.matcher.matcher.entities.ChatHeader;
import com.matcher.matcher.interfaces.LogAnalyticEventListener;


public class ChatsFragment extends Fragment {

    private static final String TAG = "ChatsFragment";
    private static final int CHAT_FRIEND_REQUEST = 6;
    private LogAnalyticEventListener mListener;

    private RecyclerView chatsList;
    private ChatsAdapter chatsAdapter;
    private DatabaseReference chatsRef;

    private ChildEventListener chatListRef;


    public ChatsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        SharedPreferenceHelper sharedPreferenceHelper = SharedPreferenceHelper.getInstance(getContext());
        String uid = sharedPreferenceHelper.getUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        this.chatsRef = databaseReference.child(DBContract.ChatsTable.TABLE_NAME).child(uid);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        FloatingActionButton fab = view.findViewById(R.id.fab_chat);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), FriendsActivity.class);
                i.putExtra(RequestCode.CHAT_FRIENDS_REQUEST.getDescription(), RequestCode.CHAT_FRIENDS_REQUEST.getCode());
                startActivityForResult(i, CHAT_FRIEND_REQUEST);
            }
        });
        this.chatsList = view.findViewById(R.id.rvChatsList);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");
        this.chatsAdapter = new ChatsAdapter(((MainActivity) getActivity()));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_chat_tab, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_create_group: {
                createGroup();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void createGroup() {
        Intent i = new Intent(getContext(), CreateGroupActivity.class);
        i.putExtra(Constants.GROUP_ACTIVITY_TYPE, Constants.GROUP_ACTIVITY_TYPE_CREATE);
        startActivity(i);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof LogAnalyticEventListener) {
            mListener = (LogAnalyticEventListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        retrieveChats();
        listenChats();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        chatsAdapter.clearList();
        if (chatListRef != null) {
            chatsRef.removeEventListener(chatListRef);
        }
    }

    private void retrieveChats() {
        chatsList.setAdapter(chatsAdapter);
        chatsList.setLayoutManager(new LinearLayoutManager(getActivity()));
    }


    public void listenChats() {
        chatListRef = chatsRef.addChildEventListener(
                new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Log.d(TAG, "listenChats.onChildAdded: " + dataSnapshot);
                        ChatHeader chatHeader = dataSnapshot.getValue(ChatHeader.class);
                        if (chatHeader != null) {
                            chatHeader.setUid(dataSnapshot.getKey());
                            chatsAdapter.onChatAdded(chatHeader);
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        Log.d(TAG, "listenChats.onChildChanged: " + dataSnapshot);
                        ChatHeader chatHeader = dataSnapshot.getValue(ChatHeader.class);
                        if (chatHeader != null) {
                            chatHeader.setUid(dataSnapshot.getKey());
                            chatsAdapter.onChatChanged(chatHeader);
                        }
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        ChatHeader chatHeader = dataSnapshot.getValue(ChatHeader.class);
                        if (chatHeader != null) {
                            chatHeader.setUid(dataSnapshot.getKey());
                            chatsAdapter.onChatRemoved(chatHeader);
                        }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CHAT_FRIEND_REQUEST: {
                Log.d(TAG, "resultCode: " + resultCode);
                showChatActivity(data);
            }
        }
    }

    private void showChatActivity(Intent data) {
        if (mListener != null) {
            mListener.logAnalyticEvent(Constants.CHAT_SELECT_CHAT_EVENT, DBContract.ChatsTable.TABLE_NAME);
        }
        if (data == null) {
            Log.d(TAG, "data es null");
            return;
        }
        Bundle extras = data.getExtras();
        if (extras != null) {
            String friendUid = "", fullName = "";
            if (!extras.getString(DBContract.UserTable.COL_NAME_UID, "").isEmpty()) {
                friendUid = extras.getString(DBContract.UserTable.COL_NAME_UID);
            } else {
                return;
            }
            if (!extras.getString(DBContract.UserTable.COL_NAME_FULLNAME, "").isEmpty()) {
                fullName = extras.getString(DBContract.UserTable.COL_NAME_FULLNAME);
            } else {
                return;
            }
            Intent i = new Intent(getContext(), ChatActivity.class);
            i.putExtra(DBContract.UserTable.COL_NAME_FULLNAME, fullName);
            i.putExtra(DBContract.UserTable.COL_NAME_UID, friendUid);
            startActivity(i);
        }
    }
}
