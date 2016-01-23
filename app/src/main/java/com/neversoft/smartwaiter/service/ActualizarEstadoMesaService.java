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
import com.neversoft.smartwaiter.R;
import com.neversoft.smartwaiter.database.DBHelper;
import com.neversoft.smartwaiter.io.RestConnector;
import com.neversoft.smartwaiter.io.RestUtil;
import com.neversoft.smartwaiter.model.entity.MesaPisoEE;
import com.neversoft.smartwaiter.preference.ConexionSharedPref;
import com.neversoft.smartwaiter.ui.LoginActivity;
import com.neversoft.smartwaiter.util.Funciones;

import java.net.URLEncoder;

/**
 * Created by Usuario on 07/01/2016.
 */
public class ActualizarEstadoMesaService extends IntentService {
    public static final String ACTION_UPDATE_TABLE_STATUS = "com.neversoft.smartwaiter.service.SEND_UPDATE_TABLE_STATUS";
    public static final String EXTRA_RESULTADO_ACTUALIZACION = "resultado_actualizacion";
    public static final String EXTRA_CLASS_NAME = "class_name";
    public static final String EXTRA_TABLE = "table";
    private static final String NAME = "ActualizarEstadoMesa";
    private static int NOTIFY_ID = 1340;
    private boolean exito = false;
    private String mensaje = "";
    private SharedPreferences mPrefConfig;
    private SharedPreferences mPrefConexion;

    public ActualizarEstadoMesaService() {
        super(NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        boolean procesoOK;

        // get SharedPreferences
        mPrefConfig = getApplication().getSharedPreferences(LoginActivity.PREF_CONFIG, Context.MODE_PRIVATE);
        mPrefConexion = getApplication().getSharedPreferences(ConexionSharedPref.NAME, Context.MODE_PRIVATE);
        MesaPisoEE mesaPisoEE = new MesaPisoEE();
        String urlServer = RestUtil.obtainURLServer(getApplicationContext());
        String url = urlServer
                + "restaurante/ActualizarEstadoMesa/?nroMesa=%s&idAmbiente=%s&estadoMesa=%s&idReserva=%s&estadoReserva=%s&codCia=%s&cadenaConexion=%s";

        ///ActualizarEstadoMesa/?nroMesa=10&idAmbiente=1&estadoMesa=PAT&idReserva=0&estadoReserva=&nbsp&codCia=001&cadenaConexion=Initial%20Catalog=PRUEBAMOVILJHAV
        //ActualizarEstadoMesa/?nroMesa=10&idAmbiente=1&estadoMesa=PAT&idReserva=2&estadoReserva=EFE&codCia=001&cadenaConexion=Initial%20Catalog=PRUEBAMOVILJHAV

        String estadoReserva = "EFE";
        String codCia = mPrefConfig.getString("CodCia", "");
        String ambiente = mPrefConexion.getString(ConexionSharedPref.AMBIENTE, "");
        String className = intent.getStringExtra(EXTRA_CLASS_NAME);

        String mesaString = intent.getStringExtra(EXTRA_TABLE);
        Gson gson = new Gson();
        mesaPisoEE = gson.fromJson(mesaString,
                MesaPisoEE.class);
        String idReserva = String.valueOf(mesaPisoEE.getCodReserva());

        Log.d(DBHelper.TAG, ambiente);
        try {
            // Simple GET
            Class<?> clase = Class.forName(className);
            String mensajeError = "";

            if (codCia != "") {
                String encondedAmbiente = URLEncoder.encode(ambiente,
                        "utf-8");
                //OCU es el estado al que pasará la mesa
                String urlWithParams = String.format(url, mesaPisoEE.getNroMesa(), mesaPisoEE.getCodAmbiente(),
                        "OCU", idReserva, estadoReserva,
                        codCia, encondedAmbiente);
                Log.d(DBHelper.TAG, "ID_RESERVA: " + idReserva);
                procesoOK = sendRequestToServer(urlWithParams);
                if (procesoOK) {
                    Intent event = new Intent(ActualizarEstadoMesaService.ACTION_UPDATE_TABLE_STATUS);
                    event.putExtra(EXTRA_RESULTADO_ACTUALIZACION, procesoOK);
                    if (!LocalBroadcastManager.getInstance(this).sendBroadcast(event)) {

                        NotificationCompat.Builder b = new NotificationCompat.Builder(this);
                        Intent ui = new Intent(this, clase);

                        b.setAutoCancel(true).setDefaults(Notification.DEFAULT_SOUND)
                                .setContentTitle(getString(R.string.notif_title))
                                .setContentText("true")
                                .setSmallIcon(android.R.drawable.stat_notify_more)
                                .setTicker(getString(R.string.notif_title))
                                .setContentIntent(PendingIntent.getActivity(this, 0, ui, 0));

                        NotificationManager mgr =
                                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                        mgr.notify(NOTIFY_ID, b.build());
                    }
                }

            } else {
                mensajeError = "No se ha configurado 'código de compañía'";
            }
            if (mensajeError != "") {
                throw new Exception(mensajeError);
            }
        } catch (Exception e) {
            Log.d(DBHelper.TAG, e.getMessage());
            mensaje = e.getMessage();
            exito = false;
        }
    }

    private boolean sendRequestToServer(String urlWithParams) throws Exception {
        boolean procesoOK = false;
        Object requestObject;
        String resultado;
        Log.d(DBHelper.TAG, urlWithParams);
        if (Funciones.hasActiveInternetConnection(getApplicationContext())) {
            RestConnector restConnector = RestUtil
                    .obtainGetConnection(urlWithParams);
            requestObject = restConnector.doRequest(urlWithParams);
            if (requestObject instanceof String) {
                // Only if the request was successful parse the returned value
                // otherwise re-throw the exception
                resultado = (String) requestObject;
                procesoOK = Boolean.parseBoolean(resultado);

            } else if (requestObject instanceof Exception) {
                throw new Exception(((Exception) requestObject).getMessage());
            }
        }
        return procesoOK;
    }
}
