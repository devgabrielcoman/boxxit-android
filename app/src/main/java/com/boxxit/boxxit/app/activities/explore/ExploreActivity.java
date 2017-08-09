package com.boxxit.boxxit.app.activities.explore;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
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
import com.boxxit.boxxit.datastore.DataStore;
import com.boxxit.boxxit.library.parse.models.Product;
import com.boxxit.boxxit.library.parse.models.facebook.Profile;
import com.boxxit.boxxit.workers.ProductsWorker;
import com.boxxit.boxxit.workers.UserWorker;
import com.gabrielcoman.rxrecyclerview.RxAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;

public class ExploreActivity extends BaseActivity {

    private String facebookUser = "me";
    private int minPrice = 500;
    private int maxPrice = 5000;

    private RelativeLayout errorView;
    private RecyclerView recyclerView;
    private RxAdapter adapter;
    private ProgressBar spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        getStringExtras("profile")
                .doOnSubscribe(this::setStateInitial)
                .flatMap(UserWorker::getProfile)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(this::populateProfileUI)
                .toObservable()
                .map(profile -> profile.id)
                .doOnError(throwable -> Log.e("Boxxit", "Explore Activity: " + throwable.getMessage()))
                .doOnNext(userId -> facebookUser = userId)
                .subscribe(userId -> getUserProducts(facebookUser, minPrice, maxPrice), this::setStateError);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Business Logic
    ////////////////////////////////////////////////////////////////////////////////////////////////

    void populateProfileUI (Profile profile) {
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
    }

    void getUserProducts (String userId, int min, int max) {

        ProductsWorker.getProductsForUser(userId, min, max)
                .doOnSubscribe(this::setStateLoading)
                .toList()
                .reduce(new ArrayList<Product>(), (products1, products2) -> {
                    products1.addAll(products2);
                    return products1;
                })
                .doOnError(throwable -> Log.e("Boxxit", "Favourites Activity: " + throwable.getMessage()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::populateProductsUI, this::setStateError);

    }

    void populateProductsUI (List<Product> products) {
        if (products.size() == 0) {
            setStateError(null);
        } else {
            setStateSuccess(products);
        }
    }

    public void backAction (View view) {
        this.finishOK();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Routing Logic
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void gotoNextScreen (View view) {
        Intent intent = new Intent(this, FavouritesActivity.class);
        intent.putExtra("profile", facebookUser);
        startActivity(intent);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // State Logic
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void setStateInitial () {

        //
        // set the error & spinner view
        errorView = (RelativeLayout) findViewById(R.id.ErrorView);
        spinner = (ProgressBar) findViewById(R.id.Spinner);

        //
        // set the recycler
        recyclerView = (RecyclerView) findViewById(R.id.ProductsRecyclerView);
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
                .didReachEnd(() -> getUserProducts(facebookUser, minPrice, maxPrice));
    }

    private void setStateLoading () {
        spinner.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
    }

    private void setStateSuccess (List<Product> products) {
        spinner.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        adapter.update(products);
    }

    private void setStateError (Throwable throwable) {
        recyclerView.setVisibility(View.GONE);
        spinner.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);

        TextView errorTxt = (TextView) errorView.findViewById(R.id.ErrorText);
        errorTxt.setText(getString(R.string.activity_explore_error));

        Button retry = (Button) errorView.findViewById(R.id.RetryButton);
        retry.setOnClickListener(v -> getUserProducts(facebookUser, minPrice, maxPrice));
    }
}
