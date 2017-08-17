package com.boxxit.boxxit.app.activities.main;

import com.boxxit.boxxit.library.parse.models.facebook.Profile;

import java.util.ArrayList;
import java.util.List;

public class MainUIState {
    Profile profile;
    List<Profile> events;
    Throwable error;
    boolean isLoading;
    boolean profileSuccess;
    boolean eventsSuccess;
    boolean eventsEmpty;
    boolean gotoExplore;

    public MainUIState(Profile profile, List<Profile> events, Throwable error, boolean isLoading, boolean profileSuccess, boolean eventsSuccess, boolean eventsEmpty, boolean gotoExplore) {
        this.profile = profile;
        this.events = events;
        this.error = error;
        this.isLoading = isLoading;
        this.profileSuccess = profileSuccess;
        this.eventsSuccess = eventsSuccess;
        this.eventsEmpty = eventsEmpty;
        this.gotoExplore = gotoExplore;
    }

    public static MainUIState initial () {
        return new MainUIState(null, null, null, false, false, false, false, false);
    }

    public static MainUIState isLoading () {
        return new MainUIState(null, null, null, true, false, false, false, false);
    }

    public static MainUIState error (Throwable error) {
        return new MainUIState(null, null, error, false, false, false, false, false);
    }

    public static MainUIState profileSuccess (Profile profile) {
        return new MainUIState(profile, null, null, false, true, false, false, false);
    }

    public static MainUIState eventsSuccess (List<Profile> events) {
        return new MainUIState(null, events, null, false, false, true, false, false);
    }

    public static MainUIState eventsEmpty () {
        return new MainUIState(null, new ArrayList<>(), null, false, false, false, true, false);
    }

    public static MainUIState gotoExplore () {
        return new MainUIState(null, null, null, false, false, false, false, true);
    }
}
