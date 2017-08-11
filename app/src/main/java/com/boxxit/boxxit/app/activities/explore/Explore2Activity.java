package com.boxxit.boxxit.app.activities.explore;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.boxxit.boxxit.R;
import com.boxxit.boxxit.app.activities.BaseActivity;
import com.boxxit.boxxit.app.events.AppendEvent;
import com.boxxit.boxxit.app.events.ClickEvent;
import com.boxxit.boxxit.app.events.InitEvent;
import com.boxxit.boxxit.app.events.UIEvent;
import com.boxxit.boxxit.app.results.LoadProductsResult;
import com.boxxit.boxxit.app.results.LoadProfileResult;
import com.boxxit.boxxit.app.results.Result;
import com.boxxit.boxxit.app.views.ErrorView;
import com.boxxit.boxxit.workers.ProductsWorker;
import com.boxxit.boxxit.workers.UserWorker;
import com.gabrielcoman.rxrecyclerview.RxAdapter;
import com.jakewharton.rxbinding.view.RxView;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public class Explore2Activity extends BaseActivity {

    @BindView(R.id.ErrorView) ErrorView errorView;
    @BindView(R.id.Spinner) ProgressBar spinner;
    @BindView(R.id.ProductsRecyclerView) RecyclerView recyclerView;

    @BindView(R.id.ProfilePicture) ImageView profilePicture;
    @BindView(R.id.ProfileName) TextView profileName;
    @BindView(R.id.ProfileBirthday) TextView profileBirthday;
    @BindView(R.id.SeeLikesButton) ImageButton seeLikes;

    private RxAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);
        ButterKnife.bind(this);

        //
        // initial state
        ExploreUIState initialState = ExploreUIState.initial();

        //
        // streams of either UI events or automated (initial) events
        Observable<InitEvent> init = Observable.just(new InitEvent());
        Observable<ClickEvent> retries = RxView.clicks(errorView.retry).map(ClickEvent::new);
        PublishSubject<AppendEvent> append = PublishSubject.create();
        Observable<UIEvent> events = Observable.merge(init, retries, append);

        //
        // products transformer
        Observable.Transformer<InitEvent, LoadProfileResult> profileTransformer = initEventObservable -> events
                .flatMap(uiEvent -> getStringExtras("profile").toObservable())
                .flatMap(userId -> UserWorker.getProfile(userId).toObservable())
                .map(LoadProfileResult::success)
                .onErrorReturn(LoadProfileResult::error)
                .observeOn(AndroidSchedulers.mainThread());

        //
        // product transformer
        Observable.Transformer<UIEvent, LoadProductsResult> productsTransformer = uiEventObservable -> events
                .flatMap(uiEvent -> getStringExtras("profile").toObservable())
                .flatMap(userId -> ProductsWorker.getProductsForUser(userId, 500, 5000).asObservable()
                        .map(LoadProductsResult::success)
                        .onErrorReturn(LoadProductsResult::error)
                        .startWith(LoadProductsResult.LOADING))
                .observeOn(AndroidSchedulers.mainThread());

        //
        // merged transformer and scan into state
        Observable.Transformer<UIEvent, Result> transformer =
                evt -> evt.publish(evt1 -> Observable.merge(
                    evt1.ofType(InitEvent.class).compose(profileTransformer),
                    evt1.ofType(UIEvent.class).compose(productsTransformer)
                ));

        //
        // state updates observer
        Observable<ExploreUIState> state = events.compose(transformer)
                .scan(initialState, this::stateReducer);

        //
        // UI updates
        state.subscribe(this::updateUI, throwable -> Log.e("Boxxit", "Error is " + throwable.getMessage()));
    }

    private ExploreUIState stateReducer (ExploreUIState previousState, Result result) {
        if (result instanceof LoadProfileResult) {
            if (result == LoadProfileResult.SUCCESS) {
                return ExploreUIState.profileSuccess(((LoadProfileResult) result).profile);
            } else {
                return ExploreUIState.error(((LoadProfileResult) result).throwable);
            }
        }
        else if (result instanceof LoadProductsResult) {
            if (result == LoadProductsResult.SUCCESS) {
                return ExploreUIState.productsSuccess(((LoadProductsResult) result).products);
            } else if (result == LoadProductsResult.LOADING) {
                return ExploreUIState.isLoading();
            } else {
                return ExploreUIState.error(((LoadProductsResult) result).throwable);
            }
        }
        else {
            return previousState;
        }
    }

    public void updateUI (ExploreUIState state) {
        Log.d("Boxxit", "Explore UI Model State:\n\tisLoading: " + state.isLoading + "\n\tprofileSuccess: " + state.profileSuccess + "\n\tproductSuccess: " + state.productSuccess + "\n\terror: " + state.error);
    }

//    public void backAction (View view) {
//        this.finishOK();
//    }
//
//    private void updateProfileUI (Profile profile) {
//        profileName.setText(profile.name);
//        profileBirthday.setText(profile.birthday);
//
//        Picasso.with(Explore2Activity.this)
//                .load(profile.picture.data.url)
//                .placeholder(R.drawable.ic_user_default)
//                .error(R.drawable.ic_user_default)
//                .transform(new CropCircleTransformation())
//                .into(profilePicture);
//    }
//
//    private void updateProductsUI (List<Product> products) {
//        Log.d("Boxxit", "Received  " + products.size() + " products ");
//    }
}
