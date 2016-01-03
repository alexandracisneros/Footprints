package com.neversoft.smartwaiter.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.neversoft.smartwaiter.R;
import com.neversoft.smartwaiter.database.DBHelper;
import com.neversoft.smartwaiter.io.RestConnector;
import com.neversoft.smartwaiter.io.RestUtil;
import com.neversoft.smartwaiter.model.business.MesaPisoDAO;
import com.neversoft.smartwaiter.model.business.ReservaDAO;
import com.neversoft.smartwaiter.model.entity.ClienteEE;
import com.neversoft.smartwaiter.model.entity.MesaPisoEE;
import com.neversoft.smartwaiter.preference.ConexionSharedPref;
import com.neversoft.smartwaiter.util.Funciones;

import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class ConsultarReservasActivity extends Activity
        implements AdapterView.OnItemClickListener, View.OnClickListener {
    private EditText mIdClienteEditText;
    private EditText mCodigoReservaEditText;
    private ImageButton mBuscarReservaImageButton;
    private GridView mMesasGridView;
    private ListView mMenuListView;
    private ArrayList<MesaPisoEE> mMesaPisoLista;
    private String mColorReserva;

    private String mUrlServer;
    private SharedPreferences mPrefConfig;
    private SharedPreferences mPrefConexion;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultar_reservas);

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
        mMenuListView.setItemChecked(SmartWaiter.OPCION_RESERVAS, true);

        mIdClienteEditText = (EditText) findViewById(R.id.idClienteEditText);
        mCodigoReservaEditText = (EditText) findViewById(R.id.codReservaEditText);
        mBuscarReservaImageButton = (ImageButton) findViewById(R.id.buscarClieImageButton);
        mBuscarReservaImageButton.setOnClickListener(this);

        mMesasGridView = (GridView) findViewById(R.id.mesasGridView);
        //mMesasGridView.setOnItemClickListener(this);

        mUrlServer = RestUtil.obtainURLServer(getApplicationContext());
        mPrefConfig = getSharedPreferences(LoginActivity.PREF_CONFIG, MODE_PRIVATE);
        mPrefConexion = getSharedPreferences(ConexionSharedPref.NAME, MODE_PRIVATE);
        mColorReserva = "#3333ff";

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v,
                            int position, long id) {
        if (parent.getId() == R.id.mesasGridView) {
            Intent intent = new Intent(this, TomarPedidoActivity.class);
            startActivity(intent);
        } else if (parent.getId() == R.id.menu_listview) {
            if (position != SmartWaiter.OPCION_RESERVAS) {
                WeakReference<Activity> weakActivity = new WeakReference<Activity>(this);
                Funciones.selectMenuOption(weakActivity, position);
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.buscarClieImageButton) {
            buscarMesa();
        }
    }

    private void buscarMesa() {
        String url = mUrlServer
                + "restaurante/ObtenerClienteReserva/?idReserva=%s&nroID=%s&codCia=%s&cadenaConexion=%s";

        String idReserva = (mCodigoReservaEditText.getText().toString().trim().equals("") ? "0" : mCodigoReservaEditText.getText().toString().trim());
        String nroID = mIdClienteEditText.getText().toString().trim();
        String codCia = mPrefConfig.getString("CodCia", "");
        String ambiente = mPrefConexion.getString(ConexionSharedPref.AMBIENTE, "");
        Log.d(DBHelper.TAG, ambiente);
        try {
            // Simple GET
            String mensajeError = "";
            if (!(nroID.equals("") && idReserva.equals("0"))) {
                if (codCia != "") {
                    String encondedAmbiente = URLEncoder.encode(ambiente,
                            "utf-8");
                    String urlWithParams = String.format(url, idReserva, nroID,
                            codCia, encondedAmbiente);
                    new BuscarMesaRerservada().execute(urlWithParams);

                } else {
                    mensajeError = "No se ha configurado 'código de compañía'";
                }
            } else {
                mensajeError = "Debe ingresar por lo menos un criterio de búsqueda.";
            }


            if (mensajeError != "") {
                throw new Exception(mensajeError);
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private class BuscarMesaRerservada extends AsyncTask<String, Void, Object> {
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
            mMesaPisoLista = new ArrayList<>();
            if (result instanceof String) {
                String stringObject = (String) result;
                Gson gson = new Gson();
                JsonArray jsonArray = gson.fromJson(stringObject, JsonArray.class);
                if (jsonArray != null && jsonArray.size() > 0) {
                    JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
                    ClienteEE clienteEE = new ClienteEE();
                    clienteEE.setNroDocumento(jsonObject.get("nroID").getAsString());
                    clienteEE.setRazonSocial(jsonObject.get("razonSocial").getAsString());
                    JsonArray jsonReservas = jsonObject.getAsJsonArray("detalle");


                    String idReserva = (mCodigoReservaEditText.getText().toString().trim().equals("") ? null : mCodigoReservaEditText.getText().toString().trim());
                    String nroID = (mIdClienteEditText.getText().toString().trim().equals("") ? null : mIdClienteEditText.getText().toString().trim());


                    Object[] params = {jsonReservas, clienteEE.getNroDocumento(), idReserva, nroID};
                    new InsertarActualizarReservadas().execute(params);

                } else {
                    mMesaPisoLista = new ArrayList<>();
                    mMesasGridView.setAdapter(new MesaItemAdapter(ConsultarReservasActivity.this, mMesaPisoLista));
                }

            } else if (result instanceof Exception) {
                String response;
                response = ((Exception) result).getMessage();
                Log.d(DBHelper.TAG, "Se produjó la excepción: " + response);
                Toast.makeText(ConsultarReservasActivity.this, response, Toast.LENGTH_LONG)
                        .show();
            }
        }

    }

    private class InsertarActualizarReservadas extends AsyncTask<Object, Void, Object> {
        @Override
        protected Object doInBackground(Object... params) {
            Object requestObject;
            JsonArray jsonReserva = (JsonArray) params[0];
            String idCliente = (String) params[1];
            String idReservaLocal = (String) params[2];
            String idClienteLocal = (String) params[3];
            try {
                ReservaDAO reservaDAO = new ReservaDAO(getApplicationContext());
                MesaPisoDAO mesaPisoDAO = new MesaPisoDAO(getApplicationContext());
                reservaDAO.insertOrUpdateReservadas(jsonReserva, idCliente);
                requestObject = mesaPisoDAO.getListaMesasReservadas(idReservaLocal, idClienteLocal);
            } catch (Exception e) {
                requestObject = e;
            }
            return requestObject;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (result instanceof List<?>) {
                mMesaPisoLista = (ArrayList<MesaPisoEE>) result;
                mMesasGridView.setAdapter(new MesaItemAdapter(ConsultarReservasActivity.this, mMesaPisoLista));
            } else if (result instanceof Exception) {
                String response;
                response = ((Exception) result).getMessage();
                Log.d(DBHelper.TAG, "Se produjó la excepción: " + response);
                Toast.makeText(ConsultarReservasActivity.this, response, Toast.LENGTH_LONG)
                        .show();
            }
        }

    }
}
