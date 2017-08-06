package com.boxxit.boxxit.library.auth;

import android.app.Activity;
import android.content.Intent;

import com.boxxit.boxxit.library.base.Task;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import rx.Observable;
import rx.Single;

public class FacebookAuthTask implements Task <FacebookAuthRequest, String, Single<String>> {

    private Activity activity;
    private CallbackManager manager;

    public FacebookAuthTask(Activity context) {
        manager = CallbackManager.Factory.create();
        activity = context;
    }

    @Override
    public Single<String> execute(FacebookAuthRequest input) {

        return Single.create(subscriber -> {

            LoginManager.getInstance().registerCallback(manager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {

                    AccessToken accessToken = AccessToken.getCurrentAccessToken();
                    if (accessToken != null && accessToken.getToken() != null) {
                        subscriber.onSuccess(accessToken.getToken());
                    } else {
                        subscriber.onError(new Throwable("Could not get access token!"));
                    }
                }

                @Override
                public void onCancel() {
                    subscriber.onError(null);
                }

                @Override
                public void onError(FacebookException error) {
                    subscriber.onError(error);
                }
            });

            //
            // start login process
            LoginManager.getInstance().logInWithReadPermissions(activity, input.permissions);

        });
    }

    public void callbackResult(int requestCode, int resultCode, Intent data) {
        manager.onActivityResult(requestCode, resultCode, data);
    }
}
