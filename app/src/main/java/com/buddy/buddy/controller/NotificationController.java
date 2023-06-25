package com.buddy.buddy.controller;

import com.buddy.buddy.domain.FCMBody;
import com.buddy.buddy.domain.FCMResponse;
import com.buddy.buddy.retrofit.IFCMapi;
import com.buddy.buddy.retrofit.RetrofitClient;

import retrofit2.Call;

public class NotificationController {
    private  String url = "https://fcm.googleapis.com";

    public NotificationController(){

    }

    public Call<FCMResponse> sendNotification(FCMBody body){
        return RetrofitClient.getClientObject(url).create(IFCMapi.class).send(body);
    }

}
