package com.boxxit.boxxit.app.activities.load;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.boxxit.boxxit.R;
import com.boxxit.boxxit.app.activities.BaseActivity;
import com.boxxit.boxxit.app.activities.main.MainActivity;
import com.boxxit.boxxit.datastore.DataStore;
import com.boxxit.boxxit.workers.UserWorker;

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

    // TODO: 07/08/2017 Add error popup
    void setStateError (Throwable throwable) {
        Log.d("Boxxit", "Error is: " + throwable.getMessage());
    }
}
