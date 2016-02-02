package com.neversoft.smartwaiter.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
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
import com.neversoft.smartwaiter.service.ActualizarEstadoMesaService;
import com.neversoft.smartwaiter.util.Funciones;

import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class ConsultarReservasActivity extends Activity
        implements AdapterView.OnItemClickListener, View.OnClickListener {
    private EditText mIdClienteEditText;
    private EditText mCodigoReservaEditText;
    private TextView mRazonSocialBusqTextView;
    private TextView mIDClieBusqTextView;
    private ImageButton mBuscarReservaImageButton;
    private GridView mMesasGridView;
    private ListView mMenuListView;
    private ArrayList<MesaPisoEE> mMesaPisoLista;
    private String mColorReserva;
    private MesaPisoEE mMesaPisoSeleccionado;

    private String mUrlServer;
    private SharedPreferences mPrefConfig;
    private SharedPreferences mPrefConexion;
    private FrameLayout mIndicatorFrameLayout;
    private LinearLayout mMainLinearLayout;

    private BroadcastReceiver onEventActualizarEstadoMesa = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean resultadoOperacion = intent.getBooleanExtra(ActualizarEstadoMesaService.EXTRA_RESULTADO_ACTUALIZACION, false);
            //ANTES DE ESTO DEBERIA HABERSE UTILIZANDO UN LOADING QUE NO DEJE SELECCIONAR NADA MAS PARA PODER REFRESCAR LOS ITEMS PREVIAMENTE SELECCIONADOS
            if (resultadoOperacion) {
                Toast.makeText(ConsultarReservasActivity.this, "Resultado de Actualizar Estado: " + resultadoOperacion, Toast.LENGTH_SHORT).show();

                String[] params = {String.valueOf(mMesaPisoSeleccionado.getId()),
                        String.valueOf(mMesaPisoSeleccionado.getCodReserva())};
                new ActualizarMesa_Reserva().execute(params);

            } else {
                showProgressIndicator(false);
                String response = "Error";
                //response = ((Exception) result).getMessage();
                Log.d(DBHelper.TAG, "Se produjó la excepción: " + response);
                Toast.makeText(ConsultarReservasActivity.this, response, Toast.LENGTH_LONG)
                        .show();
            }
        }
    };

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

        mRazonSocialBusqTextView = (TextView) findViewById(R.id.razonSocialBusqTextView);
        mIDClieBusqTextView = (TextView) findViewById(R.id.IDClieBusqTextView);

        mIdClienteEditText = (EditText) findViewById(R.id.idClienteEditText);
        mCodigoReservaEditText = (EditText) findViewById(R.id.codReservaEditText);
        mBuscarReservaImageButton = (ImageButton) findViewById(R.id.buscarClieImageButton);
        mBuscarReservaImageButton.setOnClickListener(this);

        mMesasGridView = (GridView) findViewById(R.id.mesasGridView);
        mMesasGridView.setOnItemClickListener(this);

        mUrlServer = RestUtil.obtainURLServer(getApplicationContext());
        mPrefConfig = getSharedPreferences(LoginActivity.PREF_CONFIG, MODE_PRIVATE);
        mPrefConexion = getSharedPreferences(ConexionSharedPref.NAME, MODE_PRIVATE);
        mColorReserva = "#3333ff";

        mIndicatorFrameLayout = (FrameLayout) findViewById(R.id.loadingIndicatorLayout);
        mMainLinearLayout = (LinearLayout) findViewById(R.id.mainLinearLayout);

    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filterNotificarRecojo = new IntentFilter(ActualizarEstadoMesaService.ACTION_UPDATE_TABLE_STATUS);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(onEventActualizarEstadoMesa, filterNotificarRecojo);
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(onEventActualizarEstadoMesa);
        super.onPause();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v,
                            int position, long id) {
        if (parent.getId() == R.id.mesasGridView) {
            mMesaPisoSeleccionado = mMesaPisoLista.get(position);
            confirmarActualizarEstadoMesa(mMesaPisoSeleccionado);


        } else if (parent.getId() == R.id.menu_listview) {
            if (position != SmartWaiter.OPCION_RESERVAS) {
                WeakReference<Activity> weakActivity = new WeakReference<Activity>(this);
                Funciones.selectMenuOption(weakActivity, position);
            }
        }
    }

    private void confirmarActualizarEstadoMesa(final MesaPisoEE mesaPisoEE) {

        new AlertDialog.Builder(this)
                .setTitle("Confirmación")
                .setMessage("¿Realmente desea proceder a efectuar un pedido sobre la mesa :" + mesaPisoEE.getNroMesa() + " ?")
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Start daily operations
                        dialog.cancel();
                        Gson gson = new Gson();
                        String mesaString = gson.toJson(mesaPisoEE);
                        Intent serviceIntent = new Intent(ConsultarReservasActivity.this,
                                ActualizarEstadoMesaService.class);
                        serviceIntent.putExtra(ActualizarEstadoMesaService.EXTRA_TABLE, mesaString);
                        serviceIntent.putExtra(ActualizarEstadoMesaService.EXTRA_CLASS_NAME, this.getClass().getName());
                        Log.d(DBHelper.TAG, "Antes de startService ActualizarEstadoMesaService");
                        showProgressIndicator(true);
                        startService(serviceIntent);

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

    private void showProgressIndicator(boolean showValue) {
        if (showValue) {
            mMainLinearLayout.setVisibility(View.GONE);
            mIndicatorFrameLayout.setVisibility(View.VISIBLE);
        } else {
            mMainLinearLayout.setVisibility(View.VISIBLE);
            mIndicatorFrameLayout.setVisibility(View.GONE);
        }
    }

    private class BuscarMesaRerservada extends AsyncTask<String, Void, Object> {
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

                    mRazonSocialBusqTextView.setText(clienteEE.getRazonSocial());
                    mIDClieBusqTextView.setText(clienteEE.getNroDocumento());

                    String idReserva = (mCodigoReservaEditText.getText().toString().trim().equals("") ? null : mCodigoReservaEditText.getText().toString().trim());
                    String nroID = (mIdClienteEditText.getText().toString().trim().equals("") ? null : mIdClienteEditText.getText().toString().trim());


                    Object[] params = {jsonReservas, clienteEE.getNroDocumento(), idReserva, nroID};
                    new InsertarActualizarReservadas().execute(params);

                } else {
                    mMesaPisoLista = new ArrayList<>();
                    mMesasGridView.setAdapter(new MesaItemAdapter(ConsultarReservasActivity.this, mMesaPisoLista, "RES"));
                    showProgressIndicator(false);
                }

            } else if (result instanceof Exception) {
                showProgressIndicator(false);
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
            showProgressIndicator(false);
            if (result instanceof List<?>) {
                mMesaPisoLista = (ArrayList<MesaPisoEE>) result;
                mMesasGridView.setAdapter(new MesaItemAdapter(ConsultarReservasActivity.this, mMesaPisoLista, "RES"));
            } else if (result instanceof Exception) {
                String response;
                response = ((Exception) result).getMessage();
                Log.d(DBHelper.TAG, "Se produjó la excepción: " + response);
                Toast.makeText(ConsultarReservasActivity.this, response, Toast.LENGTH_LONG)
                        .show();
            }
        }

    }

    private class ActualizarMesa_Reserva extends AsyncTask<String, Void, Object> {
        @Override
        protected Object doInBackground(String... params) {


            Object requestObject;
            String idMesa = params[0];
            String idReservaLocal = params[1];
            try {

                MesaPisoDAO mesaPisoDAO = new MesaPisoDAO(getApplicationContext());
                requestObject = mesaPisoDAO.updateEstadoMesaYReserva(Integer.parseInt(idMesa), Integer.parseInt(idReservaLocal),
                        "OCU", "EFE");
            } catch (Exception e) {
                requestObject = e;
            }
            return requestObject;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (result instanceof Integer) {
                Intent intent = new Intent(ConsultarReservasActivity.this, TomarPedidoActivity.class);
                intent.putExtra(TomarPedidoActivity.EXTRA_PREVIOUS_ACTIVITY_CLASS, ConsultarReservasActivity.this.getClass().getName());
                startActivity(intent);
                showProgressIndicator(false); //<----TODO ACA ME QUEDE DEBERIA LIMPIAR ANTES DE TODO PORQUE SE QUEDA CON LA ANTERIOR BUSQUEDA
                //finish(); //TODO: Revisar si Tomar pedido deberia mostrar el menu lateral o no segun lineamientos de android
            } else if (result instanceof Exception) {
                showProgressIndicator(false);
                String response;
                response = ((Exception) result).getMessage();
                Log.d(DBHelper.TAG, "Se produjó la excepción: " + response);
                Toast.makeText(ConsultarReservasActivity.this, response, Toast.LENGTH_LONG)
                        .show();
            }
        }

    }
}
