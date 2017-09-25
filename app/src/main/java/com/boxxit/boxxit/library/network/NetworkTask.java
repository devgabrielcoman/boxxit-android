package com.boxxit.boxxit.library.network;

import android.util.Log;

import com.boxxit.boxxit.library.base.Task;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import rx.Single;

public class NetworkTask implements Task <NetworkRequest, String, Single<String>> {

    @Override
    public Single<String> execute(NetworkRequest input) {

        OkHttpClient client = new OkHttpClient();
        String endpoint = input.getUrl();
        okhttp3.Request request = new okhttp3.Request.Builder().url(endpoint).build();

        Log.d("Boxxit", "Calling: " + endpoint);

        return Single.create(subscriber -> {

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    subscriber.onError(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        subscriber.onSuccess(response.body().string());
                    } catch (Exception e) {
                        subscriber.onError(e);
                    } finally {
                        response.close();
                    }
                }
            });
        });
    }
}
