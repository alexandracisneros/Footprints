package com.neversoft.smartwaiter.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.neversoft.smartwaiter.preference.AlarmPedidoDespaSharedPref;
import com.neversoft.smartwaiter.preference.ConexionSharedPref;
import com.neversoft.smartwaiter.service.ConsultarPedidosRecogerService;
import com.neversoft.smartwaiter.util.Funciones;

import java.util.Date;

/**
 * Created by Usuario on 05/11/2015.
 */
public class ConsultarPedidosRecogerReceiver extends BroadcastReceiver {
    private static final int PERIOD = 120000; // 2 minutes
    private static final int INITIAL_DELAY = 60000; // 5 seconds

    public static void scheduleAlarms(Context ctxt) {
        SharedPreferences prefAlarmDespachos;
        prefAlarmDespachos = ctxt.getApplicationContext().getSharedPreferences(ConexionSharedPref.NAME, Context.MODE_PRIVATE);
        long lastTimeStamp = prefAlarmDespachos.getLong(AlarmPedidoDespaSharedPref.FECHA_ULTIMA_SINCRONIZACION,
                Funciones.getCurrentTimeStamp());
        long currentTimeStamp = new Date().getTime();
        if (currentTimeStamp - lastTimeStamp > PERIOD) {
            AlarmManager mgr = (AlarmManager) ctxt.getSystemService(Context.ALARM_SERVICE);
            Intent i = new Intent(ctxt, ConsultarPedidosRecogerReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(ctxt, 0, i, 0);

            mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + INITIAL_DELAY, PERIOD, pi);
        }

    }

    public static void cancelWakefulWork(Context ctxt) {
//        AlarmManager mgr = (AlarmManager) ctxt.getSystemService(Context.ALARM_SERVICE);
//        Intent i = new Intent(ctxt, ConsultarPedidosRecogerReceiver.class);
//        PendingIntent pi = PendingIntent.getBroadcast(ctxt, 0, i, 0);//
//        mgr.cancel(pi);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null) {
            WakefulIntentService.sendWakefulWork(context, ConsultarPedidosRecogerService.class);
        } else {
            scheduleAlarms(context);
        }
    }
}
