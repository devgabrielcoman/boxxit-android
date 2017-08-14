package com.boxxit.boxxit.app.activities.favourites;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.boxxit.boxxit.R;
import com.boxxit.boxxit.app.activities.BaseActivity;
import com.boxxit.boxxit.app.events.BackClickEvent;
import com.boxxit.boxxit.app.events.InitEvent;
import com.boxxit.boxxit.app.events.RetryClickEvent;
import com.boxxit.boxxit.app.events.UIEvent;
import com.boxxit.boxxit.app.results.LoadProductsResult;
import com.boxxit.boxxit.app.results.LoadProfileResult;
import com.boxxit.boxxit.app.results.NavigateResult;
import com.boxxit.boxxit.app.results.Result;
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

public class FavouritesActivity extends BaseActivity {

    @BindView(R.id.ErrorView) ErrorView errorView;
    @BindView(R.id.Spinner) ProgressBar spinner;
    @BindView(R.id.FavouritesRecyclerView) RecyclerView recyclerView;

    @BindView(R.id.ProfilePicture) ImageView profilePicture;
    @BindView(R.id.ProfileName) TextView profileName;
    @BindView(R.id.ProfileBirthday) TextView profileBirthday;
    @BindView(R.id.BackButton) ImageButton backButton;

    private RxAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);
        ButterKnife.bind(this);

        //
        // get pre-determined values
        String userId = getStringExtrasDirect("profile");

        Observable<InitEvent> init = Observable.just(new InitEvent());
        Observable<RetryClickEvent> retries = RxView.clicks(errorView.retry).map(RetryClickEvent::new);
        Observable<BackClickEvent> back = RxView.clicks(backButton).map(BackClickEvent::new);
        Observable<UIEvent> events = Observable.merge(init, retries, back);

        //
        // initial state
        FavouritesUIState initialState = FavouritesUIState.initial();

        //
        // profile transformer
        Observable.Transformer<InitEvent, LoadProfileResult> profileTransformer = initEventObservable -> init
                .flatMap(initEvent -> UserWorker.getProfile(userId).toObservable())
                .map(LoadProfileResult::success)
                .onErrorReturn(LoadProfileResult::error)
                .observeOn(AndroidSchedulers.mainThread());

        //
        // products transformer
        Observable.Transformer<UIEvent, LoadProductsResult> productsTransformer = eventObservable -> events
                .flatMap(uiEvent -> ProductsWorker.getFavouriteProductsForUser(userId).asObservable()
                        .map(LoadProductsResult::success)
                        .onErrorReturn(LoadProductsResult::error)
                        .startWith(LoadProductsResult.LOADING))
                .observeOn(AndroidSchedulers.mainThread());

        //
        // navigation transformers
        Observable.Transformer<BackClickEvent, NavigateResult> backTransformer = backClickEventObservable -> back
                .map(uiEvent -> NavigateResult.BACK)
                .observeOn(AndroidSchedulers.mainThread());

        //
        // merged transformer and scan into state
        Observable.Transformer<UIEvent, Result> transformer = eventObservable -> Observable.merge(
                eventObservable.ofType(UIEvent.class).compose(productsTransformer),
                eventObservable.ofType(InitEvent.class).compose(profileTransformer),
                eventObservable.ofType(BackClickEvent.class).compose(backTransformer)
        );

        //
        // state updates observer
        Observable<FavouritesUIState> state = events.compose(transformer).scan(initialState, this::stateReducer);

        //
        // UI updates
        state.subscribe(this::stateHandler, throwable -> Log.e("Boxxit", "Error is " + throwable.getMessage()));
    }

    private FavouritesUIState stateReducer (FavouritesUIState previousState, Result result) {
        if (result instanceof LoadProfileResult) {
            if (result == LoadProfileResult.SUCCESS) {
                return FavouritesUIState.profileSuccess(((LoadProfileResult) result).profile);
            } else {
                return FavouritesUIState.error(((LoadProfileResult) result).throwable);
            }
        }
        else if (result instanceof LoadProductsResult) {
            if (result == LoadProductsResult.SUCCESS) {
                if (((LoadProductsResult) result).products.size() > 0) {
                    return FavouritesUIState.productsSuccess(((LoadProductsResult) result).products);
                } else {
                    return FavouritesUIState.productsEmpty();
                }
            } else if (result == LoadProductsResult.LOADING) {
                return FavouritesUIState.isLoading();
            } else {
                return FavouritesUIState.error(((LoadProductsResult) result).throwable);
            }
        }
        else if (result instanceof NavigateResult) {
            if (result == NavigateResult.BACK) {
                return FavouritesUIState.gotoBack();
            } else {
                return previousState;
            }
        }
        else {
            return previousState;
        }
    }

    public void stateHandler(FavouritesUIState state) {
        if (state.isLoading) {
            updateLoadingUI();
        } else if (state.profileSuccess) {
            updateProfileUI(state.profile);
        } else if (state.productSuccess && state.products != null) {
            updateProductsUI(state.products);
        } else if (state.productEmpty) {
            updateEmptyUI();
        } else if (state.error != null) {
            updateErrorUI(state.error);
        } else if (state.goBack) {
            gotoBack();
        } else {
            updateInitialUI();
        }
    }

    private void gotoBack () {
        finishOK();
    }

    private void updateLoadingUI () {
        spinner.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
    }

    private void updateProfileUI (Profile profile) {
        profileName.setText(profile.name);
        profileBirthday.setText(profile.birthday);

        Picasso.with(FavouritesActivity.this)
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
        errorView.errorText.setText(getString(R.string.activity_favourites_error));
        errorView.retry.setVisibility(View.VISIBLE);
    }

    private void updateEmptyUI () {
        errorView.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        errorView.errorText.setText(getString(R.string.activity_favourites_no_products));
        errorView.retry.setVisibility(View.GONE);
    }

    private void updateInitialUI () {
        adapter = RxAdapter.create()
                .bindTo(recyclerView)
                .setLayoutManger(new LinearLayoutManager(getApplicationContext()))
                .customizeRow(R.layout.row_favourite, Product.class, (position, view, product, total) -> {

                    TextView productName = (TextView) view.findViewById(R.id.ProductName);
                    TextView productPrice = (TextView) view.findViewById(R.id.ProductPrice);
                    ImageView productImage = (ImageView) view.findViewById(R.id.ProductImage);

                    productName.setText(product.title);
                    productPrice.setText(product.price);

                    Picasso.with(FavouritesActivity.this)
                            .load(product.largeIcon)
                            .into(productImage);
                })
                .didClickOnRow(Product.class, (integer, product) -> {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(product.click)));
                });
    }
}
