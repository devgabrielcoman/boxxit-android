package com.boxxit.boxxit.app.results;

import com.boxxit.boxxit.library.parse.models.facebook.Profile;

public enum TutorialResult implements Result {
    PRESENT1,
    PRESENT2,
    PRESENT3,
    DISMISS,
    GOTO_NEXT_TUTORIAL,
    ERROR;

    public Throwable error;
    public Profile profile;

    public static TutorialResult gotoNext (Profile profile) {
        TutorialResult result = GOTO_NEXT_TUTORIAL;
        result.profile = profile;
        return result;
    }

    public static TutorialResult error (Throwable throwable) {
        TutorialResult result = ERROR;
        result.error = throwable;
        return result;
    }

}
