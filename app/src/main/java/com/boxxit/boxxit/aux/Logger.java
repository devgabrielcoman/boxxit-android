package com.boxxit.boxxit.aux;

import android.util.Log;

public class Logger {

    private static final String TAG = "Boxxit";

    public static void logError (Throwable throwable) {
        Log.e(TAG, "Error: " + throwable.getMessage());
    }

}
