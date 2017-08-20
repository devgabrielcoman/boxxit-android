package com.boxxit.boxxit.app.activities.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.boxxit.boxxit.R;
import com.boxxit.boxxit.app.activities.BaseActivity;
import com.boxxit.boxxit.app.activities.main.MainActivity;
import com.boxxit.boxxit.app.events.ClickEvent;
import com.boxxit.boxxit.app.events.UIEvent;
import com.boxxit.boxxit.app.results.LoginResult;
import com.boxxit.boxxit.app.views.CustomAlert;
import com.boxxit.boxxit.datastore.DataStore;
import com.boxxit.boxxit.library.auth.FacebookAuthRequest;
import com.boxxit.boxxit.library.auth.FacebookAuthTask;
import com.boxxit.boxxit.workers.UserWorker;
import com.facebook.FacebookException;
import com.jakewharton.rxbinding.view.RxView;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;

public class LoginActivity extends BaseActivity {

    @BindView(R.id.LoginButton) Button loginButton;
    @BindView(R.id.Spinner) ProgressBar spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        FacebookAuthRequest request = new FacebookAuthRequest();
        FacebookAuthTask task = new FacebookAuthTask(this);
        setOnActivityResult(task::callbackResult);

        //
        // initial state
        LoginUIState initialState = LoginUIState.INITIAL;

        //
        // UI & other events
        Observable<ClickEvent> events = RxView.clicks(loginButton).map(ClickEvent::new);

        //
        // transformer
        Observable.Transformer<ClickEvent, LoginResult> transformer = eventObservable -> events
                .flatMap(uiEvent -> task.execute(request).toObservable()
                        .flatMap(token -> UserWorker.populateUserProfile(token).toObservable())
                        .flatMap(aVoid -> UserWorker.getProfile(DataStore.getOwnId()).toObservable())
                        .map(r -> LoginResult.LOGGED_IN)
                        .onErrorReturn(LoginResult::error)
                        .startWith(LoginResult.LOADING))
                .observeOn(AndroidSchedulers.mainThread());

        //
        // state updates observer
        Observable<LoginUIState> state = events.compose(transformer).scan(initialState, this::stateReducer);

        // UI updates
        state.subscribe(this::stateHandler, throwable -> Log.e("Boxxit", "Error is " + throwable.getMessage()));
    }

    private LoginUIState stateReducer (LoginUIState previousState, LoginResult result) {
        switch (result) {
            case LOGGED_IN:
                return LoginUIState.AUTH_SUCCESS;
            case LOADING:
                return LoginUIState.LOADING;
            case ERROR:
                return result.throwable instanceof FacebookException ?
                        LoginUIState.ERROR(result.throwable) :
                        LoginUIState.AUTH_CANCEL;
            case NOT_LOGGED_IN:
            default:
                return previousState;

        }
    }

    private void stateHandler (LoginUIState state) {
        switch (state) {
            case INITIAL:
                break;
            case LOADING:
                updateLoadingUI();
                break;
            case AUTH_CANCEL:
                updateInitialUI();
                break;
            case AUTH_SUCCESS:
                gotoNextScreen();
                break;
            case ERROR:
                updateErrorUI(state.throwable);
                break;
        }
    }

    private void updateLoadingUI () {
        spinner.setVisibility(View.VISIBLE);
        loginButton.setVisibility(View.GONE);
    }

    private void updateInitialUI () {
        spinner.setVisibility(View.GONE);
        loginButton.setVisibility(View.VISIBLE);
    }

    void gotoNextScreen () {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("hasTutorial", true);
        startActivity(intent);
    }

    private void updateErrorUI (Throwable throwable) {
        spinner.setVisibility(View.GONE);
        loginButton.setVisibility(View.VISIBLE);
        CustomAlert.shared()
                .show(this,
                        getString(R.string.alert_auth_error_title),
                        getString(R.string.alert_auth_error_message),
                        getString(R.string.alert_ok),
                        null)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> Log.e("Boxxit", "Login Activity: " + throwable.getMessage()));
    }
}
