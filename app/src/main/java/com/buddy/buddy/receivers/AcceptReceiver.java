package com.buddy.buddy.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.buddy.buddy.activity.MapDriverBookingActivity;
import com.buddy.buddy.controller.ClientBookingController;
import com.buddy.buddy.controller.GeofireController;
import com.buddy.buddy.controller.UserController;

public class AcceptReceiver extends BroadcastReceiver {

    private ClientBookingController clientBookingController;
    private GeofireController geofireController;
    private UserController userController;

    @Override
    public void onReceive(Context context, Intent intent) {
        userController = new UserController();
        geofireController = new GeofireController("active_drivers");
        geofireController.removeLocation(userController.getUidCurrentUser());

        String idClient = intent.getExtras().getString("idClient");
        clientBookingController = new ClientBookingController();
        clientBookingController.updateStatus(idClient,"accept");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(2);

        Intent intentBooking = new Intent(context, MapDriverBookingActivity.class);
        intentBooking.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intentBooking.setAction(Intent.ACTION_RUN);
        intentBooking.putExtra("idClient", idClient);
        context.startActivity(intentBooking);

    }
}
