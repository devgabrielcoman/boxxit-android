package com.boxxit.boxxit.app.results;

import com.boxxit.boxxit.library.parse.models.facebook.Profile;

public enum TutorialResult implements Result {
    PRESENT,
    ADVANCE,
    ERROR;

    public Throwable error;
    public Profile profile;

    public static TutorialResult advance (Profile profile) {
        TutorialResult result = ADVANCE;
        result.profile = profile;
        return result;
    }

    public static TutorialResult error (Throwable throwable) {
        TutorialResult result = ERROR;
        result.error = throwable;
        return result;
    }

}
