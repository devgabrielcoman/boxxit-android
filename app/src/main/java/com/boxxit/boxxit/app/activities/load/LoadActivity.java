package com.boxxit.boxxit.app.activities.load;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.boxxit.boxxit.R;
import com.boxxit.boxxit.app.activities.BaseActivity;
import com.boxxit.boxxit.app.activities.main.MainActivity;
import com.boxxit.boxxit.app.events.InitEvent;
import com.boxxit.boxxit.app.events.RetryClickEvent;
import com.boxxit.boxxit.app.events.UIEvent;
import com.boxxit.boxxit.app.results.LoadProfileResult;
import com.boxxit.boxxit.app.views.CustomAlert;
import com.boxxit.boxxit.datastore.DataStore;
import com.boxxit.boxxit.workers.UserWorker;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public class LoadActivity extends BaseActivity {

    PublishSubject<RetryClickEvent> retries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);

        //
        // initial state
        LoadUIState initialState = LoadUIState.initial();

        //
        // events
        retries = PublishSubject.create();
        Observable<InitEvent> inits = Observable.just(new InitEvent());
        Observable<UIEvent> events = Observable.merge(retries.asObservable(), inits);

        //
        // transformer
        Observable.Transformer<UIEvent, LoadProfileResult> transformer = initEventObservable -> events
                .flatMap(initEvent -> UserWorker.getProfile(DataStore.getOwnId()).toObservable()
                        .map(LoadProfileResult::success)
                        .onErrorReturn(LoadProfileResult::error))
                .observeOn(AndroidSchedulers.mainThread());


        //
        // state updates observer
        Observable<LoadUIState> state = events.compose(transformer).scan(initialState, this::stateReducer);

        // UI updates
        state.subscribe(this::stateHandler, throwable -> Log.e("Boxxit", "Error is " + throwable.getMessage()));
    }

    private LoadUIState stateReducer (LoadUIState previousState, LoadProfileResult result) {
        if (result == LoadProfileResult.SUCCESS) {
            return LoadUIState.profileSuccess();
        } else if (result == LoadProfileResult.ERROR) {
            return LoadUIState.error(result.throwable);
        } else {
            return previousState;
        }
    }

    private void stateHandler (LoadUIState state) {
        if (state.profileSuccess) {
            gotoMainScreen();
        } else if (state.error != null) {
            updateErrorUI(state.error);
        } else {
            // do nothing
        }
    }

    private void gotoMainScreen () {
        Intent intent = new Intent(LoadActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void updateErrorUI (Throwable throwable) {
        CustomAlert.shared()
                .show(this,
                        getString(R.string.alert_network_error_title),
                        getString(R.string.alert_network_error_message),
                        getString(R.string.alert_try_again),
                        null)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(integer -> Log.e("Boxxit", "Load Activity: " + throwable.getMessage()))
                .subscribe(integer -> retries.onNext(new RetryClickEvent(null)));
    }
}
