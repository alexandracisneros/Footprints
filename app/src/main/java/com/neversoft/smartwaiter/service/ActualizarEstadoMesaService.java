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
import com.neversoft.smartwaiter.model.business.MesaPisoDAO;
import com.neversoft.smartwaiter.model.entity.MesaPisoEE;
import com.neversoft.smartwaiter.preference.ConexionSharedPref;
import com.neversoft.smartwaiter.preference.PedidoExtraSharedPref;
import com.neversoft.smartwaiter.ui.LoginActivity;
import com.neversoft.smartwaiter.ui.MesasActivity;
import com.neversoft.smartwaiter.util.Funciones;

import java.net.URLEncoder;

/**
 * Created by Usuario on 07/01/2016.
 */
public class ActualizarEstadoMesaService extends IntentService {
    public static final String ACTION_UPDATE_TABLE_STATUS = "com.neversoft.smartwaiter.service.SEND_UPDATE_TABLE_STATUS";
    //inputs
    public static final String EXTRA_NUEVO_ESTADO_RESERVA = "estado_reserva";
    public static final String EXTRA_NUEVO_ESTADO_MESA = "estado_mesa";
    //outputs
    public static final String EXTRA_RESULTADO_ACTUALIZACION = "resultado_actualizacion";
    public static final String EXTRA_MENSAJE_ACTUALIZACION = "mensaje_actualizacion";

    private static final String NAME = "ActualizarEstadoMesa";
    private static int NOTIFY_ID = 1340;
    private boolean exito = false;
    private String mMensaje = "";
    private int mResultado = 0;
    private SharedPreferences mPrefConfig;
    private SharedPreferences mPrefConexion;
    private SharedPreferences mPrefPedidoExtras;

    public ActualizarEstadoMesaService() {
        super(NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        boolean procesoOK;

        // Retrieve Shared Preferences
        mPrefConfig = getApplication().getSharedPreferences(LoginActivity.PREF_CONFIG, Context.MODE_PRIVATE);
        mPrefConexion = getApplication().getSharedPreferences(ConexionSharedPref.NAME, Context.MODE_PRIVATE);
        mPrefPedidoExtras = getApplication().getSharedPreferences(PedidoExtraSharedPref.NAME, Context.MODE_PRIVATE);

        // Retrieve Extras
        String nuevoEstadoReserva = intent.getStringExtra(EXTRA_NUEVO_ESTADO_RESERVA);
        String nuevoEstadoMesa = intent.getStringExtra(EXTRA_NUEVO_ESTADO_MESA);


        MesaPisoEE mesaPisoEE = new MesaPisoEE();
        String urlServer = RestUtil.obtainURLServer(getApplicationContext());
        String url = urlServer
                + "restaurante/ActualizarEstadoMesa/?nroMesa=%s&idAmbiente=%s&estadoMesa=%s&idReserva=%s&estadoReserva=%s&codCia=%s&cadenaConexion=%s";

        ///ActualizarEstadoMesa/?nroMesa=10&idAmbiente=1&estadoMesa=PAT&idReserva=0&estadoReserva=&nbsp&codCia=001&cadenaConexion=Initial%20Catalog=PRUEBAMOVILJHAV
        //ActualizarEstadoMesa/?nroMesa=10&idAmbiente=1&estadoMesa=PAT&idReserva=2&estadoReserva=EFE&codCia=001&cadenaConexion=Initial%20Catalog=PRUEBAMOVILJHAV

        //String nuevoEstadoReserva = "EFE";
        //String nuevoEstadoMesa = "OCU";
        String codCia = mPrefConfig.getString("CodCia", "");
        String ambiente = mPrefConexion.getString(ConexionSharedPref.AMBIENTE, "");
        //Retrieving extras
        String mesaString = mPrefPedidoExtras.getString(PedidoExtraSharedPref.SELECTED_TABLE_JSON, null);
        String className = mPrefPedidoExtras.getString(PedidoExtraSharedPref.STARTING_ACTIVITY, MesasActivity.class.getClass().getName());

        Gson gson = new Gson();
        mesaPisoEE = gson.fromJson(mesaString, MesaPisoEE.class);
        String idReserva = String.valueOf(mesaPisoEE.getCodReserva());
        Class<?> clase = MesasActivity.class; //Clase por defecto para evitar asignar null

        Log.d(DBHelper.TAG, ambiente);
        try {
            // Simple GET
            clase = Class.forName(className);
            String mensajeError = "";

            if (codCia != "") {
                String encondedAmbiente = URLEncoder.encode(ambiente,
                        "utf-8");
                //OCU es el estado al que pasará la mesa
                String urlWithParams = String.format(url, mesaPisoEE.getNroMesa(), mesaPisoEE.getCodAmbiente(),
                        nuevoEstadoMesa, idReserva, nuevoEstadoReserva, codCia, encondedAmbiente);
                Log.d(DBHelper.TAG, "ID_RESERVA: " + idReserva);
                procesoOK = sendRequestToServer(urlWithParams);
                if (procesoOK) {
                    MesaPisoDAO mesaPisoDAO = new MesaPisoDAO(getApplicationContext());
                    mResultado = mesaPisoDAO.updateEstadoMesa(mesaPisoEE.getId(),
                            nuevoEstadoMesa);
                    if (mResultado == 0) {
                        mensajeError = "No se pudo actualizar el estado de la mesa nro" + mesaPisoEE.getNroMesa();
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
            mMensaje = e.getMessage();
            exito = false;
        }
        Intent event = new Intent(ActualizarEstadoMesaService.ACTION_UPDATE_TABLE_STATUS);
        event.putExtra(EXTRA_RESULTADO_ACTUALIZACION, mResultado);
        event.putExtra(EXTRA_MENSAJE_ACTUALIZACION, mMensaje);

//        SystemClock.sleep(2000);
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
