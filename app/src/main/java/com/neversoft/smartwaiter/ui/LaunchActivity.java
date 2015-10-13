package com.neversoft.smartwaiter.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.neversoft.smartwaiter.preference.ControlSharedPref;
import com.neversoft.smartwaiter.preference.LoginSharedPref;

/**
 * Created by Usuario on 02/10/2015.
 */
public class LaunchActivity extends Activity {
    private SharedPreferences mPrefLoginValues;
    private SharedPreferences mPrefControl;
    private SharedPreferences mPrefPedidoEnCurso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get SharedPreferences object
        mPrefLoginValues = getSharedPreferences(LoginSharedPref.NAME, MODE_PRIVATE);
        mPrefControl = getSharedPreferences(ControlSharedPref.NAME, MODE_PRIVATE);
//        mPrefPedidoEnCurso=getSharedPreferences(PREF_Pedido_EnCurso.NAME, MODE_PRIVATE); //TENER EN CUENTA PARA PEDIDO ENC CURSO

        showCorrespondingActivity();
        finish();
    }

    private void showCorrespondingActivity() {

        boolean hasDayBeenStarted, hasExit;
        hasDayBeenStarted = mPrefControl.getBoolean(ControlSharedPref.INICIO_DIA, false);
        Intent intent;
        // If the user has already started daily operations, then let him/her
        // use the QuickLogin Activity
        // this needs to be done because when the user logs in, A List of Price lists and
        // companies are downloaded, and this necessarily has to be the once the repartidor
        // is going to use the app
        if (hasDayBeenStarted) {
            hasExit = mPrefLoginValues.getBoolean(LoginSharedPref.SALIO_APLICACION, false);
            if (hasExit) { //User has exit the app via EXIT
                //Go to Quick Login
                intent = new Intent(LaunchActivity.this, LoginRapidoActivity.class);
            } else {
//                int pedidoIdEnCurso=mPrefPedidoEnCurso.getInt(PREF_Pedido_EnCurso.PEDIDO_ID, 0); //TENER EN CUENTA PARA PEDIDO ENC CURSO
                int pedidoIdEnCurso = 0;
                if (pedidoIdEnCurso > 0) {
                    //If there's a Pedido en Curso, go and show that Pedido
                    intent = new Intent(LaunchActivity.this, TomarPedidoActivity.class);
                    startActivity(intent);
                } else {
                    //Otherwise go straight to MenuPrincipal
                    intent = new Intent(LaunchActivity.this, MesasActivity.class);
                }
            }
        } else { //This is the first time the user has opened the app
            //Ask the user to Login in, having to fill in all the requeried fields
            intent = new Intent(LaunchActivity.this, LoginActivity.class);
        }
        startActivity(intent);
    }
}
