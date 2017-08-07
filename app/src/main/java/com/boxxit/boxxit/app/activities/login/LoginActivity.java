package com.boxxit.boxxit.app.activities.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.boxxit.boxxit.R;
import com.boxxit.boxxit.app.activities.BaseActivity;
import com.boxxit.boxxit.app.activities.load.LoadActivity;
import com.boxxit.boxxit.app.activities.main.MainActivity;
import com.boxxit.boxxit.datastore.DataStore;
import com.boxxit.boxxit.library.auth.FacebookAuthRequest;
import com.boxxit.boxxit.library.auth.FacebookAuthTask;
import com.boxxit.boxxit.workers.UserWorker;

public class LoginActivity extends BaseActivity {

    FacebookAuthRequest request;
    FacebookAuthTask task;

    Button loginButton;
    ProgressBar spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        request = new FacebookAuthRequest();
        task = new FacebookAuthTask(this);
        setOnActivityResult(task::callbackResult);

        setStateInitial();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Business Logic
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void loginAction (View view) {

        task.execute(request)
                .doOnSubscribe(this::setStateLoading)
                .flatMap(UserWorker::populateUserProfile)
                .flatMap(aVoid -> UserWorker.getProfile(DataStore.getOwnId()))
                .subscribe(this::gotoNextScreen, this::handleErrors);

    }

    void handleErrors (Throwable throwable) {
        if (throwable == null) {
            setStateNotLoading();
        } else {
            setStateError(throwable);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Routing Logic
    ////////////////////////////////////////////////////////////////////////////////////////////////

    void gotoNextScreen (Object o) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // State Logic
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void setStateInitial () {
        loginButton = (Button) findViewById(R.id.LoginButton);
        spinner = (ProgressBar) findViewById(R.id.Spinner);
    }

    private void setStateLoading () {
        spinner.setVisibility(View.VISIBLE);
        loginButton.setVisibility(View.GONE);
    }

    private void setStateNotLoading () {
        spinner.setVisibility(View.GONE);
        loginButton.setVisibility(View.VISIBLE);
    }

    // TODO: 07/08/2017 Add error popup
    private void setStateError (Throwable throwable) {
        spinner.setVisibility(View.GONE);
        loginButton.setVisibility(View.VISIBLE);
        Log.d("Boxxit", "Error is " + throwable.getMessage());
    }
}
