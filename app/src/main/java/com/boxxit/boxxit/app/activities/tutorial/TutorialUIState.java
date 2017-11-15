package com.boxxit.boxxit.app.activities.tutorial;

import com.boxxit.boxxit.library.parse.models.facebook.Profile;

public enum TutorialUIState {
    INITIAL,
    STEP_1,
    STEP_2,
    STEP_3,
    DONE,
    ERROR;

    public Profile profile;
    public Throwable error;

    public TutorialUIState withProfile (Profile profile) {
        this.profile = profile;
        return this;
    }

    public static TutorialUIState ERROR (Throwable throwable) {
        TutorialUIState result = ERROR;
        result.error = throwable;
        return result;
    }
}
