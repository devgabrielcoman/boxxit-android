package com.boxxit.boxxit.app.activities.favourites;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.boxxit.boxxit.R;
import com.boxxit.boxxit.app.activities.BaseActivity;
import com.boxxit.boxxit.workers.UserWorker;
import com.squareup.picasso.Picasso;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class FavouritesActivity extends BaseActivity {

    private String facebookUser = "me";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);

        Log.d("Boxxit", "Favourites activity");

        getStringExtras("profile")
                .subscribe(userId -> {

                    //
                    // save reference
                    this.facebookUser = userId;

                    //
                    // perform state logic
                    getUserProfile(userId);

                }, throwable -> {
                    // weird error that should not be
                });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Business Logic
    ////////////////////////////////////////////////////////////////////////////////////////////////

    void getUserProfile (String userId) {

        UserWorker.getProfile(userId)
                .subscribe(profile -> {

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

                }, throwable -> {
                    // do nothing here
                });
    }

    public void backAction (View view) {
        this.finishOK();
    }
}
