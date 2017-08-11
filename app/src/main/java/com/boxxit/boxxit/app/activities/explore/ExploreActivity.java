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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.boxxit.boxxit.R;
import com.boxxit.boxxit.app.activities.BaseActivity;
import com.boxxit.boxxit.app.activities.favourites.FavouritesActivity;
import com.boxxit.boxxit.aux.Logger;
import com.boxxit.boxxit.datastore.DataStore;
import com.boxxit.boxxit.library.parse.models.Product;
import com.boxxit.boxxit.library.parse.models.facebook.Profile;
import com.boxxit.boxxit.workers.ProductsWorker;
import com.boxxit.boxxit.workers.UserWorker;
import com.gabrielcoman.rxrecyclerview.RxAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import rx.Observable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subjects.PublishSubject;

public class ExploreActivity extends BaseActivity {

    private String facebookUser = "me";
    private int minPrice = 500;
    private int maxPrice = 5000;

    @BindView(R.id.ErrorView) RelativeLayout errorView;
    @BindView(R.id.Spinner) ProgressBar spinner;
    @BindView(R.id.ProductsRecyclerView) RecyclerView recyclerView;

    private RxAdapter adapter;

    private PublishSubject<Void> subject;

    @BindString(R.string.activity_explore_product_reason_you) String reasonYou;
    @BindString(R.string.activity_explore_product_reason_friend) String reasonFriend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);
        ButterKnife.bind(this);

        String fbUser = getStringExtrasDirect("profile");

        //
        // 1. have some events coming from the UI
        //      - get incoming string event
        //      - click ==> retry event
        //
        // 2. have a model to represent the
        //      - profile state  - HeaderUIModel
        //          - in progress, success, error
        //      - products state - ProductListUIModel
        //          - in progress, success, error

        UserWorker.getProfile(fbUser)
                .doOnSubscribe(() -> setState(ExploreState.initial))
                .map(ExploreState.update_profile::withProfile)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(Logger::logError)
                .onErrorResumeNext(throwable -> Single.just(ExploreState.update_profile.withProfile(new Profile())))
                .subscribe(this::setState);

        subject = PublishSubject.create();
        subject.asObservable()
                .doOnNext(aVoid -> setState(ExploreState.loading))
                .flatMap(aVoid -> ProductsWorker.getProductsForUser(fbUser, minPrice, maxPrice)
                        .map(ExploreState.update_products::withProducts)
                        .doOnError(Logger::logError)
                        .onErrorResumeNext(throwable -> Observable.just(ExploreState.error)))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(exploreState -> setState(ExploreState.not_loading))
                .subscribe(this::setState);

        subject.onNext(null);
    }

    public void backAction (View view) {
        this.finishOK();
    }

    public void gotoNextScreen (View view) {
        Intent intent = new Intent(this, FavouritesActivity.class);
        intent.putExtra("profile", facebookUser);
        startActivity(intent);
    }

    private void setState (ExploreState state) {
        switch (state) {
            case initial: {
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
                            productReason.setText(getString(this.facebookUser.equals(DataStore.getOwnId()) ?
                                            R.string.activity_explore_product_reason_you :
                                            R.string.activity_explore_product_reason_friend,
                                    product.categId));
                            likeProduct.setVisibility(this.facebookUser.equals(DataStore.getOwnId()) ? View.VISIBLE : View.GONE);
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
                        .didReachEnd(() -> subject.onNext(null));
                break;
            }
            case loading: {
                spinner.setVisibility(View.VISIBLE);
                errorView.setVisibility(View.GONE);
                break;
            }
            case not_loading: {
                spinner.setVisibility(View.GONE);
                break;
            }
            case error: {
                recyclerView.setVisibility(View.GONE);
                spinner.setVisibility(View.GONE);
                errorView.setVisibility(View.VISIBLE);

//                ButterKnife.bind(this, errorView);

                TextView errorTxt = (TextView) errorView.findViewById(R.id.ErrorText);
                errorTxt.setText(getString(R.string.activity_explore_error));

                Button retry = (Button) errorView.findViewById(R.id.RetryButton);
                retry.setOnClickListener(v -> {
                    Log.d("Boxxit", "Trying again!");
                    subject.onNext(null);
                });
                break;
            }
            case update_products: {
                spinner.setVisibility(View.GONE);
                errorView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.add(state.products);
                break;
            }
            case update_profile: {
                Profile profile = state.profile;
                ImageView profilePicture = (ImageView) findViewById(R.id.ProfilePicture);
                TextView profileName = (TextView) findViewById(R.id.ProfileName);
                TextView profileBirthday = (TextView) findViewById(R.id.ProfileBirthday);

                profileName.setText(profile.name);
                profileBirthday.setText(profile.birthday);

                Picasso.with(ExploreActivity.this)
                        .load(profile.picture.data.url)
                        .placeholder(R.drawable.ic_user_default)
                        .error(R.drawable.ic_user_default)
                        .transform(new CropCircleTransformation())
                        .into(profilePicture);
                break;
            }
        }
    }

    public enum ExploreState {
        initial,
        loading,
        not_loading,
        error,
        update_products,
        update_profile;

        private Profile profile;
        private List<Product> products = new ArrayList<>();

        public ExploreState withProfile(Profile profile) {
            this.profile = profile;
            return this;
        }

        public ExploreState withProducts(List<Product> products) {
            this.products = products;
            return this;
        }
    }
}
