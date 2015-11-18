package com.neversoft.smartwaiter.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.commonsware.cwac.wakeful.WakefulIntentService;

/**
 * Created by Usuario on 05/11/2015.
 */
public class ConsultarPedidosRecogerReceiver extends BroadcastReceiver {
    private static final int PERIOD=240000; // 4 minute
    private static final int INITIAL_DELAY=5000; // 5 seconds
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null) {
            WakefulIntentService.sendWakefulWork(context, ConsultarPedidosRecogerService.class);
        }
        else {
            scheduleAlarms(context);
        }
    }
    public static void scheduleAlarms(Context ctxt) {
        AlarmManager mgr=
                (AlarmManager)ctxt.getSystemService(Context.ALARM_SERVICE);
        Intent i=new Intent(ctxt, ConsultarPedidosRecogerReceiver.class);
        PendingIntent pi=PendingIntent.getBroadcast(ctxt, 0, i, 0);

        mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + INITIAL_DELAY,
                PERIOD, pi);

    }
}
