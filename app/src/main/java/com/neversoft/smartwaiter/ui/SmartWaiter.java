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

    @Override
    public void onCreate() {
        super.onCreate();
        Context mContext = this.getApplicationContext();
        //0=mode private. Only this app can read these preferences
        mPrefFirstRun = mContext.getSharedPreferences(PREF_CREACION_APLICACION, MODE_PRIVATE);
        Log.d(DBHelper.TAG, "QuickOrder application class has been started");
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