package com.boxxit.boxxit.app.results;

import com.boxxit.boxxit.library.parse.models.Product;

import java.util.List;

public enum LoadProductsResult implements Result {
    LOADING,
    SUCCESS,
    ERROR;

    public Throwable throwable;
    public List<Product> products;

    public static LoadProductsResult error(Throwable throwable) {
        LoadProductsResult result = LoadProductsResult.ERROR;
        result.throwable = throwable;
        return result;
    }

    public static LoadProductsResult success(List<Product> products) {
        LoadProductsResult result = LoadProductsResult.SUCCESS;
        result.products = products;
        return result;
    }
}
