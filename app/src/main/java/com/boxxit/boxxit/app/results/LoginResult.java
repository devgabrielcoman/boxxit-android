package com.boxxit.boxxit.app.results;

public enum  LoginResult implements Result {
    LOGGED_IN,
    NOT_LOGGED_IN;

    String token;

    static LoginResult loggedIn (String token) {
        LoginResult result = LOGGED_IN;
        result.token = token;
        return result;
    }
}
