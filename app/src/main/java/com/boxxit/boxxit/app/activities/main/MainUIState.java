package com.boxxit.boxxit.app.activities.main;

import com.boxxit.boxxit.library.parse.models.facebook.Profile;

import java.util.ArrayList;
import java.util.List;

public enum MainUIState {
    INITIAL,
    PROFILE_SUCCESS,
    PROFILE_ERROR,
    EVENTS_LOADING,
    EVENTS_SUCCESS,
    EVENTS_EMPTY,
    EVENTS_ERROR,
    GOTO_EXPLORE,
    PRESENT_TUTORIAL;

    Profile profile;
    List<Profile> events = new ArrayList<>();
    Throwable throwable;

    public static MainUIState PROFILE_SUCCESS (Profile profile) {
        MainUIState result = PROFILE_SUCCESS;
        result.profile = profile;
        return result;
    }

    public static MainUIState PROFILE_ERROR (Throwable throwable) {
        MainUIState result = PROFILE_ERROR;
        result.throwable = throwable;
        return result;
    }


    public static MainUIState EVENTS_SUCCESS (List<Profile> events) {
        MainUIState result = EVENTS_SUCCESS;
        result.events = events;
        return result;
    }

    public static MainUIState EVENTS_ERROR (Throwable throwable) {
        MainUIState result = EVENTS_ERROR;
        result.throwable = throwable;
        return result;
    }
}
