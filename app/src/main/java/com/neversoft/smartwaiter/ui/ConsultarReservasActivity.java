package com.neversoft.smartwaiter.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.neversoft.smartwaiter.R;
import com.neversoft.smartwaiter.database.DBHelper;
import com.neversoft.smartwaiter.io.RestConnector;
import com.neversoft.smartwaiter.io.RestUtil;
import com.neversoft.smartwaiter.model.entity.ClienteEE;
import com.neversoft.smartwaiter.model.entity.MesaPisoEE;
import com.neversoft.smartwaiter.preference.ConexionSharedPref;
import com.neversoft.smartwaiter.preference.PedidoExtraSharedPref;
import com.neversoft.smartwaiter.service.ActualizarEstadoMesaService;
import com.neversoft.smartwaiter.util.Funciones;

import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.ArrayList;

;

public class ConsultarReservasActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener,
        View.OnClickListener,
        NavigationView.OnNavigationItemSelectedListener {
    private EditText mIdClienteEditText;
    private EditText mCodigoReservaEditText;
    private TextView mRazonSocialBusqTextView;
    private TextView mIDClieBusqTextView;
    private ImageButton mBuscarReservaImageButton;
    private GridView mMesasGridView;
    private NavigationView mNavigationView;
    private ArrayList<MesaPisoEE> mMesaPisoLista;
    private String mColorReserva;
    private MesaPisoEE mMesaPisoSeleccionado;

    private String mUrlServer;
    private SharedPreferences mPrefConfig;
    private SharedPreferences mPrefConexion;
    private FrameLayout mIndicatorFrameLayout;
    private LinearLayout mMainLinearLayout;
    private SharedPreferences mPrefPedidoExtras;

    private BroadcastReceiver onEventActualizarEstadoMesa = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int resultadoOperacion = intent.getIntExtra(ActualizarEstadoMesaService.EXTRA_RESULTADO_ACTUALIZACION, 0);
            String mensajeOperacion = intent.getStringExtra(ActualizarEstadoMesaService.EXTRA_MENSAJE_ACTUALIZACION);
            if (resultadoOperacion > 0) {
                Log.d(DBHelper.TAG, "Resultado de Actualizar Estado de Mesa: " + resultadoOperacion);

                Intent intentTo = new Intent(ConsultarReservasActivity.this, TomarPedidoActivity.class);
                startActivity(intentTo);
                finish(); // finaliza actividad para que al volver necesariamente se tenga que volver a cargar la actividad
            } else {
                Log.d(DBHelper.TAG, "Se produjó la excepción: " + mensajeOperacion);
                Toast.makeText(ConsultarReservasActivity.this, mensajeOperacion, Toast.LENGTH_LONG).show();
                showProgressIndicator(false);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultar_reservas);

        overridePendingTransition(0, 0);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // get reference to the ListView and set its listener
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.getMenu().getItem(SmartWaiter.OPCION_RESERVAS).setChecked(true);

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

        mPrefPedidoExtras = getSharedPreferences(PedidoExtraSharedPref.NAME, MODE_PRIVATE);

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
            confirmarActualizarEstadoMesa();
        }
    }

    private void confirmarActualizarEstadoMesa() {

        new AlertDialog.Builder(this)
                .setTitle("Confirmación")
                .setMessage("¿Realmente desea proceder a efectuar un pedido sobre la mesa :" + mMesaPisoSeleccionado.getNroMesa() + " ?")
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                        Gson gson = new Gson();
                        String mesaString = gson.toJson(mMesaPisoSeleccionado);
                        Intent serviceIntent = new Intent(ConsultarReservasActivity.this, ActualizarEstadoMesaService.class);
                        //Put Extras
                        serviceIntent.putExtra(ActualizarEstadoMesaService.EXTRA_NUEVO_ESTADO_MESA, "OCU");
                        serviceIntent.putExtra(ActualizarEstadoMesaService.EXTRA_NUEVO_ESTADO_RESERVA, "EFE");

                        //Store extra info as preference
                        PedidoExtraSharedPref.save(mPrefPedidoExtras, mesaString, ConsultarReservasActivity.this.getClass().getName());

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
                    String encondedAmbiente = URLEncoder.encode(ambiente, "utf-8");
                    String urlWithParams = String.format(url, idReserva, nroID, codCia, encondedAmbiente);
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

    private ArrayList<MesaPisoEE> parseJsonToMesaPiso(JsonArray jsonMesasReservadas, String idClienteReserva) {
        ArrayList<MesaPisoEE> mesaPisoLista = new ArrayList<>();
        MesaPisoEE mesaPiso;
        JsonObject mesaJsonObject;
        for (JsonElement element : jsonMesasReservadas) {
            mesaJsonObject = element.getAsJsonObject();
            mesaPiso = new MesaPisoEE();
            mesaPiso.setId(mesaJsonObject.get("CODMESA").getAsInt());
            mesaPiso.setNroPiso(mesaJsonObject.get("NROPISO").getAsInt());
            mesaPiso.setCodAmbiente(mesaJsonObject.get("CAMBIENTE").getAsInt());
            mesaPiso.setDescAmbiente(mesaJsonObject.get("DAMBIENTE").getAsString());
            mesaPiso.setNroMesa(mesaJsonObject.get("NROMESA").getAsInt());
            mesaPiso.setNroAsientos(mesaJsonObject.get("NROASIENTOS").getAsInt());
            mesaPiso.setCodEstado(mesaJsonObject.get("CEMESA").getAsString());
            mesaPiso.setDescEstado(mesaJsonObject.get("DEMESA").getAsString());
            mesaPiso.setCodReserva(mesaJsonObject.get("CODRESERVA").getAsInt());
            mesaPiso.setHTMLColor("#0099FF"); //Temporal //TODO Solicitar a Alex que cruce data y te traiga ese valor
            mesaPiso.setIDCliente(idClienteReserva);
            mesaPisoLista.add(mesaPiso);
        }
        return mesaPisoLista;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        if (menuItem.getOrder() != SmartWaiter.OPCION_RESERVAS) {
            WeakReference<Activity> weakActivity = new WeakReference<Activity>(ConsultarReservasActivity.this);
            Funciones.selectMenuOption(weakActivity, menuItem.getOrder());
            return true;
        }
        return true;
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
            mMesaPisoLista = new ArrayList<>();
            if (result instanceof String) {
                String stringObject = (String) result;
                Gson gson = new Gson();
                ClienteEE clienteEE = new ClienteEE();
                JsonArray jsonArray = gson.fromJson(stringObject, JsonArray.class);
                if (jsonArray != null && jsonArray.size() > 0) {
                    JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();

                    clienteEE.setNroDocumento(jsonObject.get("NROID").getAsString());
                    clienteEE.setRazonSocial(jsonObject.get("RAZONSOCIAL").getAsString());
                    JsonArray jsonMesasReservadas = jsonObject.getAsJsonArray("detalle");

                    mMesaPisoLista = parseJsonToMesaPiso(jsonMesasReservadas, clienteEE.getNroDocumento());

                }
                mRazonSocialBusqTextView.setText(clienteEE.getRazonSocial());
                mIDClieBusqTextView.setText(clienteEE.getNroDocumento());
                mMesasGridView.setAdapter(new MesaItemAdapter(ConsultarReservasActivity.this, mMesaPisoLista, "RES"));


            } else if (result instanceof Exception) {
                String response;
                response = ((Exception) result).getMessage();
                Log.d(DBHelper.TAG, "Se produjó la excepción: " + response);
                Toast.makeText(ConsultarReservasActivity.this, response, Toast.LENGTH_LONG).show();
            }
            showProgressIndicator(false);
        }

    }


//                //TODO  <---- 15/02/2016 12:13 am
////                1) Eliminar tabla reserva
////                2) Con la data que llega desde el webservice http://qa.siempresoft.com/pruebamovilalex/api/restaurante/ObtenerClienteReserva/?idReserva=0&nroID=20486245027&codCia=001&cadenaConexion=Initial+Catalog%3Dpruebamoviljhav
////                   Actualizar registros de la tabla Mesa :codeReserva, idClieente
////                3) Leer solo de Tabla Mesas y mostrar pero que solo se haga click en mesas reservadas

}
