package com.boxxit.boxxit.app.activities.tutorial;

import com.boxxit.boxxit.library.parse.models.facebook.Profile;

public class TutorialUIState {
    boolean isInitial;
    boolean isTutorial1;
    boolean isTutorial2;
    boolean isTutorial3;
    boolean isTutorial4;
    boolean isTutorial5;
    boolean isDismissed;

    Profile profile;
    Throwable error;

    public TutorialUIState(boolean isInitial,
                           boolean isTutorial1,
                           boolean isTutorial2,
                           boolean isTutorial3,
                           boolean isTutorial4,
                           boolean isTutorial5,
                           boolean isDismissed,
                           Profile profile,
                           Throwable error) {
        this.isInitial = isInitial;
        this.isTutorial1 = isTutorial1;
        this.isTutorial2 = isTutorial2;
        this.isTutorial3 = isTutorial3;
        this.isTutorial4 = isTutorial4;
        this.isTutorial5 = isTutorial5;
        this.isDismissed = isDismissed;
        this.profile = profile;
        this.error = error;
    }

    public static TutorialUIState initial () {
        return new TutorialUIState(true, false, false, false, false, false, false, null, null);
    }

    public static TutorialUIState tutorial1 (Profile profile) {
        return new TutorialUIState(false, true, false, false, false, false, false, profile, null);
    }

    public static TutorialUIState tutorial2 (Profile profile) {
        return new TutorialUIState(false, false, true, false, false, false, false, profile, null);
    }

    public static TutorialUIState tutorial3 (Profile profile) {
        return new TutorialUIState(false, false, false, true, false, false, false, profile, null);
    }

    public static TutorialUIState tutorial4 () {
        return new TutorialUIState(false, false, false, false, true, false, false, null, null);
    }

    public static TutorialUIState tutorial5 () {
        return new TutorialUIState(false, false, false, false, false, true, false, null, null);
    }

    public static TutorialUIState error (Throwable throwable) {
        return new TutorialUIState(false, false, false, false, false, false, false, null, throwable);
    }

    public static TutorialUIState dismiss () {
        return new TutorialUIState(false, false, false, false, false, false, true, null, null);
    }
}
