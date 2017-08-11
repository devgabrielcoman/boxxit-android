package com.boxxit.boxxit.app.activities.explore.results;

public enum LoadProductsResult implements Result {
    LOADING,
    SUCCESS,
    ERROR;

    public Throwable throwable;

    public static LoadProductsResult error(Throwable throwable) {
        LoadProductsResult result = LoadProductsResult.ERROR;
        result.throwable = throwable;
        return result;
    }
}
