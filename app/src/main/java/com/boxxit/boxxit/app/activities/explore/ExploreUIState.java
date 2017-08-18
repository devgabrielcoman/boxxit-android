package com.boxxit.boxxit.app.activities.explore;

import com.boxxit.boxxit.library.parse.models.Product;
import com.boxxit.boxxit.library.parse.models.facebook.Profile;

import java.util.List;

import static android.app.Activity.RESULT_OK;

public class ExploreUIState {
    Profile profile;
    List<Product> products;
    Throwable error;
    int backResult;

    boolean isLoading;
    boolean profileSuccess;
    boolean productSuccess;
    boolean goBack;
    boolean goToFav;

    public ExploreUIState(Profile profile, List<Product> products, Throwable error, int backResult, boolean isLoading, boolean profileSuccess, boolean productSuccess, boolean goBack, boolean goToFav) {
        this.profile = profile;
        this.products = products;
        this.error = error;
        this.backResult = backResult;
        this.isLoading = isLoading;
        this.profileSuccess = profileSuccess;
        this.productSuccess = productSuccess;
        this.goBack = goBack;
        this.goToFav = goToFav;
    }

    public static ExploreUIState initial () {
        return new ExploreUIState(null, null, null, RESULT_OK, false, false, false, false, false);
    }

    public static ExploreUIState isLoading() {
        return new ExploreUIState(null, null, null, RESULT_OK, true, false, false, false, false);
    }

    public static ExploreUIState error (Throwable throwable) {
        return new ExploreUIState(null, null, throwable, RESULT_OK, false, false, false, false, false);
    }

    public static ExploreUIState profileSuccess (Profile profile) {
        return new ExploreUIState(profile, null, null, RESULT_OK, false, true, false, false, false);
    }

    public static ExploreUIState productsSuccess (List<Product> products) {
        return new ExploreUIState(null, products, null, RESULT_OK, false, false, true, false, false);
    }

    public static ExploreUIState gotoBack (int backResult) {
        return new ExploreUIState(null, null, null, backResult, false, false, false, true, false);
    }

    public static ExploreUIState gotoFav () {
        return new ExploreUIState(null, null, null, RESULT_OK, false, false, false, false, true);
    }
}
