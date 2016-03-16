package com.neversoft.smartwaiter.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.neversoft.smartwaiter.preference.ControlSharedPref;
import com.neversoft.smartwaiter.preference.LoginSharedPref;
import com.neversoft.smartwaiter.preference.PedidoExtraSharedPref;

/**
 * Created by Usuario on 02/10/2015.
 */
public class LaunchActivity extends Activity {
    private SharedPreferences mPrefControl;
    private SharedPreferences mPedidoExtras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get SharedPreferences object
        mPrefControl = getSharedPreferences(ControlSharedPref.NAME, MODE_PRIVATE);
        mPedidoExtras = getSharedPreferences(PedidoExtraSharedPref.NAME, MODE_PRIVATE);

        showCorrespondingActivity();
        finish();
    }

    private void showCorrespondingActivity() {

        boolean hasDayBeenStarted;
        hasDayBeenStarted = mPrefControl.getBoolean(ControlSharedPref.INICIO_DIA, false);
        Intent intent;
        if (hasDayBeenStarted) {
            boolean tableHasBeenSelected = mPedidoExtras.contains(PedidoExtraSharedPref.STARTING_ACTIVITY);
            if (tableHasBeenSelected) {
                intent = new Intent(LaunchActivity.this, TomarPedidoActivity.class);
                startActivity(intent);
            } else {
                intent = new Intent(LaunchActivity.this, MesasActivity.class);
            }
        } else { //This is the first time the user has opened the app
            //Ask the user to Login in, having to fill in all the required fields
            intent = new Intent(LaunchActivity.this, LoginActivity.class);
        }
        startActivity(intent);
    }
}
