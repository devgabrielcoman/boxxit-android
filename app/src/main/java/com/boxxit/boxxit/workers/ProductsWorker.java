package com.boxxit.boxxit.workers;

import com.boxxit.boxxit.library.network.NetworkRequest;
import com.boxxit.boxxit.library.network.NetworkTask;
import com.boxxit.boxxit.library.parse.ParseBackendDataTask;
import com.boxxit.boxxit.library.parse.models.Product;

import java.util.List;

import rx.Observable;
import rx.Single;

public class ProductsWorker {

    public static Observable<List<Product>> getProductsForUser(String id, int min, int max) {
        NetworkRequest request = NetworkRequest.getProductsForUser(id, min, max);
        NetworkTask task = new NetworkTask();
        return task.execute(request)
                .flatMap(s -> new ParseBackendDataTask().execute(s))
                .toObservable()
                .flatMap(productBackendData -> Observable.just(productBackendData.data));
    }

    public static Observable<List<Product>> getFavouriteProductsForUser(String id) {
        NetworkRequest request = NetworkRequest.getFavouriteProductsForUser(id);
        NetworkTask task = new NetworkTask();
        return task.execute(request)
                .flatMap(s -> new ParseBackendDataTask().execute(s))
                .toObservable()
                .flatMap(productBackendData -> Observable.just(productBackendData.data));
    }

    public static Single<Void> saveFavouriteProduct(String asin, String id) {
        NetworkRequest request = NetworkRequest.saveProduct(id, asin);
        NetworkTask task = new NetworkTask();
        return task.execute(request).map(s -> null);
    }

    public static Single<Void> deleteFavouriteProduct(String asin, String id) {
        NetworkRequest request = NetworkRequest.deleteProduct(id, asin);
        NetworkTask task = new NetworkTask();
        return task.execute(request).map(s -> null);
    }
}
