package com.matcher.matcher.interfaces;


import com.matcher.matcher.entities.EventGroup;
import com.matcher.matcher.entities.EventParticipant;

public interface GroupEventDetailListener {
    void setParticipantStatus(String uid, String status);

    void addParticipant(EventParticipant eventParticipant);

    void setEventFields(EventGroup eventGroup);
}
