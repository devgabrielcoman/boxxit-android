package com.boxxit.boxxit.app.activities.favourites;

import com.boxxit.boxxit.library.parse.models.Product;
import com.boxxit.boxxit.library.parse.models.facebook.Profile;

import java.util.ArrayList;
import java.util.List;

public class FavouritesUIState {
    Profile profile;
    List<Product> products;
    Throwable error;
    boolean isLoading;
    boolean profileSuccess;
    boolean productSuccess;
    boolean productEmpty;
    boolean goBack;

    public FavouritesUIState(Profile profile, List<Product> products, Throwable error, boolean isLoading, boolean profileSuccess, boolean productSuccess, boolean productEmpty, boolean goBack) {
        this.profile = profile;
        this.products = products;
        this.error = error;
        this.isLoading = isLoading;
        this.profileSuccess = profileSuccess;
        this.productSuccess = productSuccess;
        this.productEmpty = productEmpty;
        this.goBack = goBack;
    }

    public static FavouritesUIState initial () {
        return new FavouritesUIState(null, null, null, false, false, false, false, false);
    }

    public static FavouritesUIState isLoading() {
        return new FavouritesUIState(null, null, null, true, false, false, false, false);
    }

    public static FavouritesUIState error (Throwable throwable) {
        return new FavouritesUIState(null, null, throwable, false, false, false, false, false);
    }

    public static FavouritesUIState profileSuccess (Profile profile) {
        return new FavouritesUIState(profile, null, null, false, true, false, false, false);
    }

    public static FavouritesUIState productsSuccess (List<Product> products) {
        return new FavouritesUIState(null, products, null, false, false, true, false, false);
    }

    public static FavouritesUIState productsEmpty () {
        return new FavouritesUIState(null, new ArrayList<>(), null, false, false, false, true, false);
    }

    public static FavouritesUIState gotoBack () {
        return new FavouritesUIState(null, null, null, false, false, false, false, true);
    }
}
