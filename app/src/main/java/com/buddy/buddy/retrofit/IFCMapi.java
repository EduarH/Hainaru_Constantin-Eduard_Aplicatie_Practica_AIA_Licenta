package com.buddy.buddy.retrofit;

import com.buddy.buddy.domain.FCMBody;
import com.buddy.buddy.domain.FCMResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMapi {

    String key_server ="AAAAZEc1LQU:APA91bEGfmvPEw1YYrU0g5AFXTzMn-UA_wdg7WHSQha6g8kGP0LJXs6d-3riSqFZyvHpyPxQqNYcfBUBo06Vd09z2XbkEyGaAbzMmF0roEh38RjDCkTB5wTKmcqPI901GZMq54_hXGPX";

    @Headers({
            "Content-Type:application/json",
            "Authorization:key="+key_server
    })
    @POST("fcm/send")
    Call<FCMResponse> send(@Body FCMBody body);


}
