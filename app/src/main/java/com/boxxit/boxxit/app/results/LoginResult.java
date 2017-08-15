package com.boxxit.boxxit.app.results;

public enum  LoginResult implements Result {
    LOGGED_IN,
    NOT_LOGGED_IN,
    LOADING,
    ERROR;

    public Throwable throwable;
    public String token;

    public static LoginResult loggedIn (String token) {
        LoginResult result = LOGGED_IN;
        result.token = token;
        return result;
    }

    public static LoginResult error (Throwable throwable) {
        LoginResult result = ERROR;
        result.throwable = throwable;
        return result;
    }
}
