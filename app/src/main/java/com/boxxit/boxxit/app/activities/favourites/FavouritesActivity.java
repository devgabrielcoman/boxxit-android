package com.boxxit.boxxit.app.activities.favourites;

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
import com.boxxit.boxxit.app.events.BackClickEvent;
import com.boxxit.boxxit.app.events.InitEvent;
import com.boxxit.boxxit.app.events.RemoveEvent;
import com.boxxit.boxxit.app.events.RetryClickEvent;
import com.boxxit.boxxit.app.events.UIEvent;
import com.boxxit.boxxit.app.results.LoadProductsResult;
import com.boxxit.boxxit.app.results.LoadProfileResult;
import com.boxxit.boxxit.app.results.NavigateResult;
import com.boxxit.boxxit.app.results.Result;
import com.boxxit.boxxit.app.views.ErrorView;
import com.boxxit.boxxit.datastore.DataStore;
import com.boxxit.boxxit.library.parse.models.Product;
import com.boxxit.boxxit.library.parse.models.facebook.Profile;
import com.boxxit.boxxit.workers.ProductsWorker;
import com.boxxit.boxxit.workers.UserWorker;
import com.gabrielcoman.rxrecyclerview.RxAdapter;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jakewharton.rxbinding.view.RxView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class FavouritesActivity extends BaseActivity {

    @BindView(R.id.ErrorView) ErrorView errorView;
    @BindView(R.id.Spinner) ProgressBar spinner;
    @BindView(R.id.FavouritesRecyclerView) RecyclerView recyclerView;

    @BindView(R.id.ProfilePicture) ImageView profilePicture;
    @BindView(R.id.ProfileName) TextView profileName;
    @BindView(R.id.ProfileBirthday) TextView profileBirthday;
    @BindView(R.id.BackButton) ImageButton backButton;

    private RxAdapter adapter;

    private FirebaseAnalytics mFirebaseAnalytics;
    List<Product> tmpList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);
        ButterKnife.bind(this);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //
        // UI & other events
        Observable<InitEvent> init = Observable.just(new InitEvent());
        Observable<RetryClickEvent> retries = RxView.clicks(errorView.retry).map(RetryClickEvent::new);
        Observable<BackClickEvent> back = RxView.clicks(backButton).map(BackClickEvent::new);
        Observable<UIEvent> events = Observable.merge(init, retries, back);

        //
        // initial state
        FavouritesUIState initialState = FavouritesUIState.INITIAL;

        //
        // profile transformer
        Observable.Transformer<InitEvent, LoadProfileResult> profileTransformer = initEventObservable -> init
                .flatMap(initEvent -> getStringExtras("profile").toObservable())
                .flatMap(userId -> UserWorker.getProfile(userId).toObservable())
                .map(LoadProfileResult::success)
                .onErrorReturn(LoadProfileResult::error)
                .observeOn(AndroidSchedulers.mainThread());

        //
        // products transformer
        Observable.Transformer<UIEvent, LoadProductsResult> productsTransformer = eventObservable -> events
                .flatMap(initEvent -> getStringExtras("profile").toObservable())
                .flatMap(userId -> ProductsWorker.getFavouriteProductsForUser(userId).asObservable()
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
            LoadProfileResult loadProfileResult = (LoadProfileResult) result;
            switch (loadProfileResult) {
                case SUCCESS:
                    return FavouritesUIState.PROFILE_SUCCESS(loadProfileResult.profile);
                case ERROR:
                    return FavouritesUIState.PROFILE_ERROR(loadProfileResult.throwable);
                default:
                    return previousState;
            }
        }
        else if (result instanceof LoadProductsResult) {
            LoadProductsResult loadProductsResult = (LoadProductsResult) result;
            switch (loadProductsResult) {
                case LOADING:
                    return FavouritesUIState.PRODUCTS_LOADING;
                case SUCCESS:
                    return loadProductsResult.products.size() > 0 ?
                            FavouritesUIState.PRODUCTS_SUCCESS(loadProductsResult.products) :
                            FavouritesUIState.PRODUCTS_EMPTY;
                case ERROR:
                    return FavouritesUIState.PROFILE_ERROR(loadProductsResult.throwable);
                default:
                    return previousState;
            }
        }
        else if (result instanceof NavigateResult) {
            NavigateResult navigateResult = (NavigateResult) result;
            switch (navigateResult) {
                case BACK:
                    return FavouritesUIState.GO_BACK;
                case NEXT:
                default:
                    return previousState;
            }
        }
        else {
            return previousState;
        }
    }

    public void stateHandler(FavouritesUIState state) {
        switch (state) {
            case INITIAL:
                updateInitialUI();
                break;
            case PROFILE_SUCCESS:
                updateProfileUI(state.profile);
                break;
            case PROFILE_ERROR:
                break;
            case PRODUCTS_LOADING:
                updateLoadingUI();
                break;
            case PRODUCTS_SUCCESS:
                this.tmpList = state.products;
                updateProductsUI(this.tmpList);
                break;
            case PRODUCTS_EMPTY:
                updateEmptyUI();
                break;
            case PRODUCTS_ERROR:
                updateErrorUI(state.throwable);
                break;
            case GO_BACK:
                gotoBack();
                break;
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
        profileBirthday.setText(getBirthday(profile));

        Picasso.with(FavouritesActivity.this)
                .load(profile.picture.data.url)
                .placeholder(R.drawable.ic_user_default)
                .error(R.drawable.ic_user_default)
                .transform(new RoundedCornersTransformation(25, 0))
                .into(profilePicture);
    }

    private void updateProductsUI (List<Product> products) {
        spinner.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        adapter.update(products);
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
                    ImageButton removeButton = (ImageButton) view.findViewById(R.id.RemoveButton);
                    Button buyAmazon = (Button) view.findViewById(R.id.BuyOnAmazonButton);

                    //
                    // UGH!!!!
                    String own = DataStore.getOwnId();
                    String comp = getStringExtrasDirect("profile");
                    boolean isVisible = false;
                    try {
                        isVisible = own != null && own.equals(comp);
                    } catch (NullPointerException e) {
                        isVisible = false;
                    }
                    removeButton.setVisibility(isVisible ? View.VISIBLE : View.GONE);

                    productName.setText(product.title);
                    productPrice.setText(product.price);

                    Picasso.with(FavouritesActivity.this)
                            .load(product.largeIcon)
                            .placeholder(R.drawable.no_ama_pic)
                            .into(productImage);

                    //
                    // remove action
                    removeButton.setOnClickListener(v -> FavouritesActivity.this.removeProduct(product));

                    //
                    // buy action
                    buyAmazon.setOnClickListener(v -> {
                        //
                        // open Url
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(product.click)));

                        //
                        // get current user
                        String ownId = DataStore.getOwnId();
                        String fbUser = getStringExtrasDirect("profile");
                        Profile profile = DataStore.shared().getProfile(fbUser);

                        Bundle params = new Bundle();

                        //
                        // prep data
                        params.putString("user_id", ownId);
                        params.putString("friend_id", profile.id);
                        params.putString("friend_name", profile.name);
                        params.putString("product_id", product.asin);
                        params.putString("product_name", product.title);

                        //
                        // send analytics
                        mFirebaseAnalytics.logEvent("view_product", params);
                    });
                });
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

    private void removeProduct (Product product) {
        String userId = getStringExtrasDirect("profile");
        ProductsWorker.deleteFavouriteProduct(product.asin, userId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aVoid -> {
                    FavouritesActivity.this.tmpList.remove(product);
                    FavouritesActivity.this.adapter.update(FavouritesActivity.this.tmpList);
                }, throwable -> Log.d("Boxxit", "Could not delete product " + product.asin + " from user " + userId + " because: " + throwable.getMessage()));
    }
}
