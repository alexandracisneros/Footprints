package com.neversoft.smartwaiter.ui;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.neversoft.smartwaiter.R;
import com.neversoft.smartwaiter.database.DBHelper;
import com.neversoft.smartwaiter.io.RestConnector;
import com.neversoft.smartwaiter.io.RestUtil;
import com.neversoft.smartwaiter.preference.ConexionSharedPref;
import com.neversoft.smartwaiter.preference.ControlSharedPref;
import com.neversoft.smartwaiter.util.Funciones;

import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.Locale;

public class IniciarDiaActivity extends AppCompatActivity
        implements View.OnClickListener,
        NavigationView.OnNavigationItemSelectedListener {
    private NavigationView mNavigationView;
    private Button mIniciarDiaButton;
    private String mUrlServer;
    private SharedPreferences mPrefControl;
    private SharedPreferences mPrefConfig;
    private SharedPreferences mPrefConexion;
    private MaterialDialog mProgress;
    private String mFechaInicioDia;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iniciar_dia);
        overridePendingTransition(0, 0);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // get SharedPreferences
        mPrefConfig = getSharedPreferences(LoginActivity.PREF_CONFIG, MODE_PRIVATE);
        mPrefControl = getSharedPreferences(ControlSharedPref.NAME, MODE_PRIVATE);
        mPrefConexion = getSharedPreferences(ConexionSharedPref.NAME, MODE_PRIVATE);

        mIniciarDiaButton = (Button) findViewById(R.id.iniciarDiaButton);
        mIniciarDiaButton.setOnClickListener(this);


        mUrlServer = RestUtil.obtainURLServer(getApplicationContext());

        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.getMenu().getItem(SmartWaiter.OPCION_INICIAR_DIA).setChecked(true);

    }


    private void showProgressIndicator(boolean showValue) {
        if (showValue) {
            mProgress = new MaterialDialog.Builder(IniciarDiaActivity.this)
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iniciarDiaButton:
                boolean isDayStarted = mPrefControl.getBoolean(ControlSharedPref.INICIO_DIA,
                        false);
                if (!isDayStarted) {
                    confirmarIniciarDia();
                } else {
                    Toast.makeText(IniciarDiaActivity.this,
                            "Ya se ha iniciado día en el dispositivo.",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void confirmarIniciarDia() {

        new AlertDialog.Builder(this)
                .setTitle("Confirmación")
                .setMessage("¿Realmente desea iniciar las operaciones diarias?")
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Start daily operations
                        dialog.cancel();
                        iniciarDia();

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();

                    }
                }).show();

    }

    public void iniciarDia() {
        String url = mUrlServer
                + "restaurante/IniciarDiaMozo/?fecha=%s&codCia=%s&usuario=%s&cadenaConexion=%s";
        http:
//siempresoftqa.cloudapp.net/pruebamovilalex/api/restaurante/IniciarDiaMozo/?fecha=2016/01/01&usuario=SUPERVISOR&cadenaConexion=Initial%20Catalog=PRUEBAMOVILJHAV&codCia=001
        mFechaInicioDia = Funciones.getCurrentDate("yyyy/MM/dd");
        String codCia = mPrefConfig.getString("CodCia", "");
        String usuario = mPrefConfig.getString("Usuario", "").toUpperCase(
                Locale.getDefault());
        String ambiente = mPrefConexion.getString(ConexionSharedPref.AMBIENTE, "");
        Log.d(DBHelper.TAG, ambiente);
        try {
            // Simple GET
            String mensajeError = "";

            if (codCia != "") {
                if (usuario != "") {
                    String encondedAmbiente = URLEncoder.encode(ambiente,
                            "utf-8");
                    String urlWithParams = String.format(url,
                            mFechaInicioDia, codCia, usuario, encondedAmbiente);
                    new DoIniciarDia().execute(urlWithParams);
                } else {
                    mensajeError = "No se ha configurado 'código de vendedor'";
                }
            } else {
                mensajeError = "No se ha configurado 'código de compañía'";
            }

            if (mensajeError != "") {
                throw new Exception(mensajeError);
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        if (menuItem.getOrder() != SmartWaiter.OPCION_INICIAR_DIA) {
            WeakReference<AppCompatActivity> weakActivity = new WeakReference<AppCompatActivity>(IniciarDiaActivity.this);
            Funciones.selectMenuOption(weakActivity, menuItem.getOrder());
            return true;
        }
        return true;
    }

    class DoIniciarDia extends AsyncTask<String, Void, Object> {
        @Override
        protected void onPreExecute() {
            showProgressIndicator(true);
        }

        @Override
        protected Object doInBackground(String... params) {
            Object requestObject = null;
            String url = params[0];
            Log.d(DBHelper.TAG, url);
            RestConnector restConnector;
            try {
                if (Funciones.hasActiveInternetConnection(getApplicationContext())) {
                    restConnector = RestUtil.obtainGetConnection(url);
                    requestObject = restConnector.doRequest(url);
                }
            } catch (Exception e) {
                requestObject = e;
            }
//            SystemClock.sleep(5000);
            return requestObject;
        }

        @Override
        protected void onPostExecute(Object result) {
            // Clear progress indicator
            String response;
            showProgressIndicator(false);
            boolean resultadoIO;
            resultadoIO = Boolean.parseBoolean(String.valueOf(result));
            if (resultadoIO) {
                // update login data to SharedPreferences
                // We only want to change the value for 'inicioDia'
                // We DO NOT want to change any of the other values
                ControlSharedPref.save(mPrefControl, true, mFechaInicioDia,
                        null, null, null, null, false);
                response = "Día iniciado correctamente.";


            } else {
                response = "No se pudo iniciar el día porque existe un registro" +
                        " anterior o el día aún no se ha iniciado en el servidor.";
            }
            Toast.makeText(IniciarDiaActivity.this, response, Toast.LENGTH_LONG).show();
        }

    }


}
