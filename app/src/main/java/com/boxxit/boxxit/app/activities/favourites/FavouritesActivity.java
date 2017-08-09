package com.boxxit.boxxit.app.activities.favourites;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.boxxit.boxxit.R;
import com.boxxit.boxxit.app.activities.BaseActivity;
import com.boxxit.boxxit.library.parse.models.Product;
import com.boxxit.boxxit.library.parse.models.facebook.Profile;
import com.boxxit.boxxit.workers.ProductsWorker;
import com.boxxit.boxxit.workers.UserWorker;
import com.gabrielcoman.rxrecyclerview.RxAdapter;
import com.squareup.picasso.Picasso;

import java.util.List;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import rx.android.schedulers.AndroidSchedulers;

public class FavouritesActivity extends BaseActivity {

    private RelativeLayout errorView;
    private RecyclerView recyclerView;
    private RxAdapter adapter;
    private ProgressBar spinner;

    private String facebookUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);

        getStringExtras("profile")
                .doOnSubscribe(this::setStateInitial)
                .flatMap(UserWorker::getProfile)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(this::populateProfileUI)
                .toObservable()
                .map(profile -> profile.id)
                .doOnNext(userId -> facebookUser = userId)
                .doOnError(throwable -> Log.e("Boxxit", "Favourites Activity: " + throwable.getMessage()))
                .subscribe(this::getUserFavouriteProducts, this::setStateError);
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

        Picasso.with(FavouritesActivity.this)
                .load(profile.picture.data.url)
                .placeholder(R.drawable.ic_user_default)
                .error(R.drawable.ic_user_default)
                .transform(new CropCircleTransformation())
                .into(profilePicture);
    }

    void getUserFavouriteProducts (String userId) {
        ProductsWorker.getFavouriteProductsForUser(userId)
                .doOnSubscribe(this::setStateLoading)
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(throwable -> Log.e("Boxxit", "Favourites Activity: " + throwable.getMessage()))
                .subscribe(this::populateProductsUI, this::setStateError);
    }

    void populateProductsUI (List<Product> products) {
        if (products.size() == 0) {
            setStateEmpty();
        } else {
            setStateSuccess(products);
        }
    }

    public void backAction (View view) {
        this.finishOK();
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
        recyclerView = (RecyclerView) findViewById(R.id.FavouritesRecyclerView);
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

    private void setStateLoading () {
        spinner.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
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
        errorTxt.setText(getString(R.string.activity_favourites_error));

        Button retry = (Button) errorView.findViewById(R.id.RetryButton);
        retry.setOnClickListener(v -> getUserFavouriteProducts(facebookUser));
    }

    private void setStateEmpty () {
        recyclerView.setVisibility(View.GONE);
        spinner.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);

        Button retry = (Button) errorView.findViewById(R.id.RetryButton);
        retry.setVisibility(View.GONE);
        TextView errorTxt = (TextView) errorView.findViewById(R.id.ErrorText);
        errorTxt.setText(getString(R.string.activity_favourites_no_products));
    }
}
