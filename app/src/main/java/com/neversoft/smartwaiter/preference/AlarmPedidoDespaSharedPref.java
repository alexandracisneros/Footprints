package com.neversoft.smartwaiter.preference;

import android.content.SharedPreferences;
import android.util.Log;

import com.neversoft.smartwaiter.database.DBHelper;

/**
 * Created by Usuario on 30/03/2016.
 */
public class AlarmPedidoDespaSharedPref {
    public static final String NAME = "prefAlarmPedidosDespachados";
    public static final String FECHA_ULTIMA_SINCRONIZACION = "fecha_ult_sync";

    public static void save(SharedPreferences prefAlarm, long fecha_sync) {
        SharedPreferences.Editor editor = prefAlarm.edit();
        editor.putLong(FECHA_ULTIMA_SINCRONIZACION, fecha_sync);
        editor.commit();
        Log.d(DBHelper.TAG, "Guarde SharedPreferece 'prefAlarmPedidosDespachados'");
    }

    public static void remove(SharedPreferences prefAlarm) {
        SharedPreferences.Editor editor = prefAlarm.edit();
        editor.clear();
        editor.commit();
        Log.d(DBHelper.TAG, "Elimine SharedPreferece 'prefAlarmPedidosDespachados'");
    }
}
