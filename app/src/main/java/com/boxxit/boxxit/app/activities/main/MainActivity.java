package com.boxxit.boxxit.app.activities.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.boxxit.boxxit.R;
import com.boxxit.boxxit.app.activities.BaseActivity;
import com.boxxit.boxxit.app.activities.explore.ExploreActivity;
import com.boxxit.boxxit.app.activities.favourites.FavouritesActivity;
import com.boxxit.boxxit.library.parse.models.Product;
import com.boxxit.boxxit.library.parse.models.facebook.FacebookData;
import com.boxxit.boxxit.library.parse.models.facebook.Profile;
import com.boxxit.boxxit.workers.UserWorker;
import com.gabrielcoman.rxrecyclerview.RxAdapter;
import com.squareup.picasso.Picasso;

import java.util.List;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Action2;
import rx.functions.Func1;

public class MainActivity extends BaseActivity {

    private RxAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("Boxxit", "Main activity");

        setStateInitial();
        getUserProfile();
        getAllEvents();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Business Logic
    ////////////////////////////////////////////////////////////////////////////////////////////////

    void getUserProfile () {

        UserWorker.getProfile("me")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(profile -> {

                    ImageView profilePicture = (ImageView) findViewById(R.id.ProfilePicture);
                    TextView profileName = (TextView) findViewById(R.id.ProfileName);
                    TextView profileBirthday = (TextView) findViewById(R.id.ProfileBirthday);

                    profileName.setText(profile.name);
                    profileBirthday.setText(profile.birthday);

                    Picasso.with(MainActivity.this)
                            .load(profile.picture.data.url)
                            .placeholder(R.drawable.ic_user_default)
                            .error(R.drawable.ic_user_default)
                            .transform(new CropCircleTransformation())
                            .into(profilePicture);

                }, throwable -> {
                    // do nothing here
                });

    }

    void getAllEvents () {

        UserWorker.getEventsForUser("me", null)
                .flatMap(facebookData -> Single.just(facebookData.data))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setStateSuccess, throwable -> Log.d("Boxxit", "Error is " + throwable.getMessage()));

    }

    void gotoProfile (String profile) {
        Intent intent = new Intent(this, ExploreActivity.class);
        intent.putExtra("profile", profile);
        startActivity(intent);
    }

    //
    // action
    public void showMyGiftsAction (View view) {
        gotoProfile("me");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // State Logic
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void setStateInitial () {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.EventsRecyclerView);
        adapter = RxAdapter.create()
                .bindTo(recyclerView)
                .setLayoutManger(new GridLayoutManager(getApplicationContext(), 2))
                .customizeRow(R.layout.row_event, Profile.class, (position, view, profile, total) -> {

                    ImageView profilePicture = (ImageView) view.findViewById(R.id.ProfilePicture);
                    TextView profileName = (TextView) view.findViewById(R.id.ProfileName);
                    TextView profileBirthday = (TextView) view.findViewById(R.id.ProfileBirthday);

                    profileName.setText(profile.name);
                    profileBirthday.setText(profile.birthday);

                    Picasso.with(MainActivity.this)
                            .load(profile.picture.data.url)
                            .placeholder(R.drawable.ic_user_default)
                            .error(R.drawable.ic_user_default)
                            .transform(new CropCircleTransformation())
                            .into(profilePicture);
                })
                .didClickOnRow(Profile.class, (integer, profile) -> {
                    Intent intent = new Intent(this, ExploreActivity.class);
                    intent.putExtra("profile", profile.id);
                    startActivity(intent);
                });
    }

    private void setStateSuccess(List<Profile> products) {
        adapter.update(products);
    }
}
