package com.boxxit.boxxit.app.activities.explore;

import com.boxxit.boxxit.library.parse.models.Product;

import java.util.List;

public class UpdateProductListUIModel {
    boolean success;
    boolean inProgress;
    String error;
    List<Product> products;

    public UpdateProductListUIModel(boolean success, boolean inProgress, String error, List<Product> products) {
        this.success = success;
        this.inProgress = inProgress;
        this.error = error;
        this.products = products;
    }

    static UpdateProductListUIModel inProgress () {
        return new UpdateProductListUIModel(false, true, null, null);
    }

    static UpdateProductListUIModel success (List<Product> products) {
        return new UpdateProductListUIModel(true, false, null, products);
    }

    static UpdateProductListUIModel error (Throwable throwable) {
        return new UpdateProductListUIModel(false, false, throwable.getLocalizedMessage(), null);
    }
}