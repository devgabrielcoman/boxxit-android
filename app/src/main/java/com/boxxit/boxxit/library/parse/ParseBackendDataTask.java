package com.boxxit.boxxit.library.parse;

import android.util.Log;

import com.boxxit.boxxit.library.base.Task;
import com.boxxit.boxxit.library.parse.models.BackendData;
import com.boxxit.boxxit.library.parse.models.Product;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import rx.Single;

public class ParseBackendDataTask implements Task <String, BackendData, Single<BackendData>> {

    @Override
    public Single<BackendData> execute(String input) {

        return Single.create(subscriber -> {

            try {
                Gson gson = new Gson();
                BackendData data = gson.fromJson(input, BackendData.class);
                subscriber.onSuccess(data);
            } catch (JsonParseException e) {
                subscriber.onError(e);
            }
        });
    }
}
