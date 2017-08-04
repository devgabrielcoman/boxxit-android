package com.boxxit.boxxit.library.parse;

import android.util.Log;

import com.boxxit.boxxit.library.base.Task;
import com.boxxit.boxxit.library.parse.models.facebook.Profile;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import rx.Single;

public class ParseFacebookProfileTask implements Task<String, Profile, Single<Profile>> {

    @Override
    public Single<Profile> execute(String input) {

        return Single.create(subscriber -> {

            try {
                Gson gson = new Gson();
                Profile profile = gson.fromJson(input, Profile.class);
                subscriber.onSuccess(profile);
            } catch (JsonParseException e) {
                subscriber.onError(e);
            }
        });
    }
}
