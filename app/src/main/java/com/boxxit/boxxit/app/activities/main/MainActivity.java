package com.boxxit.boxxit.app.activities.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.boxxit.boxxit.R;
import com.boxxit.boxxit.app.activities.BaseActivity;
import com.boxxit.boxxit.app.activities.explore.ExploreActivity;
import com.boxxit.boxxit.app.events.AppendEvent;
import com.boxxit.boxxit.app.events.ClickEvent;
import com.boxxit.boxxit.app.events.InitEvent;
import com.boxxit.boxxit.app.events.RetryClickEvent;
import com.boxxit.boxxit.app.events.UIEvent;
import com.boxxit.boxxit.app.results.LoadEventsResult;
import com.boxxit.boxxit.app.results.LoadProfileResult;
import com.boxxit.boxxit.app.results.NavigateResult;
import com.boxxit.boxxit.app.results.Result;
import com.boxxit.boxxit.app.views.ErrorView;
import com.boxxit.boxxit.datastore.DataStore;
import com.boxxit.boxxit.library.parse.models.facebook.Profile;
import com.boxxit.boxxit.workers.UserWorker;
import com.gabrielcoman.rxrecyclerview.RxAdapter;
import com.jakewharton.rxbinding.view.RxView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public class MainActivity extends BaseActivity {

    //
    // views
    @BindView(R.id.ErrorView) ErrorView errorView;
    @BindView(R.id.InviteView) ErrorView inviteView;
    @BindView(R.id.EventsRecyclerView) RecyclerView recyclerView;
    @BindView(R.id.Spinner) ProgressBar spinner;

    @BindView(R.id.ProfilePicture) ImageView profilePicture;
    @BindView(R.id.ProfileName) TextView profileName;
    @BindView(R.id.ProfileBirthday) TextView profileBirthday;
    @BindView(R.id.MainButton) Button mainButton;

    private RxAdapter adapter;

    private PublishSubject<AppendEvent> append;
    String[] offset = {null};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //
        // other state vars
        String userId = DataStore.getOwnId();

        //
        // initial state
        MainUIState initialState = MainUIState.initial();

        //
        // observables
        append = PublishSubject.create();
        Observable<InitEvent> init = Observable.just(new InitEvent());
        Observable<ClickEvent> explore = RxView.clicks(mainButton).map(ClickEvent::new);
        Observable<RetryClickEvent> retries = RxView.clicks(errorView.retry).map(RetryClickEvent::new);
        Observable<ClickEvent> invites = RxView.clicks(inviteView.retry).map(ClickEvent::new);
        Observable<UIEvent> events = Observable.merge(init, retries, invites, append.asObservable());

        //
        // profile transformer
        Observable.Transformer<InitEvent, LoadProfileResult> profileTransformer = initEventObservable -> init
                .flatMap(initEvent -> UserWorker.getProfile(userId).toObservable())
                .map(LoadProfileResult::success)
                .onErrorReturn(LoadProfileResult::error)
                .observeOn(AndroidSchedulers.mainThread());

        //
        // events transformer
        Observable.Transformer<UIEvent, LoadEventsResult> eventsTransformer = eventObservable -> events
                .flatMap(uiEvent -> UserWorker.getEventsForUser(userId, offset[0]).toObservable()
                        .doOnNext(facebookData -> offset[0] = facebookData.paging.offsetAfter())
                        .map(facebookData -> facebookData.data)
                        .map(LoadEventsResult::success)
                        .onErrorReturn(LoadEventsResult::error))
                        .startWith(LoadEventsResult.LOADING)
                .observeOn(AndroidSchedulers.mainThread());

        //
        // explore main transformer
        Observable.Transformer<ClickEvent, NavigateResult> exploreTransformer = clickEventObservable -> explore
                .map(clickEvent -> NavigateResult.NEXT);

        //
        // main transformer
        Observable.Transformer<UIEvent, Result> transformer = observable -> Observable.merge(
                observable.ofType(UIEvent.class).compose(eventsTransformer),
                observable.ofType(InitEvent.class).compose(profileTransformer),
                observable.ofType(ClickEvent.class).compose(exploreTransformer)
        );

        //
        // state updates observer
        Observable<MainUIState> state = events.compose(transformer).scan(initialState, this::stateReducer);

        //
        // UI updates
        state.subscribe(this::stateHandler, throwable -> Log.e("Boxxit", "Error is " + throwable.getMessage()));
    }

    private MainUIState stateReducer (MainUIState previousState, Result result) {
        if (result instanceof LoadProfileResult) {
            if (result == LoadProfileResult.SUCCESS) {
                return MainUIState.profileSuccess(((LoadProfileResult) result).profile);
            } else if (result == LoadProfileResult.ERROR) {
                return MainUIState.error(((LoadProfileResult) result).throwable);
            } else {
                return previousState;
            }
        } else if (result instanceof LoadEventsResult) {
            if (result == LoadEventsResult.SUCCESS) {
                if (((LoadEventsResult) result).events.size() > 0) {
                    //
                    // accumulate state
                    List<Profile> events = new ArrayList<>();
                    events.addAll(previousState.events != null ? previousState.events : new ArrayList<Profile>());
                    events.addAll(((LoadEventsResult) result).events);
                    //
                    // return new success state
                    return  MainUIState.eventsSuccess(events);
                } else {
                    return MainUIState.eventsEmpty();
                }
            } else if (result == LoadEventsResult.ERROR) {
                return MainUIState.error(((LoadEventsResult) result).error);
            } else if (result == LoadEventsResult.LOADING) {
                return MainUIState.isLoading();
            } else {
                return previousState;
            }
        } else if (result instanceof NavigateResult) {
            return MainUIState.gotoExplore();
        } else {
            return previousState;
        }
    }

    private void stateHandler (MainUIState state) {
        if (state.profileSuccess) {
            populateOwnProfileUI(state.profile);
        }
        else if (state.isLoading) {
            populateLoadingUI();
        }
        else if (state.eventsSuccess) {
            populateSuccessUI(state.events);
        }
        else if (state.eventsEmpty) {
            populateEmptyUI();
        }
        else if (state.error != null) {
            populateErrorUI(state.error);
        }
        else if (state.gotoExplore) {
            gotoNextScreen(DataStore.getOwnId());
        }
        else {
            populateInitialUI();
        }
    }

    private void populateOwnProfileUI (Profile profile) {
        profileName.setText(profile.name);
        profileBirthday.setText(profile.birthday);

        Picasso.with(MainActivity.this)
                .load(profile.picture.data.url)
                .placeholder(R.drawable.ic_user_default)
                .error(R.drawable.ic_user_default)
                .transform(new CropCircleTransformation())
                .into(profilePicture);
    }

    private void populateLoadingUI () {
        spinner.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        inviteView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
    }

    private void populateSuccessUI (List<Profile> events) {
        errorView.setVisibility(View.GONE);
        inviteView.setVisibility(View.GONE);
        spinner.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        adapter.update(events);
    }

    private void populateErrorUI (Throwable throwable) {
        errorView.setVisibility(View.VISIBLE);
        inviteView.setVisibility(View.GONE);
        spinner.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        errorView.errorText.setText(getString(R.string.activity_main_error));
        errorView.retry.setText(getString(R.string.error_button_try_again));
    }

    private void populateEmptyUI () {
        spinner.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        inviteView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        inviteView.errorText.setText(getString(R.string.activity_main_invite_nofriends_message));
        inviteView.retry.setText(getString(R.string.activity_main_invite_btn_title));
    }

    private void populateInitialUI () {
        adapter = RxAdapter.create()
                .bindTo(recyclerView)
                .setLayoutManger(new GridLayoutManager(getApplicationContext(), 2))
                .customizeRow(R.layout.row_event, Profile.class, (position, view, profile, total) -> {

                    ImageView profilePicture = (ImageView) view.findViewById(R.id.ProfilePicture);
                    TextView profileName = (TextView) view.findViewById(R.id.ProfileName);
                    TextView profileBirthday = (TextView) view.findViewById(R.id.ProfileBirthday);
                    View rightSeparator = view.findViewById(R.id.RightSeparator);

                    rightSeparator.setVisibility(position % 2 == 0 ? View.VISIBLE : View.GONE);
                    profileName.setText(profile.name);
                    profileBirthday.setText(profile.birthday);

                    Picasso.with(MainActivity.this)
                            .load(profile.picture.data.url)
                            .placeholder(R.drawable.ic_user_default)
                            .error(R.drawable.ic_user_default)
                            .transform(new CropCircleTransformation())
                            .into(profilePicture);
                })
                .didClickOnRow(Profile.class, (integer, profile) -> gotoNextScreen(profile.id))
                .didReachEnd(() -> {
                    if (offset[0] != null) {
                        append.onNext(null);
                    }
                });
    }

    void gotoNextScreen (String profile) {
        Intent intent = new Intent(this, ExploreActivity.class);
        intent.putExtra("profile", profile);
        startActivity(intent);
    }
}
