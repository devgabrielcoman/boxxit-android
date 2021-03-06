package com.boxxit.boxxit.app.activities.load;

public enum LoadUIState {
    INITIAL,
    PROFILE_SUCCESS,
    ERROR,
    SEND_NOTIF_TOKEN;

    Throwable throwable;

    public static LoadUIState ERROR(Throwable throwable) {
        LoadUIState result = ERROR;
        result.throwable = throwable;
        return result;
    }
}
