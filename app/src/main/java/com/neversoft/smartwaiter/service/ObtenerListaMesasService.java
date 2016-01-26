package com.neversoft.smartwaiter.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.neversoft.smartwaiter.R;
import com.neversoft.smartwaiter.database.DBHelper;
import com.neversoft.smartwaiter.io.RestConnector;
import com.neversoft.smartwaiter.io.RestUtil;
import com.neversoft.smartwaiter.model.business.MesaPisoDAO;
import com.neversoft.smartwaiter.preference.ConexionSharedPref;
import com.neversoft.smartwaiter.ui.LoginActivity;
import com.neversoft.smartwaiter.ui.MesasActivity;
import com.neversoft.smartwaiter.util.Funciones;

import java.net.URLEncoder;

/**
 * Created by Usuario on 21/01/2016.
 */
public class ObtenerListaMesasService extends IntentService {
    public static final String ACTION_GET_TABLES_STATUS = "com.neversoft.smartwaiter.GET_TABLES_STATUS";
    private static final String NAME = "ObtenerListaMesas";
    private static int NOTIFY_ID = 1341;
    private String mUrlServer;
    private String mAmbiente;
    private String mCodCia;
    private String mensaje = "";
    private boolean exito = false;

    // define SharedPreferences objects

    private SharedPreferences mPrefConfig;
    private SharedPreferences mPrefConexion;

    public ObtenerListaMesasService() {
        super(NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int cantidadActualizados = 0;
        //TODO: actualizar todas las mesas y contar cuantas se actualizan, si y solo si el contador >0 volver a listar desde la bd
        //http://qa.siempresoft.com/PruebaMovilAlex/api/restaurante/ObtenerListaMesas/?codCia=001&cadenaConexion=Initial%20Catalog=PRUEBAMOVILJHAV
        mPrefConfig = getApplicationContext().getSharedPreferences(LoginActivity.PREF_CONFIG, Context.MODE_PRIVATE);
        mPrefConexion = getApplication().getSharedPreferences(ConexionSharedPref.NAME, Context.MODE_PRIVATE);

        mAmbiente = mPrefConexion.getString(ConexionSharedPref.AMBIENTE, "");
        mCodCia = mPrefConfig.getString("CodCia", "");
        mUrlServer = RestUtil.obtainURLServer(getApplicationContext());

        try {

            cantidadActualizados = getEstadosMesas();
        } catch (Exception e) {

            mensaje = e.getMessage();
            exito = false;
        }
        Intent event = new Intent(ObtenerListaMesasService.ACTION_GET_TABLES_STATUS);

        event.putExtra(MesasActivity.EXTRA_CANTIDAD_MESAS_ACTUALIZADOS, cantidadActualizados);


        if (!LocalBroadcastManager.getInstance(this).sendBroadcast(event)) {
            Log.d(getClass().getSimpleName(), "I only run when I have to show a notification!");
            NotificationCompat.Builder b = new NotificationCompat.Builder(this);
            Intent ui = new Intent(this, MesasActivity.class);

            b.setAutoCancel(true).setDefaults(Notification.DEFAULT_SOUND)
                    .setContentTitle(getString(R.string.notif_title))
                    .setContentText(String.valueOf(cantidadActualizados))
                    .setContentText("Se actualizaron mesas. Tap para ver")
                    .setSmallIcon(android.R.drawable.stat_notify_more)
                    .setTicker(getString(R.string.notif_title))
                    .setContentIntent(PendingIntent.getActivity(this, 0, ui, 0));

            NotificationManager mgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            mgr.notify(NOTIFY_ID, b.build());
        }
    }

    private int getEstadosMesas() throws Exception {
        Object requestObject;
        String result;
        int cantidadActualizados = 0;
        // Only download data related to Customer if it hasn't been downloaded yet
        try {
            mAmbiente = URLEncoder.encode(mAmbiente, "utf-8");
            String GET_URI = mUrlServer + "restaurante/ObtenerListaMesas/?"
                    + "codCia=%s&cadenaConexion=%s";
            String url = String.format(GET_URI, mCodCia, mAmbiente);

            Log.d(DBHelper.TAG, url);
            if (Funciones.hasActiveInternetConnection(getApplicationContext())) {
                RestConnector restConnector = RestUtil.obtainGetConnection(url);
                requestObject = restConnector.doRequest(url);

                if (requestObject instanceof String) {
                    // Only if the request was successful parse the returned value otherwise re-throw the exception
                    result = (String) requestObject;
                    Gson gson = new Gson();
                    JsonObject jsonResponse = gson.fromJson(result, JsonObject.class);
                    JsonArray jsonArray = jsonResponse.getAsJsonArray("Table");
                    if (jsonArray != null && jsonArray.size() > 0) {
                        MesaPisoDAO mesaPisoDAO = new MesaPisoDAO(getApplicationContext());
                        cantidadActualizados = mesaPisoDAO.updateEstadoMesa(jsonArray);
                    }

                    return cantidadActualizados;

                } else if (requestObject instanceof Exception) {
                    Log.d(DBHelper.TAG, "Error al obtenerEstadoMesa/actualizarEstadoMesa : "
                            + ((Exception) requestObject).getMessage());
                    throw new Exception(((Exception) requestObject).getMessage());
                }
            }

        } catch (Exception ex) {
            // if something goes wrong delete all customers that could have been saved.
            throw ex;
        }
        return cantidadActualizados;

    }
}
