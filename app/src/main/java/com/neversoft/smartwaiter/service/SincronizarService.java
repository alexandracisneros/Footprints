package com.neversoft.smartwaiter.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.neversoft.smartwaiter.database.DBHelper;
import com.neversoft.smartwaiter.io.RestConnector;
import com.neversoft.smartwaiter.io.RestUtil;
import com.neversoft.smartwaiter.model.business.ArticuloDAO;
import com.neversoft.smartwaiter.model.business.CartaDAO;
import com.neversoft.smartwaiter.model.business.CategoriaDAO;
import com.neversoft.smartwaiter.model.business.ClienteDAO;
import com.neversoft.smartwaiter.model.business.ConceptoDAO;
import com.neversoft.smartwaiter.model.business.MesaInfoDAO;
import com.neversoft.smartwaiter.model.business.MesaPisoDAO;
import com.neversoft.smartwaiter.model.business.PrioridadDAO;
import com.neversoft.smartwaiter.model.business.SincroDAO;
import com.neversoft.smartwaiter.preference.ConexionSharedPref;
import com.neversoft.smartwaiter.preference.ControlSharedPref;
import com.neversoft.smartwaiter.preference.LoginSharedPref;
import com.neversoft.smartwaiter.ui.LoginActivity;
import com.neversoft.smartwaiter.util.Funciones;

import java.net.URLEncoder;

/**
 * Created by Usuario on 03/09/2015.
 */
public class SincronizarService extends IntentService {
    public static final String ACTION_SYNC_DATA = "com.neversoft.smartwaiter.SYNC_DATA";
    public static final String EXTRA_RESULTADO_EXITO = "resut_exito";
    public static final String EXTRA_RESULTADO_MENSAJE = "result_mesnaje";
    private static final String NAME = "SincronizarService";
    private boolean exito;
    private String mensaje = "";
    private String mUrlServer;

    private String mAmbiente;
    private String mCodCia;
    private String mUsuario;

    // define SharedPreferences object

    private SharedPreferences mPrefConfig;
    private SharedPreferences mPrefLogin;
    private SharedPreferences mPrefConexion;
    private SharedPreferences mPrefControl;
    private SincroDAO mSincroDAO;

    public SincronizarService() {
        super(NAME);
        // We don’t want intents redelivered
        // in case we’re shut down unexpectedly
        setIntentRedelivery(false);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // get SharedPreferences

        mPrefConfig = getApplicationContext().getSharedPreferences(LoginActivity.PREF_CONFIG, Context.MODE_PRIVATE);
        mPrefLogin = getApplication().getSharedPreferences(LoginSharedPref.NAME, Context.MODE_PRIVATE);
        mPrefConexion = getApplication().getSharedPreferences(ConexionSharedPref.NAME, Context.MODE_PRIVATE);
        mPrefControl = getSharedPreferences(ControlSharedPref.NAME, MODE_PRIVATE);
        mSincroDAO = new SincroDAO(getApplicationContext());

        mAmbiente = mPrefConexion.getString(ConexionSharedPref.AMBIENTE, "");
        mCodCia = mPrefConfig.getString("CodCia", "");
        mUsuario = mPrefLogin.getString(LoginSharedPref.USUARIO, "");

        mUrlServer = RestUtil.obtainURLServer(getApplicationContext());
        try {
            mAmbiente = URLEncoder.encode(mAmbiente, "utf-8");
            Log.d(DBHelper.TAG, "INICIA Insercion DataSincronizada: " + Funciones.getCurrentDate("yyyy/MM/dd hh:mm:ss"));
            leerDataWebService();
            Log.d(DBHelper.TAG, "FINALIZA Insercion DataSincronizada: " + Funciones.getCurrentDate("yyyy/MM/dd hh:mm:ss"));

        } catch (Exception e) {
            mensaje = e.getMessage();
            try {
                mSincroDAO.dropDataDownloaded();
            } catch (Exception ex1) {
                mensaje = ex1.getMessage();
            }
        }
//        SystemClock.sleep(5000);
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(SincronizarService.ACTION_SYNC_DATA);

        broadcastIntent.putExtra(EXTRA_RESULTADO_EXITO, exito);
        broadcastIntent.putExtra(EXTRA_RESULTADO_MENSAJE, mensaje);

        Log.d(DBHelper.TAG, "Mesaje Final Sincronizacion: " + mensaje);

        sendOrderedBroadcast(broadcastIntent, null);

    }

    private void leerDataWebService() throws Exception {
        Object requestObject;
        String result;
        exito = false;
        String GET_URI = mUrlServer + "restaurante/ObtenerDatosIniciales/?usuario=%s&codCia=%s&cadenaConexion=%s";
        String url = String.format(GET_URI, mUsuario, mCodCia, mAmbiente);

        Log.d(DBHelper.TAG, url);
        if (Funciones.hasActiveInternetConnection(getApplicationContext())) {
            RestConnector restConnector = RestUtil.obtainGetConnection(url);
            requestObject = restConnector.doRequest(url);

            if (requestObject instanceof String) {
                // Only if the request was successful parse the returned value otherwise re-throw the exception
                result = (String) requestObject;
                Gson gson = new Gson();
                JsonObject jsonObjectResponse = gson.fromJson(result, JsonObject.class);
                saveSyncDataToDB(jsonObjectResponse);
                Log.d(DBHelper.TAG, "Fin Insercion DataSincronizada Cantidad. Was is it successful the operation? :  " + exito);

            } else if (requestObject instanceof Exception) {
                Log.d(DBHelper.TAG, "Error al guardar data de sincronizacion: " + ((Exception) requestObject).getMessage());
                throw new Exception(((Exception) requestObject).getMessage());
            }
        }

    }

    private void saveSyncDataToDB(JsonObject jsonObjectResponse) throws Exception {

        ArticuloDAO articuloDAO = new ArticuloDAO(getApplicationContext());
        CategoriaDAO categoriaDAO = new CategoriaDAO(getApplicationContext());
        PrioridadDAO prioridadDAO = new PrioridadDAO(getApplicationContext());
        MesaPisoDAO mesaPisoDAO = new MesaPisoDAO(getApplicationContext());
        CartaDAO cartaDAO = new CartaDAO(getApplicationContext());
        ClienteDAO clienteDAO = new ClienteDAO(getApplicationContext());
        ConceptoDAO conceptoDAO = new ConceptoDAO(getApplicationContext());
        MesaInfoDAO mesaInfoDAO = new MesaInfoDAO(getApplicationContext());


        JsonArray jsonArray;
        jsonArray = jsonObjectResponse.getAsJsonArray("tablaFamilia");
        if (categoriaDAO.saveCategoriaData(jsonArray) > 0) {
            jsonArray = jsonObjectResponse.getAsJsonArray("tablaPrioridad");
            if (prioridadDAO.savePrioridadData(jsonArray) > 0) {
                jsonArray = jsonObjectResponse.getAsJsonArray("tablaMesa");
                if (mesaPisoDAO.saveMesaData(jsonArray) > 0) {
                    jsonArray = jsonObjectResponse.getAsJsonArray("tablaMesaInfo");
                    if (mesaInfoDAO.saveMesaInfoData(jsonArray) > 0) {
                        jsonArray = jsonObjectResponse.getAsJsonArray("tablaCarta");
                        if (cartaDAO.saveCartaData(jsonArray) > 0) {
                            jsonArray = jsonObjectResponse.getAsJsonArray("tablaCliente");
                            if (clienteDAO.saveClienteData(jsonArray) > 0) {
                                jsonArray = jsonObjectResponse.getAsJsonArray("tablaArticuloPrecio");
                                if (articuloDAO.saveArticuloPrecioData(jsonArray) > 0) {
                                    jsonArray = jsonObjectResponse.getAsJsonArray("tablaEstadoPedido");
                                    if (conceptoDAO.saveConceptoData(jsonArray, 1) > 0) {
                                        jsonArray = jsonObjectResponse.getAsJsonArray("tablaEstadoArticuloPedido");
                                        if (conceptoDAO.saveConceptoData(jsonArray, 2) > 0) {
                                            ControlSharedPref.save(mPrefControl, null, null, false, true,
                                                    false, "", false);
                                            SincroDAO sincroDAO = new SincroDAO(getApplicationContext());
                                            sincroDAO.dropDataUploaded();
                                            exito = true;
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }
}
