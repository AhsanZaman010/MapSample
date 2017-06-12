package com.bt.ahsanzaman.mapsample.service;

import com.bt.ahsanzaman.mapsample.domain.DirectionResults;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Ahsan Zaman on 03-06-2017.
 */

public class LocationsService {

    private static String BASE_URL =  "https://maps.googleapis.com/maps/api/directions/";

    public interface  CountryAPI{
        @GET("json?sensor=false&units=metric&mode=driving&alternatives=true")
        io.reactivex.Observable<DirectionResults> getResults(@Query("origin")String origin, @Query("destination") String destination);
    }

    public CountryAPI getAPI(){
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient httpClient = new OkHttpClient.Builder().addInterceptor(logging).build();
        Retrofit retrofit = new Retrofit
                .Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(httpClient)
                .build();
        return retrofit.create(CountryAPI.class);
    }
}
