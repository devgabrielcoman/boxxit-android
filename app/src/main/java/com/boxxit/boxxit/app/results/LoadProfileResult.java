package com.boxxit.boxxit.app.results;

import com.boxxit.boxxit.library.parse.models.facebook.Profile;

public enum LoadProfileResult implements Result {
    SUCCESS,
    ERROR;

    public Throwable throwable;
    public Profile profile;

    public static LoadProfileResult error(Throwable throwable) {
        LoadProfileResult result = LoadProfileResult.ERROR;
        result.throwable = throwable;
        return result;
    }

    public static LoadProfileResult success(Profile profile) {
        LoadProfileResult result = LoadProfileResult.SUCCESS;
        result.profile = profile;
        return result;
    }
}
