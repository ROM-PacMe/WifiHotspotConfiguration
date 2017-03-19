package com.islavstan.wifisetting.points;


import com.islavstan.wifisetting.model.Date;

import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import rx.Observable;

public interface Point {
    @PUT("android")
    Observable<Response<Date>> retrieveDate(@Query("key") String key, @Query("func") String func);
}
