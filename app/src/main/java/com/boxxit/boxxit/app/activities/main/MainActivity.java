package com.boxxit.boxxit.app.activities.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
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
import com.boxxit.boxxit.app.activities.tutorial.TutorialActivity;
import com.boxxit.boxxit.app.events.AppendEvent;
import com.boxxit.boxxit.app.events.ClickEvent;
import com.boxxit.boxxit.app.events.InitEvent;
import com.boxxit.boxxit.app.events.RetryClickEvent;
import com.boxxit.boxxit.app.events.UIEvent;
import com.boxxit.boxxit.app.events.BoolEvent;
import com.boxxit.boxxit.app.results.LoadEventsResult;
import com.boxxit.boxxit.app.results.LoadProfileResult;
import com.boxxit.boxxit.app.results.NavigateResult;
import com.boxxit.boxxit.app.results.Result;
import com.boxxit.boxxit.app.results.TutorialResult;
import com.boxxit.boxxit.app.views.ErrorView;
import com.boxxit.boxxit.datastore.DataStore;
import com.boxxit.boxxit.library.invite.InviteRequest;
import com.boxxit.boxxit.library.invite.InviteTask;
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
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Action2;
import rx.functions.Action4;
import rx.functions.Func1;
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

    // TODO: 04/09/2017 to fix this thing, this should be in the activity state somehow
    List<Profile> tmpEvents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //
        // initial state
        MainUIState initialState = MainUIState.INITIAL;

        //
        // UI & other events
        append = PublishSubject.create();
        Observable<InitEvent> init = Observable.just(new InitEvent());
        Observable<ClickEvent> explore = RxView.clicks(mainButton).map(ClickEvent::new);
        Observable<RetryClickEvent> retries = RxView.clicks(errorView.retry).map(RetryClickEvent::new);
        Observable<ClickEvent> invites = RxView.clicks(inviteView.retry).map(ClickEvent::new);
        Observable<BoolEvent> tutorial1 = getBooleanExtras("hasTutorial").toObservable()
                .filter(hasTutorial -> hasTutorial)
                .map(BoolEvent::new);
        Observable<BoolEvent> tutorial3 = getActivityResult()
                .filter(integer -> integer == 101)
                .flatMap(integer -> DataStore.shared().getThirdTutorialSeen(MainActivity.this))
                .filter(aBoolean -> !aBoolean)
                .map(BoolEvent::new);

        Observable<UIEvent> events = Observable.merge(init, retries, invites, append.asObservable(), tutorial1, tutorial3);

        //
        // profile transformer
        Observable.Transformer<InitEvent, LoadProfileResult> profileTransformer = initEventObservable -> init
                .flatMap(initEvent -> Observable.just(DataStore.getOwnId()))
                .flatMap(userId -> UserWorker.getProfile(userId).toObservable())
                .map(LoadProfileResult::success)
                .onErrorReturn(LoadProfileResult::error)
                .observeOn(AndroidSchedulers.mainThread());

        //
        // events transformer
        Observable.Transformer<UIEvent, LoadEventsResult> eventsTransformer = eventObservable -> events
                .flatMap(initEvent -> Observable.just(DataStore.getOwnId()))
                .flatMap(userId -> UserWorker.getEventsForUser(userId, offset[0]).toObservable()
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
        // tutorial transformers
        Observable.Transformer<BoolEvent, TutorialResult> tutorial1Transformer = valueEventObservable -> tutorial1
                .map(booleanValueEvent -> TutorialResult.PRESENT1);
        Observable.Transformer<BoolEvent, TutorialResult> tutorial2Transformer = integerEventObservable -> tutorial3
                .map(integerEvent -> TutorialResult.PRESENT3);

        //
        // main transformer
        Observable.Transformer<UIEvent, Result> transformer = observable -> Observable.merge(
                observable.ofType(UIEvent.class).compose(eventsTransformer),
                observable.ofType(InitEvent.class).compose(profileTransformer),
                observable.ofType(ClickEvent.class).compose(exploreTransformer),
                observable.ofType(BoolEvent.class).compose(tutorial1Transformer),
                observable.ofType(BoolEvent.class).compose(tutorial2Transformer)
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
            LoadProfileResult loadProfileResult = (LoadProfileResult) result;
            switch (loadProfileResult) {
                case SUCCESS:
                    return MainUIState.PROFILE_SUCCESS(loadProfileResult.profile);
                case ERROR:
                    return MainUIState.PROFILE_ERROR(loadProfileResult.throwable);
                default:
                    return previousState;
            }
        } else if (result instanceof LoadEventsResult) {
            LoadEventsResult loadEventsResult = (LoadEventsResult) result;
            switch (loadEventsResult) {
                case LOADING:
                    return MainUIState.EVENTS_LOADING;
                case SUCCESS:
                    return loadEventsResult.events.size() > 0 ?
                            MainUIState.EVENTS_SUCCESS(loadEventsResult.events) :
                            MainUIState.EVENTS_EMPTY;
                case ERROR:
                    return MainUIState.EVENTS_ERROR(loadEventsResult.error);
                default:
                    return previousState;
            }
        } else if (result instanceof TutorialResult) {
            TutorialResult tutorialResult = (TutorialResult) result;
            switch (tutorialResult) {
                case PRESENT1:
                    return MainUIState.PRESENT_TUTORIAL1;
                case PRESENT3:
                    return MainUIState.PRESENT_TUTORIAL3;
                default:
                    return previousState;
            }
        } else if (result instanceof NavigateResult) {
            return MainUIState.GOTO_EXPLORE;
        } else {
            return previousState;
        }
    }

    private void stateHandler (MainUIState state) {
        switch (state) {
            case INITIAL:
                populateInitialUI();
                break;
            case PROFILE_SUCCESS:
                populateOwnProfileUI(state.profile);
                break;
            case PROFILE_ERROR:
                break;
            case EVENTS_LOADING:
                populateLoadingUI();
                break;
            case EVENTS_SUCCESS:
                populateSuccessUI(state.events);
                break;
            case EVENTS_EMPTY:
                populateEmptyUI();
                break;
            case EVENTS_ERROR:
                populateErrorUI(state.throwable);
                break;
            case GOTO_EXPLORE:
                gotoNextScreen(DataStore.getOwnId());
                break;
            case PRESENT_TUTORIAL1:
                DataStore.shared().setFirstTutorialSeen(this);
                presentTutorial1();
                break;
            case PRESENT_TUTORIAL3:
                DataStore.shared().setThirdTutorialSeen(this);
                presentTutorial3();
                break;
        }
    }

    private void populateOwnProfileUI (Profile profile) {
        profileName.setText(profile.name);
        profileBirthday.setText(getBirthday(profile));

        Picasso.with(MainActivity.this)
                .load(profile.picture.data.url)
                .placeholder(R.drawable.ic_user_default)
                .error(R.drawable.ic_user_default)
                .transform(new RoundedCornersTransformation(25, 0))
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

        // TODO: 04/09/2017 to fix this thing, this should be in the activity state somehow
        tmpEvents.addAll(events);
        List<Object> result = new ArrayList<>();
        result.addAll(tmpEvents);

        adapter.update(result);
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
                .setLayoutManger(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false))
                .customizeRow(R.layout.row_event, Profile.class, (position, view, profile, total) -> {

                    ImageView profilePicture = (ImageView) view.findViewById(R.id.ProfilePicture);
                    TextView profileName = (TextView) view.findViewById(R.id.ProfileName);
                    TextView profileBirthday = (TextView) view.findViewById(R.id.ProfileBirthday);

                    profileName.setText(profile.name);
                    profileBirthday.setText(getBirthday(profile));
                    Picasso.with(MainActivity.this).load(profile.picture.data.url).into(profilePicture);
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
        boolean hasTutorial = getBooleanExtrasDirect("hasTutorial");
        intent.putExtra("hasTutorial", hasTutorial);
        startActivityForResult(intent, 100);
    }

    void presentTutorial1 () {
        Intent tutorial = new Intent(this, TutorialActivity.class);
        startActivity(tutorial);
    }

    void presentTutorial3 () {
        Intent tutorial3 = new Intent(MainActivity.this, TutorialActivity.class);
        tutorial3.putExtra("hasToFinish", true);
        startActivity(tutorial3);
    }

    private String getBirthday(Profile profile) {
        boolean isToday = profile.isBirthdayToday();

        if (isToday) {
            return getString(R.string.birthday_today);
        } else {
            String bday = profile.getNextBirthday();
            return bday != null ? bday : getString(R.string.birthday_no_data);
        }
    }

    public void executeInvite (View view) {

        InviteRequest request = new InviteRequest(MainActivity.this);
        InviteTask task = new InviteTask();
        task.execute(request)
                .subscribe(aVoid -> {
                    Log.d("Boxxit", "Executing invite");
                }, throwable -> {
                    Log.e("Boxxit", "Error trying to invite " + throwable.getMessage());
                });
    }
}
