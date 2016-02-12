package com.neversoft.smartwaiter.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.neversoft.smartwaiter.ui.SincronizarActivity;

/**
 * Created by Usuario on 29/01/2016.
 */
public class SincronizarReceiver extends BroadcastReceiver {

    private static final int SYNC_NOTIFY_ID = 1001;

    @Override
    public void onReceive(Context context, Intent intentFrom) {
        boolean exito = intentFrom.getBooleanExtra("exito", false);
        String mensajeError = intentFrom.getStringExtra("mensaje");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        Intent intentTo = new Intent(context, SincronizarActivity.class);
        intentTo.putExtras(intentFrom);
        intentTo.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        builder.setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis());
        if (exito) {
            builder.setContentTitle("Sincronización Realizada con éxito.")
                    .setContentText("La sincronización realizada con éxito.")
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .setTicker("Sincronización Realizada con éxito.");

        } else {
            builder.setContentTitle("No se pudo completar la sincronización.")
                    .setContentText(mensajeError)
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setTicker("No se pudo completar la sincronización.");
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(context, SYNC_NOTIFY_ID, intentTo, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pendingIntent);
        notificationManager.notify(SYNC_NOTIFY_ID, builder.build());


    }
}
