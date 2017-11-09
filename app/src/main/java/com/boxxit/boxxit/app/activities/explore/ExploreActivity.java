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
import com.boxxit.boxxit.app.activities.main.MainActivity;
import com.boxxit.boxxit.app.activities.tutorial.TutorialActivity;
import com.boxxit.boxxit.app.events.AppendEvent;
import com.boxxit.boxxit.app.events.BackClickEvent;
import com.boxxit.boxxit.app.events.BoolEvent;
import com.boxxit.boxxit.app.events.FavouritesClickEvent;
import com.boxxit.boxxit.app.events.InitEvent;
import com.boxxit.boxxit.app.events.RetryClickEvent;
import com.boxxit.boxxit.app.events.UIEvent;
import com.boxxit.boxxit.app.results.LoadProductsResult;
import com.boxxit.boxxit.app.results.LoadProfileResult;
import com.boxxit.boxxit.app.results.NavigateResult;
import com.boxxit.boxxit.app.results.Result;
import com.boxxit.boxxit.app.results.TutorialResult;
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

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;
import rx.Observable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class ExploreActivity extends BaseActivity {

    @BindView(R.id.ErrorView) ErrorView errorView;
    @BindView(R.id.Spinner) ProgressBar spinner;
    @BindView(R.id.ProductsRecyclerView) RecyclerView recyclerView;

    @BindView(R.id.ProfilePicture) ImageView profilePicture;
    @BindView(R.id.ProfileName) TextView profileName;
    @BindView(R.id.ProfileBirthday) TextView profileBirthday;
    @BindView(R.id.SeeLikesButton) ImageButton seeLikes;
    @BindView(R.id.BackButton) ImageButton backButton;

    PublishSubject<AppendEvent> append;
    private RxAdapter adapter;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);
        ButterKnife.bind(this);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //
        // get pre-determined values
        int minPrice = 500;
        int maxPrice = 5000;

        //
        // initial state
        ExploreUIState initialState = ExploreUIState.INITIAL;

        //
        // UI & other events
        Observable<InitEvent> init = Observable.just(new InitEvent());
        Observable<RetryClickEvent> retries = RxView.clicks(errorView.retry).map(RetryClickEvent::new);
        Observable<BackClickEvent> back = RxView.clicks(backButton).map(BackClickEvent::new);
        Observable<FavouritesClickEvent> next = RxView.clicks(seeLikes).map(FavouritesClickEvent::new);
        append = PublishSubject.create();
        Observable<BoolEvent> tutorial2 = getBooleanExtras("hasTutorial").toObservable()
                .filter(aBoolean -> aBoolean)
                .flatMap(aBoolean -> DataStore.shared().getSecondTutorialSeen(ExploreActivity.this))
                .filter(aBoolean -> !aBoolean)
                .map(BoolEvent::new);

        Observable<UIEvent> events = Observable.merge(init, retries, append, next, back, tutorial2);

        //
        // profile transformer
        Observable.Transformer<InitEvent, LoadProfileResult> profileTransformer = initEventObservable -> init
                .flatMap(initEvent -> getStringExtras("profile").toObservable())
                .flatMap(userId -> UserWorker.getProfile(userId).toObservable())
                .map(LoadProfileResult::success)
                .onErrorReturn(LoadProfileResult::error)
                .observeOn(AndroidSchedulers.mainThread());

        //
        // product transformer
        Observable.Transformer<UIEvent, LoadProductsResult> productsTransformer = uiEventObservable -> events
                .flatMap(initEvent -> getStringExtras("profile").toObservable())
                .flatMap(userId -> ProductsWorker.getProductsForUser(userId, minPrice, maxPrice).asObservable()
                        .map(LoadProductsResult::success)
                        .onErrorReturn(LoadProductsResult::error)
                        .startWith(LoadProductsResult.LOADING))
                .observeOn(AndroidSchedulers.mainThread());

        //
        // navigation transformers
        Observable.Transformer<BackClickEvent, NavigateResult> backTransformer = backClickEventObservable -> back
                .map(uiEvent -> NavigateResult.back(101))
                .observeOn(AndroidSchedulers.mainThread());
        Observable.Transformer<FavouritesClickEvent, NavigateResult> favTransformer = favouritesClickEventObservable -> next
                .map(uiEvent -> NavigateResult.NEXT)
                .observeOn(AndroidSchedulers.mainThread());

        //
        // merged transformer and scan into state
        Observable.Transformer<UIEvent, Result> transformer = eventObservable -> Observable.merge(
                eventObservable.ofType(UIEvent.class).compose(productsTransformer),
                eventObservable.ofType(InitEvent.class).compose(profileTransformer),
                eventObservable.ofType(FavouritesClickEvent.class).compose(favTransformer),
                eventObservable.ofType(BackClickEvent.class).compose(backTransformer)
        );

        //
        // state updates observer
        Observable<ExploreUIState> state = events.compose(transformer).scan(initialState, this::stateReducer);

        //
        // UI updates
        state.subscribe(this::stateHandler, throwable -> Log.e("Boxxit", "Error is " + throwable.getMessage()));
    }

    private ExploreUIState stateReducer (ExploreUIState previousState, Result result) {
        if (result instanceof LoadProfileResult) {
            LoadProfileResult loadProfileResult = (LoadProfileResult) result;
            switch (loadProfileResult) {
                case SUCCESS:
                    return ExploreUIState.PROFILE_SUCCESS(loadProfileResult.profile);
                case ERROR:
                    return ExploreUIState.PROFILE_ERROR(loadProfileResult.throwable);
                default:
                    return previousState;
            }
        }
        else if (result instanceof LoadProductsResult) {
            LoadProductsResult loadProductsResult = (LoadProductsResult) result;
            switch (loadProductsResult) {
                case LOADING:
                    return ExploreUIState.PRODUCTS_LOADING;
                case SUCCESS:
                    return ExploreUIState.PRODUCTS_SUCCESS(loadProductsResult.products);
                case ERROR:
                    return ExploreUIState.PRODUCTS_ERROR(loadProductsResult.throwable);
                default:
                    return previousState;
            }
        }
        else if (result instanceof NavigateResult) {
            NavigateResult navigateResult = (NavigateResult) result;
            switch (navigateResult) {
                case BACK:
                    return ExploreUIState.GO_BACK(navigateResult.backResult);
                case NEXT:
                    return ExploreUIState.GOTO_FAVOURITES;
                default:
                    return previousState;
            }
        }
        else {
            return previousState;
        }
    }

    public void stateHandler(ExploreUIState state) {
        switch (state) {
            case INITIAL:
                updateInitialUI(getStringExtrasDirect("profile"));
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
                updateProductsUI(state.products);
                break;
            case PRODUCTS_ERROR:
                updateErrorUI(state.throwable);
                break;
            case GO_BACK:
                gotoBack(state.backResult);
                break;
            case GOTO_FAVOURITES:
                gotoFavourites(getStringExtrasDirect("profile"));
                break;
        }
    }

    private void gotoBack (int backResult) {
        finishOK(backResult);
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
        profileBirthday.setText(getBirthday(profile));

        Picasso.with(ExploreActivity.this)
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
        adapter.add(products);
    }

    private void updateErrorUI (Throwable throwable) {
        errorView.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        Log.e("Boxxit", "Error: " + throwable.getMessage());
        errorView.errorText.setText(getString(R.string.activity_explore_error));
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
                            (product.isOwn ? R.string.activity_explore_product_reason_sure_you : R.string.activity_explore_product_reason_maybe_you) :
                            (product.isOwn ? R.string.activity_explore_product_reason_sure_friend : R.string.activity_explore_product_reason_maybe_friend),
                            capitalize(product.categId)));
                    likeProduct.setVisibility(facebookUser.equals(DataStore.getOwnId()) ? View.VISIBLE : View.GONE);
                    likeProduct.setImageDrawable(getResources().getDrawable(product.isFavourite ? R.drawable.like : R.drawable.nolike));

                    Picasso.with(ExploreActivity.this)
                            .load(product.largeIcon)
                            .placeholder(R.drawable.no_ama_pic)
                            .into(productImage);

                    //
                    // when clicking on the amazon button
                    amazonButton.setOnClickListener(v -> {
                        //
                        // open activity
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

    private String getBirthday(Profile profile) {
        boolean isToday = profile.isBirthdayToday();

        if (isToday) {
            return getString(R.string.birthday_today);
        } else {
            String bday = profile.getNextBirthday();
            return bday != null ? bday : getString(R.string.birthday_no_data);
        }
    }

    private String capitalize (String str) {
        String[] strArray = str.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String s : strArray) {
            String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
            builder.append(cap).append(" ");
        }
        return builder.toString();
    }
}
