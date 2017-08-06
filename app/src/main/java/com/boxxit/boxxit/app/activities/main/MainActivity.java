package com.boxxit.boxxit.app.activities.main;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.boxxit.boxxit.R;
import com.boxxit.boxxit.app.activities.BaseActivity;
import com.boxxit.boxxit.workers.UserWorker;
import com.squareup.picasso.Picasso;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("Boxxit", "Main activity");

        //
        // customise user profile
        getUserProfile();

        // and all events
        getAllEvents();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Business Logic
    ////////////////////////////////////////////////////////////////////////////////////////////////

    void getUserProfile () {

        UserWorker.getProfile("me")
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

    }
}
