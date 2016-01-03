package com.neversoft.smartwaiter.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

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

public class IniciarDiaActivity extends Activity
        implements View.OnClickListener, AdapterView.OnItemClickListener {
    private ListView mMenuListView;
    private Button mIniciarDiaButton;
    private String mUrlServer;
    private SharedPreferences mPrefControl;
    private SharedPreferences mPrefConfig;
    private SharedPreferences mPrefConexion;
    private ProgressDialog mProgress;
    private String mFechaInicioDia;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iniciar_dia);
        overridePendingTransition(0, 0);
        // get SharedPreferences
        mPrefConfig = getSharedPreferences(LoginActivity.PREF_CONFIG, MODE_PRIVATE);
        mPrefControl = getSharedPreferences(ControlSharedPref.NAME, MODE_PRIVATE);
        mPrefConexion = getSharedPreferences(ConexionSharedPref.NAME, MODE_PRIVATE);

        mIniciarDiaButton = (Button) findViewById(R.id.iniciarDiaButton);
        mIniciarDiaButton.setOnClickListener(this);
        mMenuListView = (ListView) findViewById(R.id.menu_listview);
        mMenuListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mMenuListView.setOnItemClickListener(this);

        mUrlServer = RestUtil.obtainURLServer(getApplicationContext());

        Resources res = getResources();
        String[] options = res.getStringArray(R.array.menu_items_array);
        ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, options);
        mMenuListView.setAdapter(itemsAdapter);
        mMenuListView.setItemChecked(SmartWaiter.OPCION_INICIAR_DIA, true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_iniciar_dia, menu);
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
                }).setIcon(android.R.drawable.ic_dialog_alert).show();

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v,
                            int position, long id) {
        if (parent.getId() == R.id.menu_listview) {
            if (position != SmartWaiter.OPCION_INICIAR_DIA) {
                WeakReference<Activity> weakActivity = new WeakReference<Activity>(this);
                Funciones.selectMenuOption(weakActivity, position);
            }
        }
    }

    public void iniciarDia() {
        String url = mUrlServer
                + "ventas/IniciarDiaVendedorMV/?fecha=%s&codVen=%s&codCia=%s&usuario=%s&cadenaConexion=%s";
        mFechaInicioDia = Funciones.getCurrentDate("yyyy/MM/dd");
        String codMozo = mPrefConfig.getString("CodMozo", "");
        String codCia = mPrefConfig.getString("CodCia", "");
        String usuario = mPrefConfig.getString("Usuario", "").toUpperCase(
                Locale.getDefault());
        String ambiente = mPrefConexion.getString(ConexionSharedPref.AMBIENTE, "");
        Log.d(DBHelper.TAG, ambiente);
        try {
            // Simple GET
            String mensajeError = "";
            if (codMozo != "") {
                if (codCia != "") {
                    if (usuario != "") {
                        String encondedAmbiente = URLEncoder.encode(ambiente,
                                "utf-8");
                        String urlWithParams = String.format(url,
                                mFechaInicioDia, codMozo, codCia, usuario,
                                encondedAmbiente);
                        new DoIniciarDia().execute(urlWithParams);
                    } else {
                        mensajeError = "No se ha configurado 'código de vendedor'";
                    }
                } else {
                    mensajeError = "No se ha configurado 'código de compañía'";
                }
            } else {
                mensajeError = "No se ha configurado 'código de mozo'";
            }
            if (mensajeError != "") {
                throw new Exception(mensajeError);
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    class DoIniciarDia extends AsyncTask<String, Void, Object> {
        @Override
        protected void onPreExecute() {
            mProgress = new ProgressDialog(IniciarDiaActivity.this);
            mProgress.setTitle("Procesando");
            mProgress.setCancelable(false);
            mProgress.setMessage("Espere por favor...");
            mProgress.show();
        }

        @Override
        protected Object doInBackground(String... params) {
            Object requestObject = null;
            String url = params[0];
            Log.d(DBHelper.TAG, url);
            RestConnector restConnector;
            try {
                if (Funciones
                        .hasActiveInternetConnection(getApplicationContext())) {
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
            String response = "";
            if (mProgress != null) {
                mProgress.dismiss();
            }
            if (result instanceof String) {
                response = (String) result;
                if (response.equals("1")) {
                    // update login data to SharedPreferences
                    // We only want to change the value for 'inicioDia'
                    // We DO NOT want to change any of the other values
                    ControlSharedPref.save(mPrefControl, true, mFechaInicioDia,
                            null, null, null, null, false);
                    response = "Día iniciado correctamente.";
                } else if (response.equals("2")) {
                    response = "No se pudo iniciar el día porque existe un registro" +
                            " anterior que aún no ha sido cerrado.";
                }

            } else if (result instanceof Exception) {
                response = ((Exception) result).getMessage();
                Log.d("QuickOrder", response);
            }
            Toast.makeText(IniciarDiaActivity.this, response,
                    Toast.LENGTH_LONG).show();
        }

    }


}
