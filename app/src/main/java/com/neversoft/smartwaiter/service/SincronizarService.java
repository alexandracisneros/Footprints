package com.neversoft.smartwaiter.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.neversoft.smartwaiter.database.SmartWaiterDB;
import com.neversoft.smartwaiter.io.RestConnector;
import com.neversoft.smartwaiter.io.RestUtil;
import com.neversoft.smartwaiter.model.business.ArticuloDAO;
import com.neversoft.smartwaiter.model.business.CartaDAO;
import com.neversoft.smartwaiter.model.business.CategoriaDAO;
import com.neversoft.smartwaiter.model.business.ClienteDAO;
import com.neversoft.smartwaiter.model.business.MesaPisoDAO;
import com.neversoft.smartwaiter.model.business.PrioridadDAO;
import com.neversoft.smartwaiter.util.Funciones;

/**
 * Created by Usuario on 03/09/2015.
 */
public class SincronizarService extends IntentService {
    public static final String ACTION_SYNC_DATA = "com.neversoft.smartwaiter.SYNC_DATA";
    private static final String NAME = "SincronizarService";
    private Exception miExcepcion = null;
    private boolean exito = false;
    private String mensaje = "";

    public SincronizarService() {
        super(NAME);
        // We don’t want intents redelivered
        // in case we’re shut down unexpectedly
        setIntentRedelivery(false);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
//        // get NetworkInfo object
//        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
//
//        // if network is connected, download data
//
//        if (networkInfo != null && networkInfo.isConnected()) {
//            Ion.with(getApplicationContext())
//                    .load(RestUtil.URLServer
//                            + "ObtenerDatosIniciales/?usuario=SUPERVISOR&codCia=001&cadenaConexion=Initial%20Catalog=ABR")
//                    .asJsonObject().setCallback(this);
//        } else {
//            mensaje = "Imposible conectarse a Internet.";
//            exito = false;
//            enviarNotificacion();
//        }
        try {
            Log.d(SmartWaiterDB.TAG,
                    "INICIA Insercion DataSincronizada: "
                            + Funciones
                            .getCurrentDate("yyyy/MM/dd hh:mm:ss"));
            leerDataWebService();
            Log.d(SmartWaiterDB.TAG,
                    "FINALIZA Insercion DataSincronizada: "
                            + Funciones
                            .getCurrentDate("yyyy/MM/dd hh:mm:ss"));
        } catch (Exception e) {

            mensaje = e.getMessage();
            exito = false;
        }
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(SincronizarService.ACTION_SYNC_DATA);

        broadcastIntent.putExtra("exito", exito);
        broadcastIntent.putExtra("resultado", "2");
        broadcastIntent.putExtra("mensaje", mensaje);

        Log.d(SmartWaiterDB.TAG, "Mesaje Final Sincronizacion: " + mensaje);

        sendOrderedBroadcast(broadcastIntent, null);

    }

    private int leerDataWebService() throws Exception {
        Object requestObject = null;
        String result = "";
        int cantidadInsertados = 0;
        // Only download data related to Customer if it hasn't been downloaded yet
        try {


            String url = RestUtil.URLServer + "ObtenerDatosIniciales/?usuario=SUPERVISOR&codCia=001&cadenaConexion=Initial%20Catalog=PRUEBAMOVILJHAV";

            Log.d(SmartWaiterDB.TAG, url);
            if (Funciones.hasActiveInternetConnection(getApplicationContext())) {
                RestConnector restConnector = RestUtil.obtainGetConnection(url);
                requestObject = restConnector.doRequest(url);

                if (requestObject instanceof String) {
                    // Only if the request was successful parse the returned value otherwise re-throw the exception
                    result = (String) requestObject;
                    Gson gson = new Gson();
                    JsonObject jsonObjectResponse = gson.fromJson(result, JsonObject.class);
                    cantidadInsertados = saveSyncDataToDB(jsonObjectResponse);
                    Log.d(SmartWaiterDB.TAG,
                            "Fin Insercion DataSincronizada Cantidad: " + cantidadInsertados);
                    return cantidadInsertados;

                } else if (requestObject instanceof Exception) {
                    Log.d(SmartWaiterDB.TAG, "Error al guardar data de sincronizacion: "
                            + ((Exception) requestObject).getMessage());
                    throw new Exception(((Exception) requestObject).getMessage());
                }
            }

        } catch (Exception ex) {
            // if something goes wrong delete all customers that could have been saved.
            //mDB.dropDataSincronizada(0);
            throw ex;
        }
        return cantidadInsertados;

    }

    private int saveSyncDataToDB(JsonObject jsonObjectResponse) throws Exception {
        ArticuloDAO articuloDAO = new ArticuloDAO(getApplicationContext());
        CategoriaDAO categoriaDAO = new CategoriaDAO(getApplicationContext());
        PrioridadDAO prioridadDAO = new PrioridadDAO(getApplicationContext());
        MesaPisoDAO mesaPisoDAO = new MesaPisoDAO(getApplicationContext());
        CartaDAO cartaDAO = new CartaDAO(getApplicationContext());
        ClienteDAO clienteDAO = new ClienteDAO(getApplicationContext());
        int nroClientes;
        JsonArray jsonArray;
        jsonArray = jsonObjectResponse.getAsJsonArray("tablaFamilia");
        exito = (categoriaDAO.saveCategoriaData(jsonArray) > 0);
        jsonArray = jsonObjectResponse.getAsJsonArray("tablaPrioridad");
        exito = (prioridadDAO.savePrioridadData(jsonArray) > 0);
        jsonArray = jsonObjectResponse.getAsJsonArray("tablaMesa");
        exito = (mesaPisoDAO.saveMesaData(jsonArray) > 0);
        jsonArray = jsonObjectResponse.getAsJsonArray("tablaCarta");
        exito = (cartaDAO.saveCartaData(jsonArray) > 0);
        jsonArray = jsonObjectResponse.getAsJsonArray("tablaCliente");
        nroClientes = clienteDAO.saveClienteData(jsonArray);
        exito = (nroClientes > 0);
        jsonArray = jsonObjectResponse.getAsJsonArray("tablaArticuloPrecio");
        exito = (articuloDAO.saveArticuloPrecioData(jsonArray) > 0);

        return nroClientes;
    }
}
