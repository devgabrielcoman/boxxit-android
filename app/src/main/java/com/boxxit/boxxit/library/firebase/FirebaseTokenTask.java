package com.boxxit.boxxit.library.firebase;

import android.os.Handler;
import android.util.Log;

import com.boxxit.boxxit.library.base.Task;
import com.google.firebase.iid.FirebaseInstanceId;

import rx.Single;

public class FirebaseTokenTask implements Task<FirebaseTokenRequest, String, Single<String>> {

    private final int DELAY = 1000;
    private Handler handler = new Handler();
    private Runnable runnable = null;

    @Override
    public Single<String> execute(FirebaseTokenRequest request) {
        return Single.create(singleSubscriber -> {

            runnable = () -> {

                String token = FirebaseInstanceId.getInstance().getToken();

                if (token != null) {
                    Log.d("Boxxit", "Managed to get Firebase token " + token);
                    singleSubscriber.onSuccess(token);
                } else {
                    Log.d("Boxxit", "Trying to get Firebase token && Facebook profile ...");
                    handler.postDelayed(runnable, DELAY);
                }
            };
            handler.postDelayed(runnable, DELAY);

        });
    }
}
