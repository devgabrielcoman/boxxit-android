package com.boxxit.boxxit.library.network;

import android.support.annotation.NonNull;

import java.util.Map;

public interface RequestOptions {
    @NonNull String getProtocol();
    @NonNull String getDomain();
    @NonNull String getPath();
    @NonNull Map<String, Object> getParams();
    @NonNull String getQuery();
    @NonNull String getUrl();
}