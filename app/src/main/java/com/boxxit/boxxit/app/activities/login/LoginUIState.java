package com.boxxit.boxxit.app.activities.login;

public enum LoginUIState {
    INITIAL,
    LOADING,
    AUTH_CANCEL,
    AUTH_SUCCESS,
    ERROR;

    Throwable throwable;

    public static LoginUIState ERROR (Throwable throwable) {
        LoginUIState result = ERROR;
        result.throwable = throwable;
        return result;
    }
}
