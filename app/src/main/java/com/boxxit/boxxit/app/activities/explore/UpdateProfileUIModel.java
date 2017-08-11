package com.boxxit.boxxit.app.activities.explore;

import com.boxxit.boxxit.library.parse.models.facebook.Profile;

public class UpdateProfileUIModel {
    boolean success;
    boolean inProgress;
    String error;
    Profile profile;

    private UpdateProfileUIModel(boolean success, boolean inProgress, Profile profile, String error) {
        this.success = success;
        this.inProgress = inProgress;
        this.error = error;
        this.profile = profile;
    }

    static UpdateProfileUIModel inProgress () {
        return new UpdateProfileUIModel(false, true, null, null);
    }

    static UpdateProfileUIModel success (Profile profile) {
        return new UpdateProfileUIModel(true, false, profile, null);
    }

    static UpdateProfileUIModel error (Throwable throwable) {
        return new UpdateProfileUIModel(false, false, null, throwable.getLocalizedMessage());
    }
}
