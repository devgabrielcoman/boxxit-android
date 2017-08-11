package com.boxxit.boxxit.app.activities.explore;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.boxxit.boxxit.R;
import com.boxxit.boxxit.app.activities.BaseActivity;
import com.boxxit.boxxit.app.activities.favourites.FavouritesActivity;
import com.boxxit.boxxit.app.events.AppendEvent;
import com.boxxit.boxxit.app.events.ClickEvent;
import com.boxxit.boxxit.app.events.InitEvent;
import com.boxxit.boxxit.app.events.UIEvent;
import com.boxxit.boxxit.app.results.LoadProductsResult;
import com.boxxit.boxxit.app.results.LoadProfileResult;
import com.boxxit.boxxit.app.results.Result;
import com.boxxit.boxxit.app.views.ErrorView;
import com.boxxit.boxxit.datastore.DataStore;
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
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public class ExploreActivity extends BaseActivity {

    @BindView(R.id.ErrorView) ErrorView errorView;
    @BindView(R.id.Spinner) ProgressBar spinner;
    @BindView(R.id.ProductsRecyclerView) RecyclerView recyclerView;

    @BindView(R.id.ProfilePicture) ImageView profilePicture;
    @BindView(R.id.ProfileName) TextView profileName;
    @BindView(R.id.ProfileBirthday) TextView profileBirthday;
    @BindView(R.id.SeeLikesButton) ImageButton seeLikes;

    PublishSubject<AppendEvent> append;
    private RxAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);
        ButterKnife.bind(this);

        //
        // get pre-determined values
        String userId = getStringExtrasDirect("profile");
        int minPrice = 500;
        int maxPrice = 5000;

        //
        // initial state
        ExploreUIState initialState = ExploreUIState.initial();

        //
        // streams of either UI events or automated (initial) events
        Observable<InitEvent> init = Observable.just(new InitEvent());
        Observable<ClickEvent> retries = RxView.clicks(errorView.retry).map(ClickEvent::new);
        append = PublishSubject.create();
        Observable<UIEvent> events = Observable.merge(init, retries, append);

        //
        // products transformer
        Observable.Transformer<InitEvent, LoadProfileResult> profileTransformer = initEventObservable -> events
                .flatMap(uiEvent -> UserWorker.getProfile(userId).toObservable())
                .map(LoadProfileResult::success)
                .onErrorReturn(LoadProfileResult::error)
                .observeOn(AndroidSchedulers.mainThread());

        //
        // product transformer
        Observable.Transformer<UIEvent, LoadProductsResult> productsTransformer = uiEventObservable -> events
                .flatMap(uiEvent -> ProductsWorker.getProductsForUser(userId, minPrice, maxPrice).asObservable()
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
        Observable<ExploreUIState> state = events.compose(transformer).scan(initialState, this::stateReducer);

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

        if (state.isLoading) {
            updateLoadingUI();
        } else if (state.profileSuccess) {
            updateProfileUI(state.profile);
        } else if (state.productSuccess && state.products != null) {
            updateProductsUI(state.products);
        } else if (state.error != null) {
            updateErrorUI(state.error);
        } else {
            updateInitialUI(getStringExtrasDirect("profile"));
        }
    }

    private void gotoBack () {
        finishOK();
    }

    private void gotoFavourites (String facebookUser) {
        Intent intent = new Intent(this, FavouritesActivity.class);
        intent.putExtra("profile", facebookUser);
        startActivity(intent);
    }

    private void updateLoadingUI () {
        spinner.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
    }

    private void updateProfileUI (Profile profile) {
        profileName.setText(profile.name);
        profileBirthday.setText(profile.birthday);

        Picasso.with(ExploreActivity.this)
                .load(profile.picture.data.url)
                .placeholder(R.drawable.ic_user_default)
                .error(R.drawable.ic_user_default)
                .transform(new CropCircleTransformation())
                .into(profilePicture);
    }

    private void updateProductsUI (List<Product> products) {
        spinner.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        adapter.add(products);
    }

    private void updateErrorUI (Throwable throwable) {
        errorView.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
    }

    private void updateInitialUI (String facebookUser) {
        adapter = RxAdapter.create()
                .bindTo(recyclerView)
                .setLayoutManger(new LinearLayoutManager(getApplicationContext()))
                .customizeRow(R.layout.row_product, Product.class, (position, view, product, total) -> {

                    TextView productName = (TextView) view.findViewById(R.id.ProductName);
                    TextView productPrice = (TextView) view.findViewById(R.id.ProductPrice);
                    TextView productReason = (TextView) view.findViewById(R.id.ProductReason);
                    ImageButton likeProduct = (ImageButton) view.findViewById(R.id.LikeButton);
                    ImageView productImage = (ImageView) view.findViewById(R.id.ProductImage);
                    Button amazonButton = (Button) view.findViewById(R.id.AmazonBtn);

                    productName.setText(product.title);
                    productPrice.setText(product.price);
                    productReason.setText(getString(facebookUser.equals(DataStore.getOwnId()) ?
                                    R.string.activity_explore_product_reason_you :
                                    R.string.activity_explore_product_reason_friend,
                            product.categId));
                    likeProduct.setVisibility(facebookUser.equals(DataStore.getOwnId()) ? View.VISIBLE : View.GONE);
                    likeProduct.setImageDrawable(getResources().getDrawable(product.isFavourite ? R.drawable.like : R.drawable.nolike));

                    Picasso.with(ExploreActivity.this)
                            .load(product.largeIcon)
                            .into(productImage);

                    //
                    // when clicking on the amazon button
                    amazonButton.setOnClickListener(v -> {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(product.click)));
                    });

                    //
                    // when clicking on the heart button

                    likeProduct.setOnClickListener(v -> {

                        Single<Void> rxOperation = product.isFavourite ?
                                ProductsWorker.deleteFavouriteProduct(product.asin, facebookUser):
                                ProductsWorker.saveFavouriteProduct(product.asin, facebookUser);

                        rxOperation
                                .doOnSubscribe(() -> {
                                    product.isFavourite = !product.isFavourite;
                                    likeProduct.setImageDrawable(getResources().getDrawable(product.isFavourite ? R.drawable.like : R.drawable.nolike));
                                })
                                .flatMap(aVoid -> rxOperation)
                                .subscribe(aVoid -> {
                                    // do nothing
                                }, throwable -> {
                                    product.isFavourite = !product.isFavourite;
                                    likeProduct.setImageDrawable(getResources().getDrawable(product.isFavourite ? R.drawable.like : R.drawable.nolike));
                                });
                    });
                })
                .didReachEnd(() -> append.onNext(null));
    }
}
