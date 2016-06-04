package com.neversoft.smartwaiter.ui;

import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.neversoft.smartwaiter.R;
import com.neversoft.smartwaiter.database.DBHelper;
import com.neversoft.smartwaiter.io.RestConnector;
import com.neversoft.smartwaiter.io.RestUtil;
import com.neversoft.smartwaiter.model.business.PedidoDAO;
import com.neversoft.smartwaiter.model.business.SincroDAO;
import com.neversoft.smartwaiter.preference.ConexionSharedPref;
import com.neversoft.smartwaiter.preference.ControlSharedPref;
import com.neversoft.smartwaiter.preference.LoginSharedPref;
import com.neversoft.smartwaiter.preference.PedidoExtraSharedPref;
import com.neversoft.smartwaiter.preference.PedidoSharedPref;
import com.neversoft.smartwaiter.util.Funciones;

import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.Locale;

public class CambiarUsuarioActivity extends AppCompatActivity
        implements View.OnClickListener,
        NavigationView.OnNavigationItemSelectedListener {
    private String mUrlServer;
    private NavigationView mNavigationView;
    private SharedPreferences mPrefLogin;
    private SharedPreferences mPrefConfig;
    private SharedPreferences mPrefControl;
    private SharedPreferences mPrefConexion;
    private SharedPreferences mPedidoExtra;
    private TextView mUsuarioTextView;
    private TextView mEmpresaTextView;
    private TextView mUltimoLoginTextView;
    private Button mCambiarUsuarioButton;

    private MaterialDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambiar_usuario);
        overridePendingTransition(0, 0);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mUsuarioTextView = (TextView) findViewById(R.id.usuarioTextView);
        mEmpresaTextView = (TextView) findViewById(R.id.empresaTextView);
        mUltimoLoginTextView = (TextView) findViewById(R.id.ultimoLoginTextView);
        mCambiarUsuarioButton = (Button) findViewById(R.id.cambiarUsuarioButton);
        mCambiarUsuarioButton.setOnClickListener(this);

        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.getMenu().getItem(SmartWaiter.OPCION_USUARIO).setChecked(true);

        // get SharedPreferences
        mPrefConfig = getSharedPreferences(LoginActivity.PREF_CONFIG, MODE_PRIVATE);
        mPrefLogin = getSharedPreferences(LoginSharedPref.NAME, MODE_PRIVATE);
        mPrefControl = getSharedPreferences(ControlSharedPref.NAME, MODE_PRIVATE);
        mPrefConexion = getSharedPreferences(ConexionSharedPref.NAME, MODE_PRIVATE);
        mPedidoExtra = getSharedPreferences(PedidoExtraSharedPref.NAME, MODE_PRIVATE);

        mUsuarioTextView.setText(mPrefLogin.getString(LoginSharedPref.USUARIO, "Sin establecer"));
        mEmpresaTextView.setText(mPrefLogin.getString(LoginSharedPref.COMPANIA, "Sin establecer"));
        mUltimoLoginTextView.setText(Funciones.getDateFromTimeStamp(
                mPrefLogin.getLong(LoginSharedPref.FECHA_LOGIN, 0), "dd/MM/yyyy hh:mm:ss"));
        mUrlServer = RestUtil.obtainURLServer(getApplicationContext());
    }

    private void showProgressIndicator(boolean showValue) {
        if (showValue) {
            mProgress = new MaterialDialog.Builder(CambiarUsuarioActivity.this)
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
            case R.id.cambiarUsuarioButton:
                boolean isDayStarted;
                boolean isDataSynchronized;
                isDayStarted = mPrefControl.getBoolean(ControlSharedPref.INICIO_DIA, false);
                if (isDayStarted) {
                    isDataSynchronized = mPrefControl.getBoolean(ControlSharedPref.DATA_SINCRONIZADA, false);
                    if (isDataSynchronized) {
                        confirmarRealizacionDePedidos();
                    } else {
                        Toast.makeText(CambiarUsuarioActivity.this, "Aún no ha sincronizado los datos.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    cambiarUsuario();
                }
                break;
        }
    }

    public void cambiarUsuario() {
        LoginSharedPref.remove(mPrefLogin);
        Intent intentTo = new Intent(CambiarUsuarioActivity.this, LoginActivity.class);
        startActivity(intentTo);
        finish();
    }

    public void confirmarRealizacionDePedidos() {
        try {
            PedidoDAO pedidoDAO = new PedidoDAO(getApplicationContext());
            String mensaje;
            long nroPedidos = pedidoDAO.getNumeroPedidos(1);
            if (nroPedidos > 0) {
                mensaje = "De continuar el día se dará por cerrado y no podrá agregar más pedidos.¿Desea continuar?";
            } else {
                mensaje = "No ha realizado ningún pedido.De continuar el día se dará por cerrado.¿Desea continuar?";
            }
            new AlertDialog.Builder(this)
                    .setTitle("Confirmación")
                    .setMessage(mensaje)
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            // Start daily operations
                            // AQUI HACER LO MISMO QUE CON ENVIAR DATA
                            dialog.cancel();
                            cerrarDia();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();

                        }
                    }).show();


        } catch (Exception ex) {
            Toast.makeText(CambiarUsuarioActivity.this, "Se produjo el error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void cerrarDia() {
        String url = mUrlServer + "restaurante/CerrarDiaMozo/?fecha=%s&usuario=%s&codCia=%s&"
                + "cadenaConexion=%s";
        String fecha = mPrefControl
                .getString(ControlSharedPref.FECHA_INICIO_DIA, Funciones.getCurrentDate("yyyy/MM/dd"));
        String usuario = mPrefConfig.getString("Usuario", "").toUpperCase(
                Locale.getDefault());
        String codCia = mPrefConfig.getString("CodCia", "");
        String ambiente = mPrefConexion.getString(ConexionSharedPref.AMBIENTE, "");
        Log.d(DBHelper.TAG, ambiente);
        try {
            String encondedAmbiente = URLEncoder.encode(ambiente, "utf-8");
            String urlWithParams = String.format(url, fecha, usuario, codCia,
                    encondedAmbiente);
            new DoCerrarDia().execute(urlWithParams);
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        if (menuItem.getOrder() != SmartWaiter.OPCION_USUARIO) {
            WeakReference<AppCompatActivity> weakActivity = new WeakReference<AppCompatActivity>(CambiarUsuarioActivity.this);
            Funciones.selectMenuOption(weakActivity, menuItem.getOrder());
            return true;
        }
        return true;
    }

    class DoCerrarDia extends AsyncTask<String, Void, Object> {

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
            return requestObject;

        }

        @Override
        protected void onPostExecute(Object result) {
            // Clear progress indicator
            String mensaje;
            boolean resultadoIO;
            showProgressIndicator(false);
            try {
                resultadoIO = Boolean.parseBoolean(String.valueOf(result));
                if (resultadoIO) {

                    SincroDAO sincroDAO = new SincroDAO(getApplicationContext());
                    sincroDAO.dropDataDownloaded();

                    // Since the user is closing the day we need to
                    // reset all values from PREF_Control,except cerrarDia,
                    // will do that when we sync data
                    // PREF_Login, and the Downloaded Data
                    ControlSharedPref.save(mPrefControl, false, "", true, false, false, "", false);
                    PedidoSharedPref.clear(getApplicationContext());
                    PedidoExtraSharedPref.remove(mPedidoExtra);
                    // Clear out mPrefLoginValues
                    cambiarUsuario();
                    mensaje = "Día cerrado correctamente.";

                } else {
                    mensaje = "El mozo aún no ha iniciado el día.";

                }
                Toast.makeText(CambiarUsuarioActivity.this, mensaje, Toast.LENGTH_LONG).show();

            } catch (Exception ex) {
                Toast.makeText(CambiarUsuarioActivity.this, "Se produjo el error:" + ex.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }

        }
    }
}
