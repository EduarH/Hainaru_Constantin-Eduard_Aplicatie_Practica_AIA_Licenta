package com.buddy.buddy.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.buddy.buddy.R;
import com.buddy.buddy.controller.ClientBookingController;
import com.buddy.buddy.controller.DriverController;
import com.buddy.buddy.controller.GeofireController;
import com.buddy.buddy.controller.GoogleApiController;
import com.buddy.buddy.controller.NotificationController;
import com.buddy.buddy.controller.TokenController;
import com.buddy.buddy.controller.UserController;
import com.buddy.buddy.domain.ClientBooking;
import com.buddy.buddy.domain.FCMBody;
import com.buddy.buddy.domain.FCMResponse;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchDriverActivity extends AppCompatActivity {

    private LottieAnimationView lottieCar;
    private GeofireController geofireController;
    private double radious = 0.1;
    private LatLng originLatLng;
    private LatLng destinationLatLng;
    private boolean driverFound = false;
    private String idDriverFound;
    private LatLng driverFoundLatLng;
    private TextView textViewTitle;
    private Button buttonCancel;
    private NotificationController notificationController;
    private TokenController tokenController;
    private ClientBookingController clientBookingController;
    private UserController userController;
    private DriverController driverController;

    private String extraOrigin;
    private String extraDestination;
    private String stringDistance;
    private String stringDuration;

    private String customerSelectedCategoryName;
    private GoogleApiController apiController;
    private ValueEventListener listener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_driver);

        lottieCar = findViewById(R.id.lottieCar);
        textViewTitle = findViewById(R.id.textViewTitle);
        buttonCancel = findViewById(R.id.buttonCancel);
        lottieCar.playAnimation();
        notificationController = new NotificationController();
        tokenController = new TokenController();
        clientBookingController = new ClientBookingController();
        userController = new UserController();
        apiController = new GoogleApiController(this);
        driverController = new DriverController();

        extraOrigin = getIntent().getStringExtra("ORIGIN");
        extraDestination = getIntent().getStringExtra("DESTINATION");
        originLatLng = new LatLng(getIntent().getDoubleExtra("ORIGIN_LAT",0),getIntent().getDoubleExtra("ORIGIN_LON",0));
        destinationLatLng = new LatLng(getIntent().getDoubleExtra("DESTIN_LAT",0),getIntent().getDoubleExtra("DESTIN_LON",0));
        customerSelectedCategoryName = getIntent().getStringExtra("CUSTOMER_SELECTED_CATEGORY_NAME");
        geofireController = new GeofireController("active_drivers");
        getClosesDriver();

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelSearch();
            }
        });

    }

    private void cancelSearch() {
        clientBookingController.delete(userController.getUidCurrentUser()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(2);
                finish();
            }
        });

    }

    private void getClosesDriver(){
        geofireController.getActiveDrivers(originLatLng,radious).addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound){
                    getDriverDetails(key, location);
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                if (!driverFound){
                    radious = radious + 0.1f;
                    if (radious > 5){
                        Toast.makeText(SearchDriverActivity.this, "No nearby buddy found.", Toast.LENGTH_LONG).show();
                        textViewTitle.setText("We're sorry \n No nearby buddy found.");
                        return;
                    }else {
                        getClosesDriver();
                    }
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void getDriverDetails(String idDriver, GeoLocation location) {
        driverController.getDriver(idDriver).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String driverCategoryName = snapshot.child("categoryName").getValue().toString();
                    if (driverCategoryName.equals(customerSelectedCategoryName)){
                        driverFound = true;
                        idDriverFound = idDriver;
                        driverFoundLatLng = new LatLng(location.latitude,location.longitude);
                        textViewTitle.setText("Buddy found Waiting answer...");
                        //Toast.makeText(SearchDriverActivity.this, key, Toast.LENGTH_LONG).show();
                        createClientBooking();
                    }else {
                        driverFound = false;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendNotification( String time, String km) {
        tokenController.getToken(idDriverFound).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String token = snapshot.child("token").getValue().toString();

                    Map<String,String> map = new HashMap<>();
                    map.put("title","service request to " + time + ". of your position");
                    map.put("body","A customer is requesting service from "+ km +" away "+ "\n"+
                                        "Pick up in "+ extraOrigin+ "\n"+
                                        "Destination "+ extraDestination);
                    map.put("idClient",userController.getUidCurrentUser());
                    FCMBody fcmBody = new FCMBody(token,"high",map);
                    notificationController.sendNotification(fcmBody).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if (response.body() != null){
                                if (response.body().getSuccess()==1){
                                    ClientBooking clientBooking = new ClientBooking(userController.getUidCurrentUser(),idDriverFound,
                                            extraOrigin,extraDestination, time, km, "create",
                                            originLatLng.latitude,originLatLng.longitude,destinationLatLng.longitude,destinationLatLng.latitude);
                                    clientBookingController.create(clientBooking).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            checkStatusClientBooking();

                                        }
                                    });
                                }else {
                                    Toast.makeText(SearchDriverActivity.this, "The notification could not be sent.", Toast.LENGTH_LONG).show();
                                }
                            }else{
                                Toast.makeText(SearchDriverActivity.this, "The notification could not be sent.", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {

                        }
                    });
                }else{
                    Toast.makeText(SearchDriverActivity.this, "Could not send notification due to token error.", Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void checkStatusClientBooking() {
        listener = clientBookingController.getStatus(userController.getUidCurrentUser()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String status = snapshot.getValue().toString();
                    if (status.equals("accept")){
                        Intent intent = new Intent(SearchDriverActivity.this, MapClientBookingActivity.class);
                        startActivity(intent);
                        finish();
                    }else if(status.equals("cancel")){
                        Toast.makeText(SearchDriverActivity.this, "The buddy did not accept the trip.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void createClientBooking(){
        apiController.getDiretions(originLatLng,driverFoundLatLng).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body());
                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                    JSONObject route = jsonArray.getJSONObject(0);
                    JSONObject polylines = route.getJSONObject("overview_polyline");
                    String points = polylines.getString("points");
                    JSONArray legs = route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject distance = leg.getJSONObject("distance");
                    JSONObject duration = leg.getJSONObject("duration");

                    stringDistance = distance.getString("text");
                    stringDuration = duration.getString("text");

                    sendNotification(stringDuration, stringDistance);

                }catch (Exception e){
                    Log.d("RouteActivity", e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null){
            clientBookingController.getStatus(userController.getUidCurrentUser()).removeEventListener(listener);
        }
    }
}