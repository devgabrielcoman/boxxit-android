package com.boxxit.boxxit.datastore;

import com.boxxit.boxxit.library.parse.models.facebook.Profile;

import java.util.HashMap;
import java.util.Map;

public class DataStore {

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
        return com.facebook.Profile.getCurrentProfile().getId();
    }
}
