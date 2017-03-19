package com.islavstan.wifisetting.api;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    public static final String BASE_URL = "https://my.vomer.com.ua:3300/";
    private static Retrofit rxRetrofit = null;


    public static Retrofit getRxRetrofit() {
        if (rxRetrofit == null) {
            Gson result = new GsonBuilder()
                    .setLenient()
                    .create();
            rxRetrofit = new Retrofit.Builder()
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(result))
                    .baseUrl(BASE_URL)
                    .build();

        }
        return rxRetrofit;

    }
}
