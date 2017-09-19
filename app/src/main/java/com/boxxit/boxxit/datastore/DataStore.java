package com.boxxit.boxxit.datastore;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.boxxit.boxxit.app.events.BoolEvent;
import com.boxxit.boxxit.library.parse.models.facebook.Profile;
import com.facebook.AccessToken;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.Single;

public class DataStore {

    private static final String PREF_FILE = "Boxxit_Preferences";
    private static final String TUTORIAL_FIRST_DONE_KEY = "First_Tutorial_Done";
    private static final String TUTORIAL_SECOND_DONE_KEY = "Second_Tutorial_Done";
    private static final String TUTORIAL_THIRD_DONE_KEY = "Third_Tutorial_Done";

    private static final DataStore instance = new DataStore();

    private Map<String, Profile> profiles;

    private DataStore() {
        profiles = new HashMap<>();
    }

    public static DataStore shared() {
        return instance;
    }

    public void setProfile (Profile user, String id) {
        profiles.put(id, user);
    }

    public Profile getProfile(String id) {
        return profiles.get(id);
    }

    public static String getOwnId () {

        com.facebook.Profile profile = com.facebook.Profile.getCurrentProfile();
        com.facebook.AccessToken token = AccessToken.getCurrentAccessToken();

        if (profile != null) {
            return profile.getId();
        } else if (token != null) {
            return token.getUserId();
        } else {
            return null;
        }
    }

    public void setFirstTutorialSeen (Context context) {
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(TUTORIAL_FIRST_DONE_KEY, true)
                .apply();
    }

    public Observable<Boolean> getFirstTutorialSeen (Context context) {
        boolean value = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
                .getBoolean(TUTORIAL_FIRST_DONE_KEY, false);
        return Observable.just(value);
    }

    public void setSecondTutorialSeen (Context context) {
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(TUTORIAL_SECOND_DONE_KEY, true)
                .apply();
    }

    public Observable<Boolean> getSecondTutorialSeen (Context context) {
        boolean value = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
                .getBoolean(TUTORIAL_SECOND_DONE_KEY, false);
        return Observable.just(value);
    }

    public void setThirdTutorialSeen (Context context) {
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(TUTORIAL_THIRD_DONE_KEY, true)
                .apply();
    }

    public Observable<Boolean> getThirdTutorialSeen (Context context) {
        boolean value = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
                .getBoolean(TUTORIAL_THIRD_DONE_KEY, false);
        return Observable.just(value);
    }
}
