package com.neversoft.smartwaiter.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.neversoft.smartwaiter.R;
import com.neversoft.smartwaiter.ui.PedidosARecogerActivity;

import java.util.Calendar;
import java.util.Random;

/**
 * Created by Usuario on 05/11/2015.
 */
public class ConsultarPedidosRecogerService extends WakefulIntentService {
    private static final String NAME = "ConsultarPedidosRecoger";
    private static int NOTIFY_ID = 1337;
    private Random rng = new Random();

    public ConsultarPedidosRecogerService() {
        super(NAME);
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        Intent event = new Intent(PedidosARecogerActivity.ACTION_EVENT);
        long now = Calendar.getInstance().getTimeInMillis();
        int random = rng.nextInt();

        event.putExtra(PedidosARecogerActivity.EXTRA_RANDOM, random);
        event.putExtra(PedidosARecogerActivity.EXTRA_TIME, now);
        //Log.d(getClass().getSimpleName(), "I ran!");
        if (!LocalBroadcastManager.getInstance(this).sendBroadcast(event)) {
            Log.d(getClass().getSimpleName(), "I only run when I have to show a notification!");
            NotificationCompat.Builder b = new NotificationCompat.Builder(this);
            Intent ui = new Intent(this, PedidosARecogerActivity.class);

            b.setAutoCancel(true).setDefaults(Notification.DEFAULT_SOUND)
                    .setContentTitle(getString(R.string.notif_title))
                    .setContentText(Integer.toHexString(random))
                    .setSmallIcon(android.R.drawable.stat_notify_more)
                    .setTicker(getString(R.string.notif_title))
                    .setContentIntent(PendingIntent.getActivity(this, 0, ui, 0));

            NotificationManager mgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            mgr.notify(NOTIFY_ID, b.build());
        }
    }
}
