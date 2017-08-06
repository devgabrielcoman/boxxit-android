package com.boxxit.boxxit.app.activities.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.boxxit.boxxit.R;
import com.boxxit.boxxit.app.activities.BaseActivity;
import com.boxxit.boxxit.app.activities.main.MainActivity;
import com.boxxit.boxxit.library.auth.FacebookAuthRequest;
import com.boxxit.boxxit.library.auth.FacebookAuthTask;
import com.boxxit.boxxit.workers.UserWorker;

public class LoginActivity extends BaseActivity {

    FacebookAuthRequest request;
    FacebookAuthTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.d("Boxxit", "Login activity");

        request = new FacebookAuthRequest();
        task = new FacebookAuthTask(this);
        setOnActivityResult(task::callbackResult);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Business Logic
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void loginAction (View view) {

        task.execute(request)
                .doOnSubscribe(() -> {
                    // set state to loading
                })
                .flatMap(UserWorker::populateUserProfile)
                .flatMap(aVoid -> UserWorker.getProfile("me"))
                .subscribe(profile -> {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                }, throwable -> {
                    if (throwable == null) {
                        Log.d("Boxxit", "Login Cancelled");
                    } else {
                        Log.d("Boxxit", "Error is " + throwable.getMessage());
                    }
                });

    }
}
