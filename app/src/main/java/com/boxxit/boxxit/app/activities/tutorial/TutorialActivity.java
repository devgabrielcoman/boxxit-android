package com.boxxit.boxxit.app.activities.tutorial;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.boxxit.boxxit.R;
import com.boxxit.boxxit.app.activities.BaseActivity;
import com.boxxit.boxxit.app.events.ClickEvent;
import com.boxxit.boxxit.app.events.InitEvent;
import com.boxxit.boxxit.app.events.UIEvent;
import com.boxxit.boxxit.app.results.TutorialResult;
import com.boxxit.boxxit.datastore.DataStore;
import com.boxxit.boxxit.library.parse.models.facebook.Profile;
import com.boxxit.boxxit.workers.UserWorker;
import com.jakewharton.rxbinding.view.RxView;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;
import rx.Observable;

public class TutorialActivity extends BaseActivity {

    @BindView(R.id.ProfilePicture) ImageView profilePicture;
    @BindView(R.id.TutorialPrimaryText) TextView primaryText;
    @BindView(R.id.TutorialSecondaryText) TextView secondaryText;
    @BindView(R.id.ContinueText) TextView continueText;
    @BindView(R.id.ContinueButton) Button continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        ButterKnife.bind(this);

        //
        // initial state, depending on when this starts
        TutorialUIState initialState = TutorialUIState.INITIAL;

        //
        // UI & other events
        Observable<InitEvent> init = Observable.just(new InitEvent());
        Observable<ClickEvent> clicks = RxView.clicks(continueButton).map(ClickEvent::new);
        Observable<UIEvent> events = Observable.merge(init, clicks);

        //
        // init transformer
        Observable.Transformer<InitEvent, TutorialResult> initTutorialTransformer = initEventObservable -> init
                .flatMap(initEvent -> Observable.just(DataStore.getOwnId()))
                .flatMap(userId -> UserWorker.getProfile(userId).toObservable())
                .map(TutorialResult::advance)
                .onErrorReturn(TutorialResult::error);

        //
        // advances tutorial transformer
        Observable.Transformer<ClickEvent, TutorialResult> advanceTutorialTransformer = clickEventObservable -> clicks
                .map(uiEvent -> TutorialResult.ADVANCE);

        //
        // main transformer
        Observable.Transformer<UIEvent, TutorialResult> transformer = observable -> Observable.merge(
                observable.ofType(InitEvent.class).compose(initTutorialTransformer),
                observable.ofType(ClickEvent.class).compose(advanceTutorialTransformer)
        );

        //
        // state updates observer
        Observable<TutorialUIState> state = events.compose(transformer).scan(initialState, this::stateReducer);

        //
        // UI updates
        state.subscribe(this::stateHandler, throwable -> Log.e("Boxxit", "Error is " + throwable.getMessage()));
    }

    private TutorialUIState stateReducer (TutorialUIState previousState, TutorialResult result) {

        switch (result) {
            case ADVANCE:
                switch (previousState) {
                    case INITIAL:
                        return TutorialUIState.STEP_1.withProfile(result.profile);
                    case STEP_1:
                        return TutorialUIState.STEP_2;
                    case STEP_2:
                        return TutorialUIState.STEP_3;
                    case STEP_3:
                        return TutorialUIState.DONE;
                    case ERROR:
                    default:
                        return previousState;
                }
            case ERROR:
                return TutorialUIState.ERROR(result.error);
            default:
                return previousState;
        }
    }

    private void stateHandler (TutorialUIState state) {
        switch (state) {
            case INITIAL:
                break;
            case STEP_1:
                populateTutorial1UI(state.profile);
                break;
            case STEP_2:
                populateTutorial2UI();
                break;
            case STEP_3:
                populateTutorial3UI();
                break;
            case DONE:
                dismissTutorial();
                break;
            case ERROR:
                populateErrorUI();
                break;
        }
    }

    private void populateTutorial1UI (Profile profile) {

        primaryText.setText(getString(R.string.activity_tutorial_welcome_text, profile.name));
        continueText.setText(R.string.activity_tutorial_continue_text);
        Picasso.with(TutorialActivity.this)
                .load(profile.picture.data.url)
                .transform(new RoundedCornersTransformation(25, 0))
                .into(profilePicture);
    }

    private void populateTutorial2UI () {
        secondaryText.setText(R.string.activity_tutorial_step_2);
        continueText.setText(R.string.activity_tutorial_continue_text);
    }

    private void populateTutorial3UI () {
        secondaryText.setText(R.string.activity_tutorial_step_3);
        continueText.setText(R.string.activity_tutorial_start_text);
    }

    private void dismissTutorial () {
        finishOK();
    }

    private void populateErrorUI () {
        finishOK();
    }
}
