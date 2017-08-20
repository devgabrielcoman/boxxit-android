package com.boxxit.boxxit.app.activities.tutorial;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.boxxit.boxxit.R;
import com.boxxit.boxxit.app.activities.BaseActivity;
import com.boxxit.boxxit.app.events.ClickEvent;
import com.boxxit.boxxit.app.events.InitEvent;
import com.boxxit.boxxit.app.events.UIEvent;
import com.boxxit.boxxit.app.results.TutorialResult;
import com.boxxit.boxxit.app.views.TutorialView;
import com.boxxit.boxxit.datastore.DataStore;
import com.boxxit.boxxit.library.parse.models.facebook.Profile;
import com.boxxit.boxxit.workers.UserWorker;
import com.jakewharton.rxbinding.view.RxView;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import rx.Observable;
import rx.functions.Func1;

public class TutorialActivity extends BaseActivity {

    @BindView(R.id.MainTutorialView) RelativeLayout mainView;
    @BindView(R.id.DismissButton) Button dismissButton;
    @BindView(R.id.Tutorial1) TutorialView tutorial1View;
    @BindView(R.id.Tutorial2) TutorialView tutorial2View;
    @BindView(R.id.Tutorial3) TutorialView tutorial3View;
    @BindView(R.id.Tutorial4) TutorialView tutorial4View;
    @BindView(R.id.Tutorial5) TutorialView tutorial5View;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        ButterKnife.bind(this);

        //
        // get start vars that influence the initial state
        boolean startInExplore = getBooleanExtrasDirect("startInExplore");
        boolean hasToFinish = getBooleanExtrasDirect("hasToFinish");

        //
        // initial state, depending on when this starts
        TutorialUIState initialState =
                hasToFinish ? TutorialUIState.TUTORIAL_4 :
                    startInExplore ? TutorialUIState.TUTORIAL_3 : TutorialUIState.INITIAL;

        //
        // UI & other events
        Observable<InitEvent> init = Observable.just(new InitEvent());
        Observable<ClickEvent> clicks = RxView.clicks(mainView).map(ClickEvent::new);
        Observable<ClickEvent> dismiss = RxView.clicks(dismissButton).map(ClickEvent::new);
        Observable<UIEvent> events = Observable.merge(init, clicks, dismiss);

        //
        // init transformer
        Observable.Transformer<InitEvent, TutorialResult> initTutorialTransformer = initEventObservable -> init
                .flatMap(initEvent -> Observable.just(DataStore.getOwnId()))
                .flatMap(userId -> UserWorker.getProfile(userId).toObservable())
                .map(TutorialResult::gotoNext)
                .onErrorReturn(TutorialResult::error);

        //
        // advances tutorial transformer
        Observable.Transformer<ClickEvent, TutorialResult> advanceTutorialTransformer = clickEventObservable -> clicks
                .map(uiEvent -> TutorialResult.GOTO_NEXT_TUTORIAL);

        //
        // dismiss transformer
        Observable.Transformer<ClickEvent, TutorialResult> dismissTransformer = clickEventObservable -> dismiss
                .map(clickEvent -> TutorialResult.DISMISS);

        //
        // main transformer
        Observable.Transformer<UIEvent, TutorialResult> transformer = observable -> Observable.merge(
                observable.ofType(InitEvent.class).compose(initTutorialTransformer),
                observable.ofType(ClickEvent.class).compose(advanceTutorialTransformer),
                observable.ofType(ClickEvent.class).compose(dismissTransformer)
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
            case DISMISS:
                return TutorialUIState.DISMISSED;
            case GOTO_NEXT_TUTORIAL:
                switch (previousState) {
                    case INITIAL:
                        return TutorialUIState.TUTORIAL_1.withProfile(result.profile);
                    case TUTORIAL_1:
                        return TutorialUIState.TUTORIAL_2;
                    case TUTORIAL_2:
                        return TutorialUIState.TUTORIAL_3;
                    case TUTORIAL_3:
                        return TutorialUIState.TUTORIAL_4;
                    case TUTORIAL_4:
                        return TutorialUIState.TUTORIAL_5;
                    case TUTORIAL_5:
                    case DISMISSED:
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
            case TUTORIAL_1:
                populateTutorial1UI(state.profile);
                break;
            case TUTORIAL_2:
                populateTutorial2UI();
                break;
            case TUTORIAL_3:
                populateTutorial3UI();
                break;
            case TUTORIAL_4:
                populateTutorial4UI();
                break;
            case TUTORIAL_5:
                populateTutorial5UI();
                break;
            case DISMISSED:
                dismissTutorial();
                break;
            case ERROR:
                populateErrorUI();
                break;
        }
    }

    private void populateTutorial1UI (Profile profile) {

        tutorial1View.bind(R.layout.view_tutorial1);
        tutorial1View.setVisibility(View.VISIBLE);

        tutorial1View.tutorialText.setText(getString(R.string.tutorial_1_text, profile.name));

        Picasso.with(TutorialActivity.this)
                .load(profile.picture.data.url)
                .placeholder(R.drawable.ic_user_default)
                .error(R.drawable.ic_user_default)
                .transform(new CropCircleTransformation())
                .into(tutorial1View.profilePicture);
    }

    private void populateTutorial2UI () {
        tutorial1View.setVisibility(View.GONE);
        tutorial2View.setVisibility(View.VISIBLE);
        tutorial2View.bind(R.layout.view_tutorial2);
    }

    private void populateTutorial3UI () {
        tutorial2View.setVisibility(View.GONE);
        tutorial3View.setVisibility(View.VISIBLE);
        tutorial3View.bind(R.layout.view_tutorial3_1);
        dismissButton.setEnabled(true);
        dismissButton.setClickable(true);
        dismissButton.setVisibility(View.VISIBLE);
    }

    private void populateTutorial4UI () {
        tutorial3View.setVisibility(View.GONE);
        tutorial4View.setVisibility(View.VISIBLE);
        tutorial4View.bind(R.layout.view_tutorial4);
        dismissButton.setEnabled(true);
        dismissButton.setClickable(true);
        dismissButton.setVisibility(View.VISIBLE);
    }

    private void populateTutorial5UI () {
        tutorial3View.setVisibility(View.GONE);
        tutorial4View.setVisibility(View.GONE);
        tutorial5View.setVisibility(View.VISIBLE);
        tutorial5View.bind(R.layout.view_tutorial5);
        dismissButton.setEnabled(true);
        dismissButton.setClickable(true);
        dismissButton.setVisibility(View.VISIBLE);
    }

    private void dismissTutorial () {
        finishOK();
    }

    private void populateErrorUI () {
        finishOK();
    }
}
