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
import com.matcher.matcher.Utils.SharedPreferenceHelper;
import com.matcher.matcher.activities.CreateDuelActivity;
import com.matcher.matcher.activities.CreateEventActivity;
import com.matcher.matcher.activities.MainActivity;
import com.matcher.matcher.adapters.EventsAdapter;
import com.matcher.matcher.entities.Challenge;
import com.matcher.matcher.entities.Event;
import com.matcher.matcher.entities.EventGroup;
import com.matcher.matcher.entities.EventParticipant;
import com.matcher.matcher.interfaces.LogAnalyticEventListener;

import java.util.HashMap;
import java.util.Map;

public class EventsFragment extends Fragment {

    private static final String TAG = "EventsFragment";

    private RecyclerView eventList;
    private EventsAdapter eventsAdapter;
    private DatabaseReference eventsRef;
    private ChildEventListener eventListRef;
    private String myUID;
    private SharedPreferenceHelper sharedPreferenceHelper;
    private LogAnalyticEventListener mListener;

    public static EventsFragment newInstance() {
        EventsFragment fragment = new EventsFragment();
        return fragment;
    }

    public EventsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferenceHelper = SharedPreferenceHelper.getInstance(getContext());
        myUID = sharedPreferenceHelper.getUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        this.eventsRef = databaseReference.child(DBContract.UserEventsTable.TABLE_NAME).child(myUID);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event, container, false);
        FloatingActionButton fabEvents = view.findViewById(R.id.fab_event);
        FloatingActionButton fabDuel = view.findViewById(R.id.fab_duel);
        fabEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), CreateEventActivity.class);
                startActivity(i);
            }
        });
        fabDuel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), CreateDuelActivity.class);
                i.putExtra(Constants.CHALLENGE_ACTIVITY_TYPE, Constants.CHALLENGE_ACTIVITY_TYPE_CREATE);
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_event_tab, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_event_item_clear_location: {
                clearLocations();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        retrieveEvents();
        listenEvents();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        eventsAdapter.clearList();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
        if (eventListRef != null) {
            eventsRef.removeEventListener(eventListRef);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof LogAnalyticEventListener) {
            mListener = (LogAnalyticEventListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement LogAnalyticEventListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void retrieveEvents() {
        eventList.setAdapter(eventsAdapter);
        eventList.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    public void listenEvents() {
        Log.d(TAG, "listenEvents");
        eventListRef = eventsRef.addChildEventListener(
                new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String eventType = dataSnapshot.child(DBContract.EventsTable.COL_NAME_EVENT_TYPE).getValue(String.class);
                        if (eventType != null && !eventType.isEmpty()) {
                            if (eventType.equals(DBContract.EventsTable.COL_NAME_GROUP_EVENT)) {
                                EventGroup anEventGroup = dataSnapshot.getValue(EventGroup.class);
                                anEventGroup.setUid(dataSnapshot.getKey());
                                eventsAdapter.onEventAdded(anEventGroup);
                            } else if (eventType.equals(DBContract.ChallengeTable.COL_NAME_CHALLENGE_EVENT)) {
                                Challenge aChallenge = dataSnapshot.getValue(Challenge.class);
                                aChallenge.setUid(dataSnapshot.getKey());
                                eventsAdapter.onChallengeAdded(aChallenge);
                            }
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        String eventType = dataSnapshot.child(DBContract.EventsTable.COL_NAME_EVENT_TYPE).getValue(String.class);
                        if (eventType != null && !eventType.isEmpty()) {
                            if (eventType.equals(DBContract.EventsTable.COL_NAME_GROUP_EVENT)) {
                                EventGroup anEventGroup = dataSnapshot.getValue(EventGroup.class);
                                anEventGroup.setUid(dataSnapshot.getKey());
                                eventsAdapter.onEventChanged(anEventGroup);
                            } else if (eventType.equals(DBContract.ChallengeTable.COL_NAME_CHALLENGE_EVENT)) {
                                Challenge aChallenge = dataSnapshot.getValue(Challenge.class);
                                aChallenge.setUid(dataSnapshot.getKey());
                                eventsAdapter.onChallengeChanged(aChallenge);
                            }
                        }
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        String eventType = dataSnapshot.child(DBContract.EventsTable.COL_NAME_EVENT_TYPE).getValue(String.class);
                        if (eventType != null && !eventType.isEmpty()) {
                            if (eventType.equals(DBContract.EventsTable.COL_NAME_GROUP_EVENT)) {
                                EventGroup anEventGroup = dataSnapshot.getValue(EventGroup.class);
                                anEventGroup.setUid(dataSnapshot.getKey());
                                eventsAdapter.onEventRemoved(anEventGroup);
                            } else if (eventType.equals(DBContract.ChallengeTable.COL_NAME_CHALLENGE_EVENT)) {
                                Challenge aChallenge = dataSnapshot.getValue(Challenge.class);
                                aChallenge.setUid(dataSnapshot.getKey());
                                eventsAdapter.onChallengeRemoved(aChallenge);
                            }
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

    private void clearLocations() {
        Log.d(TAG, "ClearLocations");
        if (mListener != null) {
            mListener.logAnalyticEvent(Constants.EVENTS_CLEAR_LOC_EVENT, DBContract.EventsTable.TABLE_NAME);
        }
        String myName = sharedPreferenceHelper.getUser().getFullName();
        if (myName != null && !myName.isEmpty()) {
            EventParticipant eventParticipant = new EventParticipant();
            eventParticipant.setFullName(myName);
            eventParticipant.setStatus(DBContract.EventsParticipantsTable.COL_NAME_PARTICIPANT_STATUS_PRESENT);
            eventParticipant.setLatitude(0.0);
            eventParticipant.setLongitude(0.0);
            Map<String, Object> updates = new HashMap<>();
            for (Event anEventGroup : eventsAdapter.getmValues()) {
                if (anEventGroup.getEventType().equals(DBContract.EventsTable.COL_NAME_GROUP_EVENT)) {
                    updates.put("/" + DBContract.EventsParticipantsTable.TABLE_NAME + "/" + anEventGroup.getUid() + "/" + myUID, eventParticipant);
                }
            }
            DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference();
            mDatabaseReference.updateChildren(updates);
            Log.d(TAG, "Clearing locations in firebase");
        }
    }

}
