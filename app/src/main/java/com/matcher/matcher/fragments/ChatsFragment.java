package com.matcher.matcher.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.matcher.matcher.R;
import com.matcher.matcher.Utils.DBContract;
import com.matcher.matcher.Utils.RequestCode;
import com.matcher.matcher.activities.ChatActivity;
import com.matcher.matcher.activities.FriendsActivity;
import com.matcher.matcher.activities.MainActivity;
import com.matcher.matcher.adapters.ChatsAdapter;
import com.matcher.matcher.entities.ChatHeader;


public class ChatsFragment extends Fragment {

    private static final String TAG = "ChatsFragment";
    private static final int CHAT_FRIEND_REQUEST = 6;
    private OnFragmentInteractionListener mListener;

    private RecyclerView chatsList;
    private ChatsAdapter chatsAdapter;
    private DatabaseReference databaseRef;

    private ChildEventListener chatListRef;


    public ChatsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.databaseRef = FirebaseDatabase.getInstance().getReference().child(DBContract.ChatsTable.TABLE_NAME).child(uid);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab_chat);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), FriendsActivity.class);
                i.putExtra(RequestCode.CHAT_FRIENDS_REQUEST.getDescription(), RequestCode.CHAT_FRIENDS_REQUEST.getCode());
                startActivityForResult(i, CHAT_FRIEND_REQUEST);
                //startActivity(i);
            }
        });
        this.chatsList = (RecyclerView) view.findViewById(R.id.rvChatsList);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");
        //((MainActivity)getActivity())
        this.chatsAdapter = new ChatsAdapter(((MainActivity) getActivity()));
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
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

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        chatsAdapter.clearList();
        retrieveChats();
        listenChats();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        chatsAdapter.clearList();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        if (chatListRef != null) {
            databaseRef.removeEventListener(chatListRef);
        }
    }

    private void retrieveChats() {
        chatsList.setAdapter(chatsAdapter);
        chatsList.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    public void listenChats() {
        chatListRef = databaseRef.addChildEventListener(
                new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Log.d(TAG, "onChildAdded :" + dataSnapshot.getValue());
                        Log.d(TAG, "onChildAdded.getKey: " + dataSnapshot.getKey());
                        ChatHeader chatHeader = dataSnapshot.getValue(ChatHeader.class);
                        chatHeader.setUid(dataSnapshot.getKey());
                        chatsAdapter.onChatAdded(chatHeader);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        Log.d(TAG, "onChildChanged :" + dataSnapshot.getValue());
                        ChatHeader chatHeader = dataSnapshot.getValue(ChatHeader.class);
                        chatHeader.setUid(dataSnapshot.getKey());
                        chatsAdapter.onChatChanged(chatHeader);
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
