package com.matcher.matcher.fragments;


import android.content.Intent;
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
import com.matcher.matcher.activities.CreateEventActivity;
import com.matcher.matcher.activities.MainActivity;
import com.matcher.matcher.adapters.EventsAdapter;
import com.matcher.matcher.entities.Event;
import com.matcher.matcher.entities.Friend;

import org.json.JSONArray;
import org.json.JSONObject;

public class EventsFragment extends Fragment {

    private static final String TAG = "EventsFragment";

    private RecyclerView eventList;
    private EventsAdapter eventsAdapter;
    private DatabaseReference databaseRef;

    private ChildEventListener eventListRef;
    private OnEventFragmentInteraction mListener;

    public interface OnEventFragmentInteraction{
        void onEventInteraction(Event event);
    }


    public static EventsFragment newInstance(OnEventFragmentInteraction mListener) {
        EventsFragment fragment = new EventsFragment();
        fragment.setListener(mListener);
        return fragment;
    }

    public EventsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.databaseRef = FirebaseDatabase.getInstance().getReference().child(DBContract.UserTable.TABLE_NAME).child(uid).child(DBContract.UserTable.COL_NAME_EVENTS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event, container, false);
        FloatingActionButton fab = view.findViewById(R.id.fab_event);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), CreateEventActivity.class);
                startActivity(i);
            }
        });
        this.eventList = view.findViewById(R.id.rvEventList);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.eventsAdapter = new EventsAdapter(((MainActivity) getActivity()));
    }

    @Override
    public void onResume() {
        super.onResume();
        retrieveEvents();
        listenEvents();
    }

    @Override
    public void onPause() {
        super.onPause();
        eventsAdapter.clearList();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (eventListRef != null) {
            databaseRef.removeEventListener(eventListRef);
        }
    }

    private void retrieveEvents() {
        eventList.setAdapter(eventsAdapter);
        eventList.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    public void listenEvents() {
        Log.e(TAG, "listenEvents");
        eventListRef = databaseRef.addChildEventListener(
                new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Event anEvent=null;
                        try {
                            Log.e(TAG, "listenEvents: "+dataSnapshot);
                            JSONObject eventDetail= new JSONObject(dataSnapshot.getValue()+"");
                            String eventUID = dataSnapshot.getKey();
                            String eventName = eventDetail.getString(DBContract.EventsTable.COL_NAME_NAME);
                            String eventSchedule = eventDetail.getString(DBContract.EventsTable.COL_NAME_SCHEDULED_TIME);
                            anEvent = new Event();
                            anEvent.setUid(eventUID);
                            anEvent.setEventName(eventName);
                            anEvent.setScheduledTime(Long.valueOf(eventSchedule));
                        } catch (Throwable t) {
                            Log.e(TAG, "Could not parse malformed JSON: \"" + dataSnapshot.getValue() + "\"");
                        }
                        eventsAdapter.onEventAdded(anEvent);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        Event anEvent=null;
                        try {
                            JSONObject eventDetail= new JSONObject(dataSnapshot.getValue()+"");
                            String eventUID = dataSnapshot.getKey();
                            String eventName = eventDetail.getString(DBContract.EventsTable.COL_NAME_NAME);
                            String eventSchedule = eventDetail.getString(DBContract.EventsTable.COL_NAME_SCHEDULED_TIME);
                            anEvent = new Event();
                            anEvent.setUid(eventUID);
                            anEvent.setEventName(eventName);
                            anEvent.setScheduledTime(Long.valueOf(eventSchedule));
                        } catch (Throwable t) {
                            Log.e(TAG, "Could not parse malformed JSON: \"" + dataSnapshot.getValue() + "\"");
                        }
                        eventsAdapter.onEventChanged(anEvent);
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

    public void setListener(OnEventFragmentInteraction mListener) {
        this.mListener = mListener;
    }
}
