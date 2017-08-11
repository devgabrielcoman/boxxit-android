package com.boxxit.boxxit.app.activities.explore;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.boxxit.boxxit.R;
import com.boxxit.boxxit.app.activities.BaseActivity;
import com.boxxit.boxxit.app.activities.explore.events.ClickEvent;
import com.boxxit.boxxit.app.activities.explore.events.InitEvent;
import com.boxxit.boxxit.app.activities.explore.events.AppendEvent;
import com.boxxit.boxxit.app.activities.explore.events.UIEvent;
import com.boxxit.boxxit.app.views.ErrorView;
import com.boxxit.boxxit.library.parse.models.Product;
import com.boxxit.boxxit.library.parse.models.facebook.Profile;
import com.boxxit.boxxit.workers.ProductsWorker;
import com.boxxit.boxxit.workers.UserWorker;
import com.gabrielcoman.rxrecyclerview.RxAdapter;
import com.jakewharton.rxbinding.view.RxView;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
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

        Observable<InitEvent> init = Observable.just(new InitEvent());
        Observable<ClickEvent> retries = RxView.clicks(errorView.retry).map(ClickEvent::new);
        PublishSubject<AppendEvent> append = PublishSubject.create();

        Observable<UIEvent> events = Observable.merge(init, retries, append);

        //
        // this will return a result
        Observable.Transformer<InitEvent, ExploreUIModel> rxtProfile = uiEventObservable -> events
                .flatMap(uiEvent -> getStringExtras("profile").toObservable())
                .flatMap(userId -> UserWorker.getProfile(userId).toObservable())
                .map(ExploreUIModel::profileSuccess)
                .onErrorReturn(ExploreUIModel::error)
                .observeOn(AndroidSchedulers.mainThread());

        //
        // this will return a result also
        Observable.Transformer<UIEvent, ExploreUIModel> rxtProducts = uiEventObservable -> events
                .flatMap(uiEvent -> getStringExtras("profile").toObservable())
                .flatMap(userId -> ProductsWorker.getProductsForUser(userId, 500, 5000).asObservable()
                        .map(ExploreUIModel::productsSuccess)
                        .onErrorReturn(ExploreUIModel::error)
                        .startWith(ExploreUIModel.isLoading()))
                .observeOn(AndroidSchedulers.mainThread());

        //
        // this will merge the two results
        Observable.Transformer<UIEvent, ExploreUIModel> rxtMerged = uiEventObservable ->
                events.publish(evt -> Observable.merge(
                        evt.ofType(InitEvent.class).compose(rxtProfile),
                        evt.ofType(UIEvent.class).compose(rxtProducts)
                ));

        //
        // and have to add a scan trat transforms a state a result into a new UI state
        // Observable<ExploreUIModel> models = rxtMerged.scan(initialUIState, (state, result) -> ...)

        events.compose(rxtMerged).subscribe(new Action1<ExploreUIModel>() {
            @Override
            public void call(ExploreUIModel model) {
                Log.d("Boxxit", "Explore UI Model State:\n\tisLoading: " + model.isLoading + "\n\tprofileSuccess: " + model.profileSuccess + "\n\tproductSuccess: " + model.productSuccess + "\n\terror: " + model.error);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Log.e("Boxxit", "Error is " + throwable.getMessage());
            }
        });
    }

    public void backAction (View view) {
        this.finishOK();
    }

    private void updateProfileUI (Profile profile) {
        profileName.setText(profile.name);
        profileBirthday.setText(profile.birthday);

        Picasso.with(Explore2Activity.this)
                .load(profile.picture.data.url)
                .placeholder(R.drawable.ic_user_default)
                .error(R.drawable.ic_user_default)
                .transform(new CropCircleTransformation())
                .into(profilePicture);
    }

    private void updateProductsUI (List<Product> products) {
        Log.d("Boxxit", "Received  " + products.size() + " products ");
    }
}
