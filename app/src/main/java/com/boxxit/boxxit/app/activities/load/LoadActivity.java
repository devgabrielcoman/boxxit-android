package com.boxxit.boxxit.app.activities.load;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.boxxit.boxxit.R;
import com.boxxit.boxxit.app.activities.BaseActivity;
import com.boxxit.boxxit.app.activities.main.MainActivity;
import com.boxxit.boxxit.app.views.CustomAlert;
import com.boxxit.boxxit.datastore.DataStore;
import com.boxxit.boxxit.workers.UserWorker;

import rx.android.schedulers.AndroidSchedulers;

public class LoadActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);
        loadUser();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Business Logic
    ////////////////////////////////////////////////////////////////////////////////////////////////

    void loadUser () {
        UserWorker.getProfile(DataStore.getOwnId())
                .subscribe(this::gotoNextScreen, this::setStateError);

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Routing Logic
    ////////////////////////////////////////////////////////////////////////////////////////////////

    void gotoNextScreen (Object o) {
        Intent intent = new Intent(LoadActivity.this, MainActivity.class);
        startActivity(intent);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // State Logic
    ////////////////////////////////////////////////////////////////////////////////////////////////

    void setStateError (Throwable throwable) {
        CustomAlert.shared()
                .show(this,
                    getString(R.string.alert_network_error_title),
                    getString(R.string.alert_network_error_message),
                    getString(R.string.alert_try_again),
                    null)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(integer -> Log.e("Boxxit", "Load Activity: " + throwable.getMessage()))
                .subscribe(integer -> loadUser());
    }
}
