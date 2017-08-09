package com.boxxit.boxxit.app.activities.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.boxxit.boxxit.R;
import com.boxxit.boxxit.app.activities.BaseActivity;
import com.boxxit.boxxit.app.activities.explore.ExploreActivity;
import com.boxxit.boxxit.datastore.DataStore;
import com.boxxit.boxxit.library.parse.models.Product;
import com.boxxit.boxxit.library.parse.models.facebook.FacebookData;
import com.boxxit.boxxit.library.parse.models.facebook.Profile;
import com.boxxit.boxxit.workers.UserWorker;
import com.gabrielcoman.rxrecyclerview.RxAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;

public class MainActivity extends BaseActivity {

    //
    // other vars
    private String facebookUser;
    private String offset;

    //
    // views
    private RecyclerView recyclerView;
    private RxAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UserWorker.getProfile(DataStore.getOwnId())
                .doOnSubscribe(this::setStateInitial)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(this::populateOwnProfileUI)
                .map(profile -> profile.id)
                .doOnSuccess(userId -> facebookUser = userId)
                .subscribe(s -> getAllEvents(facebookUser, offset), this::setStateError);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Business Logic
    ////////////////////////////////////////////////////////////////////////////////////////////////

    void populateOwnProfileUI (Profile profile) {
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
    }

    void getAllEvents (String userId, String off) {
        UserWorker.getEventsForUser(userId, off)
                .doOnSuccess(facebookData -> offset = facebookData.paging.offsetAfter())
                .flatMap(facebookData -> Single.just(facebookData.data))
                .toObservable()
                .reduce(new ArrayList<Profile>(), (profiles, profiles2) -> {
                    profiles.addAll(profiles2);
                    return profiles;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setStateSuccess, this::setStateError);

    }

    public void showMyGiftsAction (View view) {
        gotoNextScreen(facebookUser);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Routing Logic
    ////////////////////////////////////////////////////////////////////////////////////////////////

    void gotoNextScreen (String profile) {
        Intent intent = new Intent(this, ExploreActivity.class);
        intent.putExtra("profile", profile);
        startActivity(intent);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // State Logic
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void setStateInitial () {
        //
        // initialize recycler
        recyclerView = (RecyclerView) findViewById(R.id.EventsRecyclerView);
        adapter = RxAdapter.create()
                .bindTo(recyclerView)
                .setLayoutManger(new GridLayoutManager(getApplicationContext(), 2))
                .customizeRow(R.layout.row_event, Profile.class, (position, view, profile, total) -> {

                    ImageView profilePicture = (ImageView) view.findViewById(R.id.ProfilePicture);
                    TextView profileName = (TextView) view.findViewById(R.id.ProfileName);
                    TextView profileBirthday = (TextView) view.findViewById(R.id.ProfileBirthday);
                    View rightSeparator = (View) view.findViewById(R.id.RightSeparator);

                    rightSeparator.setVisibility(position % 2 == 0 ? View.VISIBLE : View.GONE);
                    profileName.setText(profile.name);
                    profileBirthday.setText(profile.birthday);

                    Picasso.with(MainActivity.this)
                            .load(profile.picture.data.url)
                            .placeholder(R.drawable.ic_user_default)
                            .error(R.drawable.ic_user_default)
                            .transform(new CropCircleTransformation())
                            .into(profilePicture);
                })
                .didClickOnRow(Profile.class, (integer, profile) -> gotoNextScreen(profile.id))
                .didReachEnd(() -> {
                    if (offset != null) {
                        getAllEvents(facebookUser, offset);
                    }
                });
    }

    private void setStateSuccess(List<Profile> events) {
        adapter.update(events);
    }

    // TODO: 07/08/2017 Handle error case
    private void setStateError (Throwable throwable) {
        // do nothing
    }
}
