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
import com.boxxit.boxxit.app.results.EmptyResult;
import com.boxxit.boxxit.app.results.LoadProfileResult;
import com.boxxit.boxxit.app.results.Result;
import com.boxxit.boxxit.app.views.CustomAlert;
import com.boxxit.boxxit.datastore.DataStore;
import com.boxxit.boxxit.workers.FirebaseWorker;
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
        LoadUIState initialState = LoadUIState.INITIAL;

        //
        // UI & other events
        retries = PublishSubject.create();
        Observable<InitEvent> inits = Observable.just(new InitEvent());
        Observable<UIEvent> events = Observable.merge(retries.asObservable(), inits);

        //
        // profile transformer
        Observable.Transformer<UIEvent, LoadProfileResult> profileTransformer = initEventObservable -> events
                .flatMap(initEvent -> UserWorker.getProfile(DataStore.getOwnId()).toObservable()
                        .map(LoadProfileResult::success)
                        .onErrorReturn(LoadProfileResult::error))
                .observeOn(AndroidSchedulers.mainThread());

        //
        // token transformer
        Observable.Transformer<UIEvent, EmptyResult> tokenTransformer = eventObservable -> inits
                .flatMap(uiEvent -> FirebaseWorker.getNotificationToken().toObservable())
                .flatMap(newToken -> UserWorker.updateNotificationToken(newToken, DataStore.getOwnId()).toObservable())
                .map(r -> EmptyResult.EMPTY_RESULT);

        //
        // final transformer
        Observable.Transformer<UIEvent, Result> transformer = observable -> Observable.merge(
                observable.ofType(UIEvent.class).compose(profileTransformer),
                observable.ofType(UIEvent.class).compose(tokenTransformer)
        );

        //
        // state updates observer
        Observable<LoadUIState> state = events.compose(transformer).scan(initialState, this::stateReducer);

        // UI updates
        state.subscribe(this::stateHandler, throwable -> Log.e("Boxxit", "Error is " + throwable.getMessage()));
    }

    private LoadUIState stateReducer (LoadUIState previousState, Result result) {
        if (result instanceof LoadProfileResult) {
            LoadProfileResult loadProfileResult = (LoadProfileResult) result;
            switch (loadProfileResult) {
                case SUCCESS:
                    return LoadUIState.PROFILE_SUCCESS;
                case ERROR:
                    return LoadUIState.ERROR(loadProfileResult.throwable);
                default:
                    return previousState;
            }
        } else if (result instanceof EmptyResult) {
            return LoadUIState.SEND_NOTIF_TOKEN;
        } else {
            return previousState;
        }
    }

    private void stateHandler (LoadUIState state) {
        switch (state) {
            case INITIAL:
                break;
            case PROFILE_SUCCESS:
                gotoMainScreen();
                break;
            case ERROR:
                updateErrorUI(state.throwable);
                break;
            case SEND_NOTIF_TOKEN:
                // do nothing it's background process
                break;
        }
    }

    private void gotoMainScreen () {
        Intent intent = new Intent(LoadActivity.this, MainActivity.class);
        boolean hasTutorial = getBooleanExtrasDirect("hasTutorial");
        intent.putExtra("hasTutorial", hasTutorial);
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
