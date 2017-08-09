package com.boxxit.boxxit.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import rx.Single;
import rx.functions.Action0;
import rx.functions.Action3;

public class BaseActivity extends Activity {

    private Action0 onActivityResult = null;
    private Action3<Integer, Integer, Intent> onActivityResultWithParams = null;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (onActivityResult != null && resultCode == RESULT_OK) {
            onActivityResult.call();
        }

        if (onActivityResultWithParams != null) {
            onActivityResultWithParams.call(requestCode, resultCode, data);
        }
    }

    public void setOnActivityResult(Action0 onActivityResult) {
        this.onActivityResult = onActivityResult;
    }

    public void setOnActivityResult(Action3<Integer, Integer, Intent> onActivityResult) {
        this.onActivityResultWithParams = onActivityResult;
    }

    public void finishOK () {
        setResult(RESULT_OK);
        finish();
    }

    public String getStringExtrasDirect(String key) {
        Bundle bundle = BaseActivity.this.getIntent().getExtras();
        return bundle != null ? bundle.getString(key) : null;
    }

    public Single<String> getStringExtras (String key) {
        return Single.create(singleSubscriber -> {

            Bundle bundle = BaseActivity.this.getIntent().getExtras();
            if (bundle != null) {
                String extra = bundle.getString(key);
                if (extra != null) {
                    singleSubscriber.onSuccess(extra);
                }
            }

        });
    }

    public Single<Boolean> getBooleanExtras (String key) {
        return Single.create(singleSubscriber -> {

            Bundle bundle = BaseActivity.this.getIntent().getExtras();
            if (bundle != null) {
                boolean extra = bundle.getBoolean(key);
                singleSubscriber.onSuccess(extra);
            }

        });
    }
}
