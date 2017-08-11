package com.boxxit.boxxit.app.activities.explore;

import com.boxxit.boxxit.library.parse.models.Product;
import com.boxxit.boxxit.library.parse.models.facebook.Profile;

import java.util.List;

public class ExploreUIModel {
    Profile profile;
    List<Product> products;
    Throwable error;
    boolean isLoading;
    boolean profileSuccess;
    boolean productSuccess;

    public ExploreUIModel(Profile profile, List<Product> products, Throwable error, boolean isLoading, boolean profileSuccess, boolean productSuccess) {
        this.profile = profile;
        this.products = products;
        this.error = error;
        this.isLoading = isLoading;
        this.profileSuccess = profileSuccess;
        this.productSuccess = productSuccess;
    }

    public static ExploreUIModel isLoading() {
        return new ExploreUIModel(null, null, null, true, false, false);
    }

    public static ExploreUIModel error (Throwable throwable) {
        return new ExploreUIModel(null, null, throwable, false, false, false);
    }

    public static ExploreUIModel profileSuccess (Profile profile) {
        return new ExploreUIModel(profile, null, null, false, true, false);
    }

    public static ExploreUIModel productsSuccess (List<Product> products) {
        return new ExploreUIModel(null, products, null, false, false, true);
    }
}
