package com.matcher.matcher.interfaces;

public interface LogAnalyticEventListener {
    void logAnalyticEvent(String name, String contentType);

    void setUserProperty(String property, String sportUid);
}
