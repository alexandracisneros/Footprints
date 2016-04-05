package com.neversoft.smartwaiter.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.neversoft.smartwaiter.R;
import com.neversoft.smartwaiter.database.DBHelper;
import com.neversoft.smartwaiter.service.SincronizarService;
import com.neversoft.smartwaiter.util.Funciones;

import java.lang.ref.WeakReference;

public class SincronizarActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private NavigationView mNavigationView;
    private MaterialDialog mProgress;


    private BroadcastReceiver onEventSincronizarDatosIniciales = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(DBHelper.TAG, "onEventSincronizarDatosIniciales broadcast receiveed.");
            // if necessary get data from intent
            boolean exito = intent.getBooleanExtra(SincronizarService.EXTRA_RESULTADO_EXITO, false);
            String mensajeError = intent.getStringExtra(SincronizarService.EXTRA_RESULTADO_MENSAJE);
            abortBroadcast();
            showProgressIndicator(false);
            if (exito) {
                mensajeError = "Sincronización Realizada con éxito.";
                Log.d(DBHelper.TAG, "Success from BroadcastReceiver within SincronizarActivity : " + exito);
            } else {
                Log.d(DBHelper.TAG, "Exception from BroadcastReceiver within SincronizarActivity :"
                        + mensajeError);
                // update the display
                //PREF_Control.save(mPrefControl, null, null, null, null, null, "", false);
            }
            Toast.makeText(SincronizarActivity.this, mensajeError, Toast.LENGTH_LONG).show();

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sincronizar);
        overridePendingTransition(0, 0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // get reference to the ListView and set its listener
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.getMenu().getItem(SmartWaiter.OPCION_SINCRONIZAR).setChecked(true);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(DBHelper.TAG, "Entre a onResume - SincronizarActivity");
        IntentFilter filter = new IntentFilter(SincronizarService.ACTION_SYNC_DATA);
        filter.setPriority(2);
        registerReceiver(onEventSincronizarDatosIniciales, filter);

        //TODO : <--- ACA ME QUEDE
//        if (!isMyServiceRunning(SincronizarService.class)) {
//            boolean isDataSynchronized = mPrefControl.getBoolean(
//                    PREF_Control.DATA_SINCRONIZADA, false);
//            if (isDataSynchronized) { // if data is already synchronized
//
//                enableControles(true);
//
//            } else { // data is not synchronized yet
//
//                enableControles(false);
//                String exceptionMessageInService = mPrefControl.getString(
//                        PREF_Control.EXCEPCION_SERVICIO, "");
//                if (exceptionMessageInService != "") {
//
//                    //Clear out the value of 'excepcionServicio'
//                    PREF_Control.save(mPrefControl, null, null, null, null, null, "", false);
//                    Toast.makeText(SincronizarActivity.this,
//                            exceptionMessageInService,
//                            Toast.LENGTH_LONG).show();
//                }
//            }
//        }
    }

    @Override
    protected void onPause() {
        unregisterReceiver(onEventSincronizarDatosIniciales);
        Log.d(DBHelper.TAG, "Entre a onPause - SincronizarActivity");
        super.onPause();
    }


    private void showProgressIndicator(boolean showValue) {
        if (showValue) {
            mProgress = new MaterialDialog.Builder(SincronizarActivity.this)
                    .content("Espere por favor...")
                    .cancelable(false)
                    .progress(true, 0)
                    .show();
        } else {
            if (mProgress != null) {
                mProgress.dismiss();
            }
        }
    }

    public void onClick(View v) {
        // here get SharedPreferences and send them with the Intent
        confirmarSincronizar();

    }

    public void confirmarSincronizar() {

        new AlertDialog.Builder(this)
                .setTitle("Confirmación")
                .setMessage("¿Realmente desea iniciar el proceso de sincronización?")
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Start daily operations
                        dialog.cancel();
                        Intent inputIntent = new Intent(SincronizarActivity.this, SincronizarService.class);
                        Log.d(DBHelper.TAG, "Antes de startService");
                        // Display progress to the user
                        showProgressIndicator(true);
                        startService(inputIntent);

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();

                    }
                }).show();

    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        if (menuItem.getOrder() != SmartWaiter.OPCION_SINCRONIZAR) {
            WeakReference<Activity> weakActivity = new WeakReference<Activity>(SincronizarActivity.this);
            Funciones.selectMenuOption(weakActivity, menuItem.getOrder());
            return true;
        }
        return true;
    }
}
