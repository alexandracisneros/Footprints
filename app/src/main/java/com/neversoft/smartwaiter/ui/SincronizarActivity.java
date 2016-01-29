package com.neversoft.smartwaiter.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.neversoft.smartwaiter.R;
import com.neversoft.smartwaiter.database.DBHelper;
import com.neversoft.smartwaiter.service.SincronizarService;
import com.neversoft.smartwaiter.util.Funciones;

import java.lang.ref.WeakReference;

public class SincronizarActivity extends Activity implements AdapterView.OnItemClickListener {
    private ListView mMenuListView;
    private FrameLayout mIndicatorFrameLayout;
    private RelativeLayout mMainRelativeLayout;


    private BroadcastReceiver onEventSincronizarDatosIniciales = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(DBHelper.TAG, "onEventSincronizarDatosIniciales broadcast receiveed.");
            // if necessary get data from intent
            boolean exito = intent.getBooleanExtra("exito", false);
            abortBroadcast();
            showProgressIndicator(false);
            String mensaje;
            if (exito) {
                mensaje = String.valueOf(intent.getIntExtra("resultado", 0));
                Log.d(DBHelper.TAG,
                        "Success from BroadcastReceiver within SincronizarActivity : "
                                + mensaje);
            } else {
                mensaje = intent.getStringExtra("mensaje");
                Log.d(DBHelper.TAG,
                        "Exception from BroadcastReceiver within SincronizarActivity :"
                                + mensaje);
                // update the display
                //PREF_Control.save(mPrefControl, null, null, null, null, null, "", false);
                Toast.makeText(SincronizarActivity.this, mensaje, Toast.LENGTH_LONG).show();
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sincronizar);
        overridePendingTransition(0, 0);

        // get reference to the ListView and set its listener
        mMenuListView = (ListView) findViewById(R.id.menu_listview);
        mMenuListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mMenuListView.setOnItemClickListener(this);

        Resources res = getResources();
        String[] options = res.getStringArray(R.array.menu_items_array);
        ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, options);
        mMenuListView.setAdapter(itemsAdapter);
        mMenuListView.setItemChecked(SmartWaiter.OPCION_SINCRONIZAR, true);

        mIndicatorFrameLayout = (FrameLayout) findViewById(R.id.loadingIndicatorLayout);
        mMainRelativeLayout = (RelativeLayout) findViewById(R.id.mainRelativeLayout);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    private void showProgressIndicator(boolean showValue) {
        if (showValue) {
            mMainRelativeLayout.setVisibility(View.GONE);
            mIndicatorFrameLayout.setVisibility(View.VISIBLE);
        } else {
            mMainRelativeLayout.setVisibility(View.VISIBLE);
            mIndicatorFrameLayout.setVisibility(View.GONE);
        }
    }

    public void onClick(View v) {
        // here get SharedPreferences and send them with the Intent
        Intent inputIntent = new Intent(SincronizarActivity.this,
                SincronizarService.class);
        Log.d(DBHelper.TAG, "Antes de startService");
        // Display progress to the user
        showProgressIndicator(true);
        startService(inputIntent);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v,
                            int position, long id) {
        if (parent.getId() == R.id.menu_listview) {
            if (position != SmartWaiter.OPCION_SINCRONIZAR) {
                WeakReference<Activity> weakActivity = new WeakReference<Activity>(this);
                Funciones.selectMenuOption(weakActivity, position);
            }
        }
    }
}
