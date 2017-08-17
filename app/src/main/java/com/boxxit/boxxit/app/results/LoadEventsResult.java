package com.boxxit.boxxit.app.results;

import com.boxxit.boxxit.library.parse.models.facebook.Profile;

import java.util.List;

public enum  LoadEventsResult implements Result {
    LOADING,
    SUCCESS,
    ERROR;

    public List<Profile> events;
    public Throwable error;

    public static LoadEventsResult success(List<Profile> events) {
        LoadEventsResult result = SUCCESS;
        result.events = events;
        return result;
    }

    public static LoadEventsResult error(Throwable error) {
        LoadEventsResult result = ERROR;
        result.error = error;
        return result;
    }
}
