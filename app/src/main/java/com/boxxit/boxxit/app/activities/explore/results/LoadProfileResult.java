package com.boxxit.boxxit.app.activities.explore.results;

public enum LoadProfileResult implements Result {
    LOADING,
    SUCCESS,
    ERROR;

    public Throwable throwable;

    public static LoadProfileResult error(Throwable throwable) {
        LoadProfileResult result = LoadProfileResult.ERROR;
        result.throwable = throwable;
        return result;
    }
}
