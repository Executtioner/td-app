package com.example.test.rest;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiInterface {
    @POST("booking/set/travelers_present/")
        //@Headers("Content-Type: application/json")
        Call<Void> createPost(@Body RequestBody body);

}
