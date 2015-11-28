package com.neversoft.smartwaiter.preference;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.neversoft.smartwaiter.database.DBHelper;


/**
 * Created by Usuario on 02/10/2015.
 */
public class ControlSharedPref {
    public static final String NAME = "prefControl";
    public static final String INICIO_DIA = "inicioDia";
    public static final String FECHA_INICIO_DIA = "fechaInicioDia";
    public static final String CIERRE_DIA = "cierreDia";
    public static final String DATA_SINCRONIZADA = "dataSincronizada";
    public static final String DATA_ENVIADA = "dataEnviada";
    public static final String EXCEPCION_SERVICIO = "excepcionServicio";

    public static void save(SharedPreferences prefControl, Boolean inicioDia, String fechaInicioDia,
                            Boolean cierreDia, Boolean dataSincroniza, Boolean dataEnviada, String excepcionMensaje,
                            boolean limpiarValoresAnteriores) {
        Editor editor = prefControl.edit();
        if (limpiarValoresAnteriores) editor.clear();
        if (inicioDia != null) editor.putBoolean(INICIO_DIA, inicioDia);
        if (fechaInicioDia != null) editor.putString(FECHA_INICIO_DIA, fechaInicioDia);
        if (cierreDia != null) editor.putBoolean(CIERRE_DIA, cierreDia);
        if (dataSincroniza != null) editor.putBoolean(DATA_SINCRONIZADA, dataSincroniza);
        if (dataEnviada != null) editor.putBoolean(DATA_ENVIADA, dataEnviada);
        //Exclusive for Services and BroadcastReceivers
        if (excepcionMensaje != null) editor.putString(EXCEPCION_SERVICIO, excepcionMensaje.trim());
        editor.commit();

        Log.d(DBHelper.TAG, "Guarde SharedPreferece 'PREF_Control'");

    }

    public static void remove(SharedPreferences prefControl) {
        Editor editor = prefControl.edit();
        editor.clear();
        editor.commit();
        Log.d(DBHelper.TAG, "Elimine SharedPreferece 'PREF_Control'");
    }
}
