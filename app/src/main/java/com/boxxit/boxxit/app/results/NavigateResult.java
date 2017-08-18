package com.boxxit.boxxit.app.results;

public enum NavigateResult implements Result {
    BACK,
    NEXT;

    public int backResult;

    public static NavigateResult back (int backResult) {
        NavigateResult result = BACK;
        result.backResult = backResult;
        return result;
    }
}
