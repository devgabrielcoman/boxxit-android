package com.boxxit.boxxit.app.activities.intro;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.boxxit.boxxit.R;
import com.boxxit.boxxit.app.activities.BaseActivity;
import com.boxxit.boxxit.app.activities.favourites.FavouritesUIState;
import com.boxxit.boxxit.app.activities.load.LoadActivity;
import com.boxxit.boxxit.app.activities.login.LoginActivity;
import com.boxxit.boxxit.app.events.InitEvent;
import com.boxxit.boxxit.app.results.LoginResult;
import com.boxxit.boxxit.app.results.Result;
import com.boxxit.boxxit.workers.UserWorker;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;

public class IntroActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        //
        // intro state
        IntroUIState initialState = IntroUIState.initial();

        //
        // initial observers
        Observable<InitEvent> events = Observable.just(new InitEvent());

        //
        // transformers
        Observable.Transformer<InitEvent, LoginResult> transformer = initEventObservable -> events
                .flatMap(initEvent -> UserWorker.checkUserLoggedIn().toObservable())
                .map(aBoolean -> LoginResult.LOGGED_IN)
                .onErrorReturn(throwable -> LoginResult.NOT_LOGGED_IN)
                .observeOn(AndroidSchedulers.mainThread());

        //
        // state manager
        //
        // state updates observer
        Observable<IntroUIState> state = events.compose(transformer).scan(initialState, this::stateReducer);

//
        // UI updates
        state.subscribe(this::stateHandler, throwable -> Log.e("Boxxit", "Error is " + throwable.getMessage()));
    }

    private IntroUIState stateReducer (IntroUIState previousState, LoginResult result) {
        if (result == LoginResult.LOGGED_IN) {
            return IntroUIState.loggedIn();
        } else {
            return IntroUIState.notLoggedIn();
        }
    }

    public void stateHandler(IntroUIState state) {
        if (!state.isInitial && state.isLoggedIn) {
            gotoLoadActivity();
        } else if (!state.isInitial && !state.isLoggedIn) {
            gotoLoginActivity();
        } else {
            // do nothing
        }
    }

    private void gotoLoadActivity () {
        Intent intent = new Intent(this, LoadActivity.class);
        startActivity(intent);
    }

    private void gotoLoginActivity () {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
