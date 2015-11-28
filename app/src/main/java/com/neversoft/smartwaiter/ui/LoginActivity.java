package com.neversoft.smartwaiter.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.neversoft.smartwaiter.R;
import com.neversoft.smartwaiter.database.DBHelper;
import com.neversoft.smartwaiter.io.RestConnector;
import com.neversoft.smartwaiter.io.RestUtil;
import com.neversoft.smartwaiter.model.entity.SpinnerEE;
import com.neversoft.smartwaiter.preference.ConexionSharedPref;
import com.neversoft.smartwaiter.preference.ControlSharedPref;
import com.neversoft.smartwaiter.preference.LoginSharedPref;
import com.neversoft.smartwaiter.preference.PedidoSharedPref;
import com.neversoft.smartwaiter.util.Funciones;

import org.apache.http.client.HttpResponseException;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;

public class LoginActivity extends Activity
        implements OnClickListener {

    public static final String PREF_CONFIG = "prefConfig";
    private EditText mUsuarioEditText;
    private EditText mContrasenaEditText;
    private Spinner mCompaniaSpinner;
    private Button mIniciarSessionButton;
    private Button mAceptarButton;
    private ProgressDialog mProgress;
    // define SharedPreferences object
    private SharedPreferences mPrefLoginValues;
    private SharedPreferences mPrefControl;
    private SharedPreferences mPrefConfig;
    private SharedPreferences mPrefPedidoEnCurso;
    private SharedPreferences mPrefConexion;

    private String mAmbiente = "";
    private String mUsuario = "";
    private String mContrasena = "";
    private SpinnerEE mSelectedItem;

    //private QuickOrderDB mDB;
    private String mURLServer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // get references to widgets
        mUsuarioEditText = (EditText) findViewById(R.id.usuarioEditText);
        mContrasenaEditText = (EditText) findViewById(R.id.contrasenaEditText);
        mCompaniaSpinner = (Spinner) findViewById(R.id.companiaSpinner);
        mIniciarSessionButton = (Button) findViewById(R.id.iniciarSessionButton);
        mAceptarButton = (Button) findViewById(R.id.aceptarButton);

        // set allCaps
        mUsuarioEditText
                .setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        // hide 'Aceptar' button so it doesn't cover the 'Conectar' button
        mAceptarButton.setVisibility(View.GONE);
        mUsuarioEditText.setEnabled(true);
        mContrasenaEditText.setEnabled(true);

        // set listeners
        mIniciarSessionButton.setOnClickListener(this);
        mAceptarButton.setOnClickListener(this);

        SmartWaiter app = (SmartWaiter) getApplication();
        if (app.getFirstRun()) {
            // you code for the first run goes here;
            // here are first configured these values
            mPrefControl = getSharedPreferences(ControlSharedPref.NAME, MODE_PRIVATE);
            ControlSharedPref.save(mPrefControl, false, "", false, false, false, "",
                    false);

            // only do this if this is the first run
            app.setRunned();
        }
        // get SharedPreferences object
        mPrefLoginValues = getSharedPreferences(LoginSharedPref.NAME, MODE_PRIVATE);
        mPrefConfig = getSharedPreferences(PREF_CONFIG, MODE_PRIVATE);
        mPrefPedidoEnCurso = getSharedPreferences(PedidoSharedPref.PREFS_NAME,
                MODE_PRIVATE);
        mPrefConexion = getSharedPreferences(ConexionSharedPref.NAME, MODE_PRIVATE);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menuConfigurarConexion:
                new ConfigurarConexionDialogFragment().show(getFragmentManager(), "dlgConfigurarConex");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iniciarSessionButton:
                logInUsuario();
                break;
            case R.id.aceptarButton:
                // showMenuPrincipal();
                aceptarIngresar();
                break;
        }
    }

    public void logInUsuario() {
        if (RestUtil.datosConexionCompletos(getApplicationContext())) {
            if (!Funciones.isEditTextEmpty(mUsuarioEditText)) {
                //if (!Funciones.isEditTextEmpty(mContrasenaEditText)) {
                mURLServer = RestUtil.obtainURLServer(getApplicationContext());
                String url = mURLServer
                        + "Compartido/Conectar/?ambiente=%s&usuario=%s&clave=%s&cadenaConexion=%s";
                mAmbiente = mPrefConexion.getString(ConexionSharedPref.AMBIENTE, "");
                mUsuario = mUsuarioEditText.getText().toString().trim()
                        .toUpperCase(Locale.getDefault());
                mContrasena = mContrasenaEditText.getText().toString()
                        .trim();

                try {
                    String encondedAmbiente = URLEncoder.encode(mAmbiente,
                            "utf-8");
                    String urlWithParams = String.format(url,
                            "", mUsuario, mContrasena,
                            encondedAmbiente);
                    new DoLoginUsuario().execute(urlWithParams);
                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG)
                            .show();
                }
//                } else {
//                    Toast.makeText(this, "Debe ingresar una contraseña.",
//                            Toast.LENGTH_LONG).show();
//                }
            } else {
                Toast.makeText(this, "Debe ingresar un nombre de usuario.",
                        Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Datos de conexión incompletos.Por favor revise los datos y vuelva a intentarlo.",
                    Toast.LENGTH_LONG).show();
        }

    }

    // used to read the companies and insert all the price lists
    private void processResponse(String response) {

        Context context = LoginActivity.this;
        try {

            JsonObject jsonObjectResponse = (new JsonParser()).parse(response).getAsJsonObject();
            JsonArray jsonTablaOtros = jsonObjectResponse
                    .getAsJsonArray("TablaOtros");
            JsonObject jsonDatosAutenticacion = jsonTablaOtros.get(0).getAsJsonObject();
            boolean usuarioExite = jsonDatosAutenticacion.get("usuario").getAsBoolean();
            if (usuarioExite) {
                JsonArray jsonCompanyArray = jsonObjectResponse
                        .getAsJsonArray("TablaCompañias");
                if (jsonCompanyArray.size() > 0) {
                    showCompañias(jsonCompanyArray);

                } else {
                    limpiarLogin();
                    Toast.makeText(context, "El usuario no tiene compañias.",
                            Toast.LENGTH_LONG).show();
                }
            } else {
                limpiarLogin();
                Toast.makeText(context, "Usuario y/o contraseña no válidos.",
                        Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            limpiarLogin();
            Log.d(DBHelper.TAG, e.toString());
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    private void showCompañias(JsonArray jsonCompanyArray) throws Exception {
        Context context = LoginActivity.this;

        ArrayList<SpinnerEE> listaCompanias = parseCompañias(jsonCompanyArray);
        ArrayAdapter<SpinnerEE> adapter = new ArrayAdapter<SpinnerEE>(
                context, android.R.layout.simple_spinner_item, listaCompanias);
        mCompaniaSpinner.setAdapter(adapter);
        mIniciarSessionButton.setVisibility(View.GONE);
        mUsuarioEditText.setEnabled(false);
        mContrasenaEditText.setEnabled(false);
        mAceptarButton.setVisibility(View.VISIBLE);

        // save login data to SharedPreferences , clearing values first,
        // that's why we pass TRUE as the last parameter

        LoginSharedPref.save(mPrefLoginValues, mUsuario, mContrasena,
                null, null, null, true);
    }

    private ArrayList<SpinnerEE> parseCompañias(JsonArray jsonCompanyArray)
            throws Exception {
        ArrayList<SpinnerEE> listaCompanias = new ArrayList<SpinnerEE>();
        for (int i = 0; i < jsonCompanyArray.size(); i++) {
            JsonObject comp = jsonCompanyArray.get(i).getAsJsonObject();
            SpinnerEE item = new SpinnerEE();
            item.setCodigo(comp.get("codcia").getAsString());
            item.setDescripcion(comp.get("razonsocial").getAsString());
            listaCompanias.add(item);
        }
        return listaCompanias;
    }

    private void limpiarLogin() {
        //mUsuarioEditText.setText("");
        mContrasenaEditText.setText("");
    }

    public void aceptarIngresar() {

        mSelectedItem = (SpinnerEE) mCompaniaSpinner.getSelectedItem();

        String url = mURLServer
                + "compartido/aceptar/?usuario=%s&codcia=%s&cadenaConexion=%s";
        if (mUsuario != "" && mSelectedItem.getCodigo().trim() != "") {
            try {

                String encondedAmbiente = URLEncoder.encode(mAmbiente, "utf-8");
                String urlWithParams = String.format(url, mUsuario,
                        mSelectedItem.getCodigo().trim(), encondedAmbiente);
                new DoAceptar().execute(urlWithParams);
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void insertSettings(String response) {

        // insert Settings Preferences
        JsonArray jsArraySettings;
        try {
            // remove all the previous values
            Editor editor = mPrefConfig.edit();
            editor.clear();

            jsArraySettings = (new JsonParser()).parse(response).getAsJsonArray();
            for (int i = 0; i < jsArraySettings.size(); i++) {
                JsonObject jsObj = jsArraySettings.get(i).getAsJsonObject();
                String value = jsObj.get("Value").getAsString();
                String key = jsObj.get("Key").getAsString();
                editor.putString(key.trim(), value.trim());
            }
            editor.commit();

            // save company to Login Preferences
            // pass false so the previous values are not cleared out.
            LoginSharedPref.save(mPrefLoginValues, null, null, mSelectedItem.getDescripcion().trim(), true, false, false);
            Log.d("QuickOrder", "Configuraciones Insertadas");
            goToMainActivity();
        } catch (Exception e) {
            Toast.makeText(this, "Se produjó la excepción: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void goToMainActivity() {
        Intent intent;
//        int pedidoIdEnCurso = mPrefPedidoEnCurso.getInt(
//                PedidoSharedPref.PEDIDO_ID, 0);  //TODO REEPLAZAR POR ORIGINAL
        int pedidoIdEnCurso = 0;
        if (pedidoIdEnCurso > 0) {
            // If there's a Pedido en Curso, go and show that Pedido
            intent = new Intent(LoginActivity.this, TomarPedidoActivity.class);
            startActivity(intent);
        } else {
            // Otherwise go straight to MenuPrincipal
            intent = new Intent(LoginActivity.this, MesasActivity.class);
            startActivity(intent);
        }
    }

    class DoLoginUsuario extends AsyncTask<String, Void, Object> {

        @Override
        protected void onPreExecute() {
            mProgress = new ProgressDialog(LoginActivity.this);
            mProgress.setTitle("Procesando");
            mProgress.setMessage("Espere por favor...");
            mProgress.setCancelable(false);
            mProgress.show();
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
            String response;
            if (mProgress != null) {
                mProgress.dismiss();
            }
            if (result instanceof String) {
                response = (String) result;
                processResponse(response);
            } else if (result instanceof Exception) {
                if (result instanceof HttpResponseException) {
                    response = "Error en Conectar Ambiente";
                } else {
                    response = ((Exception) result).getMessage();
                }
                Log.d(DBHelper.TAG, "Error en Iniciar sesion: " + response);
                Toast.makeText(LoginActivity.this, response, Toast.LENGTH_LONG)
                        .show();
            }
        }

    }

    class DoAceptar extends AsyncTask<String, Void, Object> {
        @Override
        protected void onPreExecute() {
            mProgress = new ProgressDialog(LoginActivity.this);
            mProgress.setTitle("Procesando");
            mProgress.setMessage("Espere por favor...");
            mProgress.setCancelable(false);
            mProgress.show();
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
            String response;
            if (mProgress != null) {
                mProgress.dismiss();
            }
            if (result instanceof String) {
                response = (String) result;
                insertSettings(response);

            } else if (result instanceof Exception) {
                response = ((Exception) result).getMessage();
                Log.d(DBHelper.TAG, "Se produjó la excepción: " + response);
                Toast.makeText(LoginActivity.this, response, Toast.LENGTH_LONG)
                        .show();
            }
        }

    }


}
