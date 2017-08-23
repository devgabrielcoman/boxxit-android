package com.boxxit.boxxit.workers;

import com.boxxit.boxxit.library.firebase.FirebaseTokenRequest;
import com.boxxit.boxxit.library.firebase.FirebaseTokenTask;

import rx.Single;

public class FirebaseWorker {

    public static Single<String> getNotificationToken () {
        FirebaseTokenRequest request = new FirebaseTokenRequest();
        FirebaseTokenTask task = new FirebaseTokenTask();
        return task.execute(request);
    }

}
