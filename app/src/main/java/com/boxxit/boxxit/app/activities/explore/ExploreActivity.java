package com.boxxit.boxxit.app.activities.explore;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.VolumeProviderCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.boxxit.boxxit.R;
import com.boxxit.boxxit.app.activities.BaseActivity;
import com.boxxit.boxxit.app.activities.favourites.FavouritesActivity;
import com.boxxit.boxxit.datastore.DataStore;
import com.boxxit.boxxit.library.parse.models.Product;
import com.boxxit.boxxit.library.parse.models.facebook.Profile;
import com.boxxit.boxxit.workers.ProductsWorker;
import com.boxxit.boxxit.workers.UserWorker;
import com.gabrielcoman.rxrecyclerview.RxAdapter;
import com.jakewharton.rxbinding.view.RxView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import rx.Observable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Action4;
import rx.functions.Func1;

public class ExploreActivity extends BaseActivity {

    private String facebookUser = "me";
    private int minPrice = 500;
    private int maxPrice = 5000;

    private RxAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        Log.d("Boxxit", "Explore activity");

        setStateInitial();
        getStringExtras("profile")
                .doOnSuccess(userId -> facebookUser = userId)
                .subscribe(s -> {
                    populateProfileUI(facebookUser);
                    getUserProducts(facebookUser, minPrice, maxPrice);
                }, throwable -> {
                    Log.d("Boxxit", "Error here: " + throwable.getMessage());
                });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Business Logic
    ////////////////////////////////////////////////////////////////////////////////////////////////

    void populateProfileUI (String userId) {

        UserWorker.getProfile(userId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(profile -> {

                    Log.d("Boxxit", "Profile: " + profile.name + " / " + profile.birthday + " / " + profile.picture.data.url);

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

                }, throwable -> {
                    // error
                });
    }

    void getUserProducts (String userId, int min, int max) {

        UserWorker.getProfile(userId)
                .toObservable()
                .flatMap(profile -> ProductsWorker.getProductsForUser(profile.id, min, max))
                .toList()
                .reduce(new ArrayList<Product>(), (products1, products2) -> {
                    products1.addAll(products2);
                    return products1;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(products -> {
                    if (products.size() == 0) {
                        // still nothing, some type of error here!
                    } else {
                        setStateSuccess(products);
                    }
                }, throwable -> {
                    Log.d("Boxxit", "Error is " + throwable.getMessage());
                }, () -> {
                    Log.d("Boxxit", "Completed loading!");
                });

    }

    public void showFavouritesAction (View view) {
        Intent intent = new Intent(this, FavouritesActivity.class);
        intent.putExtra("profile", facebookUser);
        startActivity(intent);
    }

    public void backAction (View view) {
        this.finishOK();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // State Logic
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void setStateInitial () {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.ProductsRecyclerView);
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
                    productReason.setText(getString(this.facebookUser.equals("me") ?
                                    R.string.activity_explore_product_reason_you :
                                    R.string.activity_explore_product_reason_friend,
                            product.categId));
                    likeProduct.setVisibility(this.facebookUser.equals("me") ? View.VISIBLE : View.GONE);
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
                    Profile user = DataStore.shared().getProfile(facebookUser);

                    likeProduct.setOnClickListener(v -> {

                        Single<Void> rxOperation = product.isFavourite ?
                                ProductsWorker.deleteFavouriteProduct(product.asin, user.id):
                                ProductsWorker.saveFavouriteProduct(product.asin, user.id);

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
                .didReachEnd(() -> getUserProducts(facebookUser, minPrice, maxPrice));
    }

    private void setStateSuccess(List<Product> products) {
        adapter.update(products);
    }
}
