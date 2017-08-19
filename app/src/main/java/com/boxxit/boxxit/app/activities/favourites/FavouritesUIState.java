package com.boxxit.boxxit.app.activities.favourites;

import com.boxxit.boxxit.library.parse.models.Product;
import com.boxxit.boxxit.library.parse.models.facebook.Profile;

import java.util.ArrayList;
import java.util.List;

public enum FavouritesUIState {
    INITIAL,
    PROFILE_SUCCESS,
    PROFILE_ERROR,
    PRODUCTS_LOADING,
    PRODUCTS_SUCCESS,
    PRODUCTS_EMPTY,
    PRODUCTS_ERROR,
    GO_BACK;

    Profile profile;
    List<Product> products = new ArrayList<>();
    Throwable throwable;

    public static FavouritesUIState PROFILE_SUCCESS (Profile profile) {
        FavouritesUIState result = PROFILE_SUCCESS;
        result.profile = profile;
        return result;
    }

    public static FavouritesUIState PROFILE_ERROR (Throwable throwable) {
        FavouritesUIState result = PROFILE_ERROR;
        result.throwable = throwable;
        return result;
    }

    public static FavouritesUIState PRODUCTS_SUCCESS (List<Product> products) {
        FavouritesUIState result = PRODUCTS_SUCCESS;
        result.products = products;
        return result;
    }

    public static FavouritesUIState PRODUCTS_ERROR (Throwable throwable) {
        FavouritesUIState result = PRODUCTS_ERROR;
        result.throwable = throwable;
        return result;
    }
}
