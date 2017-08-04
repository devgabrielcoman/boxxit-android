package com.boxxit.boxxit.library.parse;

import android.util.Log;

import com.boxxit.boxxit.library.base.Task;
import com.boxxit.boxxit.library.parse.models.facebook.FacebookData;
import com.boxxit.boxxit.library.parse.models.facebook.Profile;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import rx.Single;

public class ParseFacebookDataTask implements Task <String, FacebookData<Profile>, Single<FacebookData<Profile>>> {

    @Override
    public Single<FacebookData<Profile>> execute(String input) {

        return Single.create(subscriber -> {

            try {
                Gson gson = new Gson();
                FacebookData<Profile> data = gson.fromJson(input, FacebookData.class);
                subscriber.onSuccess(data);
            } catch (JsonParseException e) {
                subscriber.onError(e);
            }
        });
    }
}
