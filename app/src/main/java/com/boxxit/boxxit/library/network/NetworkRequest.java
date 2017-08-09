package com.boxxit.boxxit.library.network;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.facebook.AccessToken;
import com.google.common.collect.ImmutableMap;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class NetworkRequest implements RequestOptions {

    private NetworkRequest() {}

    private static String token () {
        try {
            return AccessToken.getCurrentAccessToken().getToken();
        } catch (Exception e) {
            return null;
        }
    }

    @NonNull
    @Override
    public String getQuery() {
        List<String> items = new ArrayList<>();
        for (String key : getParams().keySet()) {
            try {
                items.add(key + "=" + URLEncoder.encode(getParams().get(key).toString(), "utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return TextUtils.join("&", items);
    }

    @NonNull
    @Override
    public String getUrl() {
        return getProtocol() + getDomain() + getPath() + "?" + getQuery();
    }

    public static NetworkRequest getProfileFromFacebook(String user) {
        return new NetworkRequest() {
            @NonNull
            @Override
            public String getProtocol() {
                return "https://";
            }

            @NonNull
            @Override
            public String getDomain() {
                return "graph.facebook.com/v2.8";
            }

            @NonNull
            @Override
            public String getPath() {
                return "/" + user;
            }

            @NonNull
            @Override
            public Map<String, Object> getParams() {
                return ImmutableMap.of(
                        "fields",
                        "email,gender,name,picture.width(300),first_name,last_name,birthday,friends{id}", "access_token", token()
                );
            }
        };
    }

    public static NetworkRequest getFriendsFromFacebook(String user, String offset) {
        return new NetworkRequest() {
            @NonNull
            @Override
            public String getProtocol() {
                return "https://";
            }

            @NonNull
            @Override
            public String getDomain() {
                return "graph.facebook.com/v2.8";
            }

            @NonNull
            @Override
            public String getPath() {
                return "/" + user + "/friends";
            }

            @NonNull
            @Override
            public Map<String, Object> getParams() {
                return ImmutableMap.of(
                        "fields", "id,email,picture.width(300),name,birthday,cover",
                        "access_token", token(),
                        "limit", 0,
                        "after", offset != null ? offset : "",
                        "summary", true
                );
            }
        };
    }

    public static NetworkRequest populateUserProfile(String token) {
        return new NetworkRequest() {
            @NonNull
            @Override
            public String getProtocol() {
                return "https://";
            }

            @NonNull
            @Override
            public String getDomain() {
                return "boxxit-3231.nodechef.com";
            }

            @NonNull
            @Override
            public String getPath() {
                return "/populateUserProfile";
            }

            @NonNull
            @Override
            public Map<String, Object> getParams() {
                return ImmutableMap.of("fbToken", token);
            }
        };
    }

    public static NetworkRequest saveNotificationToken(String id, String token) {
        return new NetworkRequest() {
            @NonNull
            @Override
            public String getProtocol() {
                return "https://";
            }

            @NonNull
            @Override
            public String getDomain() {
                return "boxxit-3231.nodechef.com";
            }

            @NonNull
            @Override
            public String getPath() {
                return "/saveToken";
            }

            @NonNull
            @Override
            public Map<String, Object> getParams() {
                return ImmutableMap.of("fbId", id, "token", token);
            }
        };
    }

    public static NetworkRequest saveProduct(String id, String asin) {
        return new NetworkRequest() {
            @NonNull
            @Override
            public String getProtocol() {
                return "https://";
            }

            @NonNull
            @Override
            public String getDomain() {
                return "boxxit-3231.nodechef.com";
            }

            @NonNull
            @Override
            public String getPath() {
                return "/saveProduct";
            }

            @NonNull
            @Override
            public Map<String, Object> getParams() {
                return ImmutableMap.of("fbId", id, "asin", asin);
            }
        };
    }

    public static NetworkRequest deleteProduct(String id, String asin) {
        return new NetworkRequest() {
            @NonNull
            @Override
            public String getProtocol() {
                return "https://";
            }

            @NonNull
            @Override
            public String getDomain() {
                return "boxxit-3231.nodechef.com";
            }

            @NonNull
            @Override
            public String getPath() {
                return "/deleteProduct";
            }

            @NonNull
            @Override
            public Map<String, Object> getParams() {
                return ImmutableMap.of("fbId", id, "asin", asin);
            }
        };
    }

    public static NetworkRequest getProductsForUser (String id, int min, int max) {
        return new NetworkRequest() {
            @NonNull
            @Override
            public String getProtocol() {
                return "https://";
            }

            @NonNull
            @Override
            public String getDomain() {
                return "boxxit-3231.nodechef.com";
            }

            @NonNull
            @Override
            public String getPath() {
                return "/getProductsForUser";
            }

            @NonNull
            @Override
            public Map<String, Object> getParams() {
                return ImmutableMap.of("fbId", id, "min", min, "max", max);
            }
        };
    }

    public static NetworkRequest getFavouriteProductsForUser (String id) {
        return new NetworkRequest() {
            @NonNull
            @Override
            public String getProtocol() {
                return "https://";
            }

            @NonNull
            @Override
            public String getDomain() {
                return "boxxit-3231.nodechef.com";
            }

            @NonNull
            @Override
            public String getPath() {
                return "/getFavouriteProductsForUser";
            }

            @NonNull
            @Override
            public Map<String, Object> getParams() {
                return ImmutableMap.of("fbId", id);
            }
        };
    }
}
