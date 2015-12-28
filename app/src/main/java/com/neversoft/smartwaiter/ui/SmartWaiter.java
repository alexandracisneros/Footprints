package com.neversoft.smartwaiter.ui;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.neversoft.smartwaiter.database.DBHelper;

/**
 * Created by Usuario on 01/10/2015.
 */
public class SmartWaiter extends Application {
    public static final String PREF_CREACION_APLICACION = "prefCreacionAplicacion";
    public static final String pref_primera_ejecucion = "app_primera_ejecucion";
    private SharedPreferences mPrefFirstRun;

    //Opciones de Menu
    public static final int OPCION_INICIAR_DIA=0;
    public static final int OPCION_SINCRONIZAR=1;
    public static final int OPCION_RESERVAS=2;
    public static final int OPCION_TOMAR_PEDIDO=3;
    public static final int OPCION_PEDIDOS_RECOGER=4;
    public static final int OPCION_PEDIDOS_FACTURAR=5;
    public static final int OPCION_CERRAR_DIA=6;


    @Override
    public void onCreate() {
        super.onCreate();
        Context mContext = this.getApplicationContext();
        //0=mode private. Only this app can read these preferences
        mPrefFirstRun = mContext.getSharedPreferences(PREF_CREACION_APLICACION, MODE_PRIVATE);
        Log.d(DBHelper.TAG, "SmartWaiter application class has been started");
    }

    public boolean getFirstRun() {
        return mPrefFirstRun.getBoolean(SmartWaiter.pref_primera_ejecucion, true);
    }

    public void setRunned() {
        SharedPreferences.Editor edit = mPrefFirstRun.edit();
        edit.putBoolean(SmartWaiter.pref_primera_ejecucion, false);
        edit.commit();
    }

}