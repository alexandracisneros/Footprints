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
    private SharedPreferences mPrefLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get SharedPreferences object
        mPrefControl = getSharedPreferences(ControlSharedPref.NAME, MODE_PRIVATE);
        mPedidoExtras = getSharedPreferences(PedidoExtraSharedPref.NAME, MODE_PRIVATE);
        mPrefLogin = getSharedPreferences(LoginSharedPref.NAME, MODE_PRIVATE);

        showCorrespondingActivity();
        finish();
    }

    private void showCorrespondingActivity() {

        boolean isUserLoggedIn;
        boolean isDayStarted;
        boolean isDataSynchronized;
        boolean tableHasBeenSelected;
        Intent intent;

        isUserLoggedIn = mPrefLogin.contains(LoginSharedPref.USUARIO);
        if (isUserLoggedIn) {
            isDayStarted = mPrefControl.getBoolean(ControlSharedPref.INICIO_DIA, false);
            if (isDayStarted) {
                isDataSynchronized = mPrefControl.getBoolean(ControlSharedPref.DATA_SINCRONIZADA, false);
                if (isDataSynchronized) {
                    tableHasBeenSelected = mPedidoExtras.contains(PedidoExtraSharedPref.STARTING_ACTIVITY);
                    if (tableHasBeenSelected) {
                        intent = new Intent(LaunchActivity.this, TomarPedidoActivity.class);
                    } else {
                        intent = new Intent(LaunchActivity.this, MesasActivity.class);
                    }
                } else {
                    intent = new Intent(LaunchActivity.this, SincronizarActivity.class);
                }
            } else {
                intent = new Intent(LaunchActivity.this, IniciarDiaActivity.class);
            }
        } else {
            intent = new Intent(LaunchActivity.this, LoginActivity.class);
        }
        startActivity(intent);
    }
}
