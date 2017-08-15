package com.boxxit.boxxit.app.activities.login;

public class LoginUIState {
    boolean isInitial;
    boolean isLoading;
    boolean authSuccess;
    boolean authCancel;
    Throwable error;

    public LoginUIState(boolean isInitial, boolean isLoading, boolean authSuccess, boolean authCancel, Throwable error) {
        this.isInitial = isInitial;
        this.isLoading = isLoading;
        this.authSuccess = authSuccess;
        this.authCancel = authCancel;
        this.error = error;
    }

    public static LoginUIState initial () {
        return new LoginUIState(true, false, false, false, null);
    }

    public static LoginUIState isLoading () {
        return new LoginUIState(false, true, false, false, null);
    }

    public static LoginUIState authSuccess () {
        return new LoginUIState(false, false, true, false, null);
    }

    public static LoginUIState authCancel () {
        return new LoginUIState(false, false, false, true, null);
    }

    public static LoginUIState error (Throwable throwable) {
        return new LoginUIState(false, false, false, false, throwable);
    }
}
