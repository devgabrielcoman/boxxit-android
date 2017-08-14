package com.boxxit.boxxit.app.activities.intro;

public class IntroUIState {
    boolean isInitial;
    boolean isLoggedIn;


    public IntroUIState(boolean isInitial, boolean isLoggedIn) {
        this.isInitial = isInitial;
        this.isLoggedIn = isLoggedIn;
    }

    public static IntroUIState loggedIn () {
        return new IntroUIState(false, true);
    }

    public static IntroUIState notLoggedIn () {
        return new IntroUIState(false, false);
    }

    public static IntroUIState initial () {
        return new IntroUIState(true, false);
    }
}
