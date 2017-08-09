package com.boxxit.boxxit.app.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;

import rx.Single;

public class CustomAlert {

    public static final int OK_BUTTON = 0;
    public static final int CANCEL_BUTTON = 1;

    private static final CustomAlert instance = new CustomAlert();

    public static CustomAlert shared () {
        return instance;
    }

    private AlertDialog dialog;

    public Single<Integer> show(Context c, String title, String message, String okTitle, String nokTitle) {

        AlertDialog.Builder alert = new AlertDialog.Builder(c);
        alert.setCancelable(false);
        alert.setTitle(title);
        alert.setMessage(message);

        return Single.create(subscriber -> {

            alert.setPositiveButton(okTitle, (dialog1, which) -> subscriber.onSuccess(OK_BUTTON));

            if (nokTitle != null) {
                alert.setNegativeButton(nokTitle, (dialog12, which) -> subscriber.onSuccess(CANCEL_BUTTON));
            }

            dialog = alert.create();
            dialog.show();
        });
    }
}
