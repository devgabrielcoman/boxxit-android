package com.boxxit.boxxit.workers;

import android.app.Activity;

import com.boxxit.boxxit.datastore.DataStore;
import com.boxxit.boxxit.library.auth.FacebookAuthRequest;
import com.boxxit.boxxit.library.auth.FacebookAuthTask;
import com.boxxit.boxxit.library.network.NetworkRequest;
import com.boxxit.boxxit.library.network.NetworkTask;
import com.boxxit.boxxit.library.parse.ParseFacebookDataTask;
import com.boxxit.boxxit.library.parse.ParseFacebookProfileTask;
import com.boxxit.boxxit.library.parse.models.facebook.FacebookData;
import com.boxxit.boxxit.library.parse.models.facebook.Profile;
import com.facebook.AccessToken;

import rx.Observable;
import rx.Single;

public class UserWorker {

    public static boolean isUserLoggedIn () {
        return AccessToken.getCurrentAccessToken() != null;
    }

    public static Single<Profile> getProfile(String id) {

        Profile current = DataStore.shared().getProfile(id);

        if (current != null) {
            return Single.just(current);
        }
        else {
            NetworkRequest request = NetworkRequest.getProfileFromFacebook(id);
            NetworkTask task = new NetworkTask();
            return task.execute(request)
                    .flatMap(s -> new ParseFacebookProfileTask().execute(s))
                    .doOnSuccess(profile -> DataStore.shared().setProfile(profile, id));
        }
    }

    public static Single<Void> populateUserProfile(String token) {
        NetworkRequest request = NetworkRequest.populateUserProfile(token);
        NetworkTask task = new NetworkTask();
        return task.execute(request).map(s -> null);
    }

    public static Single<Void> updateNotificationToken(String token, String id) {
        NetworkRequest request = NetworkRequest.saveNotificationToken(id, token);
        NetworkTask task = new NetworkTask();
        return task.execute(request).map(s -> null);
    }

    public static Single<FacebookData<Profile>> getEventsForUser(String id, String offset) {
        NetworkRequest request = NetworkRequest.getFriendsFromFacebook(id, offset);
        NetworkTask task = new NetworkTask();
        return task.execute(request)
                .flatMap(s -> new ParseFacebookDataTask().execute(s));
    }

    // TODO: 04/08/2017 Fill this method
    public static Single<Void> inviteUser () {
        return Single.just(null);
    }

}
