package com.boxxit.boxxit.app.activities.tutorial;

import com.boxxit.boxxit.library.parse.models.facebook.Profile;

public enum TutorialUIState {
    INITIAL,
    TUTORIAL_1,
    TUTORIAL_2,
    TUTORIAL_3,
    TUTORIAL_4,
    TUTORIAL_5,
    DISMISSED,
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
