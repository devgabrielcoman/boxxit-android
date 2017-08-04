package com.boxxit.boxxit.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.boxxit.boxxit.R;
import com.boxxit.boxxit.library.network.NetworkRequest;
import com.boxxit.boxxit.library.network.NetworkTask;
import com.boxxit.boxxit.library.parse.ParseBackendDataTask;
import com.boxxit.boxxit.library.parse.ParseFacebookDataTask;
import com.boxxit.boxxit.library.parse.ParseFacebookProfileTask;
import com.boxxit.boxxit.library.parse.models.BackendData;
import com.boxxit.boxxit.library.parse.models.Product;
import com.boxxit.boxxit.library.parse.models.facebook.FacebookData;
import com.boxxit.boxxit.library.parse.models.facebook.Profile;

import rx.Single;
import rx.functions.Action1;
import rx.functions.Func1;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NetworkRequest request = NetworkRequest.getProfileFromFacebook("me"/*"10203560643491925"*/);
        NetworkTask task = new NetworkTask();
        task.execute(request)
                .flatMap(s -> new ParseFacebookProfileTask().execute(s))
                .subscribe(s -> {
                    Log.d("Boxxit", "Profile: \nId: " + s.id + "\nName: " + s.name + "\nGender: " + s.gender + "\nPicture: " + s.picture + "\nFriends: " + s.friends.data.size());
                }, throwable -> {
                    Log.d("Boxxit", "error 1: " + throwable.getMessage());
                });

        NetworkRequest request1 = NetworkRequest.getFriendsFromFacebook("me"/*"10203560643491925"*/, null);
        task.execute(request1)
                .flatMap(s -> new ParseFacebookDataTask().execute(s))
                .subscribe(profileFacebookData -> {
                    Log.d("Boxxit", "Fb Friends: " + profileFacebookData.data.size());
                }, throwable -> {
                    Log.d("Boxxit", "error 2: " + throwable.getMessage());
                });

        NetworkRequest request2 = NetworkRequest.getProductsForUser("me", 500, 5000);
        task.execute(request2)
                .flatMap(s -> new ParseBackendDataTask().execute(s))
                .subscribe(productBackendData -> {
                    Log.d("Boxxit", "Nr products: " + productBackendData.count + "\nActual: " + productBackendData.data.size());
                }, throwable -> {
                    Log.d("Boxxit", "error 3: " + throwable.getMessage());
                });
    }
}
