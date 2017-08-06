package com.boxxit.boxxit.app.activities.intro;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.boxxit.boxxit.R;
import com.boxxit.boxxit.app.activities.BaseActivity;
import com.boxxit.boxxit.app.activities.load.LoadActivity;
import com.boxxit.boxxit.app.activities.login.LoginActivity;
import com.boxxit.boxxit.workers.UserWorker;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

public class IntroActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        Log.d("Boxxit", "Intro activity");

        //
        // initialize Facebook SDK
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        //
        // apply business logic
        checkLogin();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Business Logic
    ////////////////////////////////////////////////////////////////////////////////////////////////

    void checkLogin () {
        Intent intent = new Intent(this, UserWorker.isUserLoggedIn() ? LoadActivity.class : LoginActivity.class);
        startActivity(intent);
    }
}
