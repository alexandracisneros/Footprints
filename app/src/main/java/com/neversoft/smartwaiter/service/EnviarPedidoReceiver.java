package com.neversoft.smartwaiter.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.neversoft.smartwaiter.ui.TomarPedidoActivity;

/**
 * Created by Usuario on 18/09/2015.
 */
public class EnviarPedidoReceiver  extends BroadcastReceiver {
    private static final int SEND_DATA_NOTIFY_ID = 1100;

    @Override
    public void onReceive(Context context, Intent intentFrom) {

        boolean exito = intentFrom.getBooleanExtra("exito", false);
        String mensajeError = intentFrom.getStringExtra("mensaje");
        NotificationManager notificationMgr = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context);
        Intent intentTo;
        intentTo = new Intent(context, TomarPedidoActivity.class);
        intentTo.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        builder.setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis());

        if (exito) {
            builder.setContentTitle("Pedido Enviado")
                    .setContentText("Pedido Enviado")
                    .setSmallIcon(android.R.drawable.stat_sys_upload)
                    .setTicker("Pedido enviado correctamente");
        } else {
            builder.setContentTitle("Error de Envio"
                    )
                    .setContentText(mensajeError)
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setTicker("Se produjo un error");

        }
        // it used to work with a request code of ZERO
        // http://stackoverflow.com/questions/19031861/pendingintent-not-opening-activity-in-android-4-3
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                SEND_DATA_NOTIFY_ID + 2, intentTo,
                PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setDefaults(Notification.DEFAULT_SOUND
                | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);
        builder.setContentIntent(pendingIntent);
        notificationMgr.notify(SEND_DATA_NOTIFY_ID, builder.build());

    }

}
