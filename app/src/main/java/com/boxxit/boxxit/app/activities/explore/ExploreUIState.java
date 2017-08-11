package com.boxxit.boxxit.app.activities.explore;

import com.boxxit.boxxit.library.parse.models.Product;
import com.boxxit.boxxit.library.parse.models.facebook.Profile;

import java.util.List;

public class ExploreUIState {
    Profile profile;
    List<Product> products;
    Throwable error;
    boolean isLoading;
    boolean profileSuccess;
    boolean productSuccess;

    public ExploreUIState(Profile profile, List<Product> products, Throwable error, boolean isLoading, boolean profileSuccess, boolean productSuccess) {
        this.profile = profile;
        this.products = products;
        this.error = error;
        this.isLoading = isLoading;
        this.profileSuccess = profileSuccess;
        this.productSuccess = productSuccess;
    }

    public static ExploreUIState initial () {
        return new ExploreUIState(null, null, null, false, false, false);
    }

    public static ExploreUIState isLoading() {
        return new ExploreUIState(null, null, null, true, false, false);
    }

    public static ExploreUIState error (Throwable throwable) {
        return new ExploreUIState(null, null, throwable, false, false, false);
    }

    public static ExploreUIState profileSuccess (Profile profile) {
        return new ExploreUIState(profile, null, null, false, true, false);
    }

    public static ExploreUIState productsSuccess (List<Product> products) {
        return new ExploreUIState(null, products, null, false, false, true);
    }
}
