package com.boxxit.boxxit.app.activities.explore;

import com.boxxit.boxxit.library.parse.models.Product;
import com.boxxit.boxxit.library.parse.models.facebook.Profile;

import java.util.ArrayList;
import java.util.List;

public enum ExploreUIState {
    INITIAL,
    PROFILE_SUCCESS,
    PROFILE_ERROR,
    PRODUCTS_LOADING,
    PRODUCTS_SUCCESS,
    PRODUCTS_ERROR,
    GO_BACK,
    GOTO_FAVOURITES,
    PRESENT_TUTORIAL2;

    Profile profile;
    List<Product> products = new ArrayList<>();
    Throwable throwable;
    int backResult;

    public static ExploreUIState PROFILE_SUCCESS (Profile profile) {
        ExploreUIState result = PROFILE_SUCCESS;
        result.profile = profile;
        return result;
    }

    public static ExploreUIState PROFILE_ERROR (Throwable throwable) {
        ExploreUIState result = PROFILE_ERROR;
        result.throwable = throwable;
        return result;
    }

    public static ExploreUIState PRODUCTS_SUCCESS (List<Product> products) {
        ExploreUIState result = PRODUCTS_SUCCESS;
        result.products = products;
        return result;
    }

    public static ExploreUIState PRODUCTS_ERROR (Throwable throwable) {
        ExploreUIState result = PRODUCTS_ERROR;
        result.throwable = throwable;
        return result;
    }

    public static ExploreUIState GO_BACK (int backResult) {
        ExploreUIState result = GO_BACK;
        result.backResult = backResult;
        return result;
    }
}
