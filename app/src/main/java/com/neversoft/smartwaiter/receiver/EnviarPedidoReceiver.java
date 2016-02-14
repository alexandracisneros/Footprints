package com.neversoft.smartwaiter.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;

import com.neversoft.smartwaiter.preference.PedidoExtraSharedPref;
import com.neversoft.smartwaiter.preference.PedidoSharedPref;
import com.neversoft.smartwaiter.service.EnviarPedidoService;
import com.neversoft.smartwaiter.ui.MesasActivity;
import com.neversoft.smartwaiter.ui.TomarPedidoActivity;

/**
 * Created by Usuario on 18/09/2015.
 */
public class EnviarPedidoReceiver extends BroadcastReceiver {
    private static final int SEND_DATA_NOTIFY_ID = 1100;

    @Override
    public void onReceive(Context context, Intent intentFrom) {

        //Retrieve Extras
        boolean exito = intentFrom.getBooleanExtra(EnviarPedidoService.EXTRA_RESULTADO_EXITO, false);
        String mensajeError = intentFrom.getStringExtra(EnviarPedidoService.EXTRA_RESULTADO_MENSAJE);

        //Retrieve Preferences
        SharedPreferences prefPedidoExtras=context.getSharedPreferences(PedidoExtraSharedPref.NAME, context.MODE_PRIVATE);
        String className =prefPedidoExtras.getString(PedidoExtraSharedPref.STARTING_ACTIVITY, MesasActivity.class.getClass().getName());

        Class<?> clase = MesasActivity.class; //Clase por defecto para evitar asignar null
        try {
            clase = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        NotificationManager notificationMgr = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context);
        Intent intentTo;
        if (exito) {
            intentTo = new Intent(context, clase);
            builder.setContentTitle("Pedido Enviado")
                    .setContentText("Pedido Enviado")
                    .setSmallIcon(android.R.drawable.stat_sys_upload)
                    .setTicker("Pedido enviado correctamente");
            PedidoSharedPref.clear(context);
            PedidoExtraSharedPref.remove(prefPedidoExtras);
        } else {
            intentTo = new Intent(context, TomarPedidoActivity.class);
            builder.setContentTitle("Error de Envio")
                    .setContentText(mensajeError)
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setTicker("Se produjo un error");

        }
        intentTo.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentTo.putExtras(intentFrom);
        builder.setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis());
        // it used to work with a request code of ZERO
        // http://stackoverflow.com/questions/19031861/pendingintent-not-opening-activity-in-android-4-3
        PendingIntent pendingIntent = PendingIntent.getActivity(context, SEND_DATA_NOTIFY_ID, intentTo,
                PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pendingIntent);
        notificationMgr.notify(SEND_DATA_NOTIFY_ID, builder.build());

    }

}
