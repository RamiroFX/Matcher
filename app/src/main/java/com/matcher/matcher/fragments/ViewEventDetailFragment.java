package com.matcher.matcher.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.matcher.matcher.R;
import com.matcher.matcher.adapters.EventParticipantsAdapter;
import com.matcher.matcher.entities.EventGroup;
import com.matcher.matcher.entities.EventParticipant;
import com.matcher.matcher.interfaces.GroupEventDetailListener;

import java.util.List;


public class ViewEventDetailFragment extends Fragment {

    private static final String TAG = "ViewEventDetailFragment";
    private TextView tvEventName, tvEventDescription, tvEventSchedule, tvEventOwnerName, tvEventPlace;
    private EventParticipantsAdapter eventParticipantsAdapter;
    private RecyclerView rvParticipants;

    public ViewEventDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_view_event_detail, container, false);
        v.setId(R.id.view_event_frag_top);
        tvEventName = v.findViewById(R.id.tv_view_event_name_content);
        tvEventDescription = v.findViewById(R.id.tv_view_event_description_content);
        tvEventSchedule = v.findViewById(R.id.tv_view_event_schedule_content);
        rvParticipants = v.findViewById(R.id.rvEventParticipants);
        tvEventOwnerName = v.findViewById(R.id.tv_view_event_owner_content);
        tvEventPlace = v.findViewById(R.id.tv_view_event_place_content);
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG,"onAttach: "+context);
        Log.d(TAG,"getTag: "+getTag());
    }

    @Override
    public void onResume() {
        super.onResume();
        rvParticipants.setAdapter(eventParticipantsAdapter);
        rvParticipants.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.eventParticipantsAdapter = new EventParticipantsAdapter();
    }

    public void setEventFields(EventGroup eventGroup) {
        Log.d(TAG,"setEventFields: "+eventGroup);
        tvEventName.setText(eventGroup.getEventName());
        tvEventDescription.setText(eventGroup.getDescription());
        tvEventSchedule.setText(eventGroup.eventSchedule());
        tvEventOwnerName.setText(eventGroup.getOwner().getFullName());
        tvEventPlace.setText(eventGroup.getPlaceName());
    }

    public void setEventParticipantsList(List<EventParticipant> participantsList) {
        this.eventParticipantsAdapter.setmValues(participantsList);
    }

    public void setParticipantStatus(String uid, String status) {
        eventParticipantsAdapter.setEventParticipantStatus(uid, status);
    }

    public void addParticipant(EventParticipant eventParticipant) {
        this.eventParticipantsAdapter.onEventParticipantAdded(eventParticipant);
    }

    public EventParticipantsAdapter getEventParticipantsAdapter() {
        return eventParticipantsAdapter;
    }
}
