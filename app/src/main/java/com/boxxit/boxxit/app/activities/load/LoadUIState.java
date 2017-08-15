package com.boxxit.boxxit.app.activities.load;

public class LoadUIState {
    boolean isInitial;
    boolean profileSuccess;
    Throwable error;

    public LoadUIState(boolean isInitial, boolean profileSuccess, Throwable error) {
        this.isInitial = isInitial;
        this.profileSuccess = profileSuccess;
        this.error = error;
    }

    public static LoadUIState initial () {
        return new LoadUIState(true, false, null);
    }

    public static LoadUIState profileSuccess () {
        return new LoadUIState(false, true, null);
    }

    public static LoadUIState error (Throwable throwable) {
        return new LoadUIState(false, false, throwable);
    }
}
