package com.neversoft.smartwaiter.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.neversoft.smartwaiter.R;
import com.neversoft.smartwaiter.database.SmartWaiterDB;
import com.neversoft.smartwaiter.io.RestConnector;
import com.neversoft.smartwaiter.io.RestUtil;
import com.neversoft.smartwaiter.model.business.DetallePedidoDAO;
import com.neversoft.smartwaiter.preference.ConexionSharedPref;
import com.neversoft.smartwaiter.preference.LoginSharedPref;
import com.neversoft.smartwaiter.ui.LoginActivity;
import com.neversoft.smartwaiter.ui.PedidosARecogerActivity;
import com.neversoft.smartwaiter.util.Funciones;

import java.net.URLEncoder;

/**
 * Created by Usuario on 05/11/2015.
 */
public class ConsultarPedidosRecogerService extends WakefulIntentService {
    public static final String ACTION_CHECK_READY_ORDERS = "com.neversoft.smartwaiter.service.CHECK_READY_ORDERS";
    private static final String NAME = "ConsultarPedidosRecoger";
    private static int NOTIFY_ID = 1337;
    private boolean exito = false;
    private String mensaje = "";
    private String mUrlServer;

    private String mAmbiente;
    private String mCodCia;
    private String mCodMozo;

    // define SharedPreferences object

    private SharedPreferences mPrefConfig;
    private SharedPreferences mPrefLogin;
    private SharedPreferences mPrefConexion;


    public ConsultarPedidosRecogerService() {
        super(NAME);
    }

    @Override
    protected void doWakefulWork(Intent intent) {

        // get SharedPreferences

        mPrefConfig = getApplicationContext().getSharedPreferences(
                LoginActivity.PREF_CONFIG, Context.MODE_PRIVATE);
        mPrefLogin = getApplication().getSharedPreferences(LoginSharedPref.NAME,
                Context.MODE_PRIVATE);
        mPrefConexion = getApplication().getSharedPreferences(ConexionSharedPref.NAME, Context.MODE_PRIVATE);
        mAmbiente = mPrefConexion.getString(ConexionSharedPref.AMBIENTE, "");
        mCodCia = mPrefConfig.getString("CodCia", "");
        mCodMozo = mPrefConfig.getString("CodMozo", "");

        mUrlServer = RestUtil.obtainURLServer(getApplicationContext());
        int cantidadActualizar = 0;
        try {
            mAmbiente = URLEncoder.encode(mAmbiente, "utf-8");
            Log.d(SmartWaiterDB.TAG,
                    "Llamada a  ObtenerPedidosDespachados: " + Funciones.getCurrentDate("yyyy/MM/dd hh:mm:ss"));
            cantidadActualizar = actualizarItemsPedidoDespachados();
        } catch (Exception e) {

            mensaje = e.getMessage();
            exito = false;
        }
        if (cantidadActualizar > 0) {

            Intent event = new Intent(ConsultarPedidosRecogerService.ACTION_CHECK_READY_ORDERS);

//        --update local data with data retrieved from cocina
            //TODO: Considerar una fecha 'fecha listo' para que esa fecha se establezca la unica vez que debo actualizar
            //el estado a despachado de cocina para luego mostrar en la lista los que llegaron primero osea ordernarlos x
            //fecha y hora DESC
            //sera necesario agregar fecha para esto y creo que tb para confirmarRecivido


            event.putExtra(PedidosARecogerActivity.EXTRA_CANTIDAD_ACTUALIZAR, cantidadActualizar);

            //Log.d(getClass().getSimpleName(), "I ran!");
            if (!LocalBroadcastManager.getInstance(this).sendBroadcast(event)) {
                Log.d(getClass().getSimpleName(), "I only run when I have to show a notification!");
                NotificationCompat.Builder b = new NotificationCompat.Builder(this);
                Intent ui = new Intent(this, PedidosARecogerActivity.class);

                b.setAutoCancel(true).setDefaults(Notification.DEFAULT_SOUND)
                        .setContentTitle(getString(R.string.notif_title))
                        .setContentText(String.valueOf(cantidadActualizar))
                        .setContentText(Integer.toHexString(1))
                        .setSmallIcon(android.R.drawable.stat_notify_more)
                        .setTicker(getString(R.string.notif_title))
                        .setContentIntent(PendingIntent.getActivity(this, 0, ui, 0));

                NotificationManager mgr =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                mgr.notify(NOTIFY_ID, b.build());
            }
        }
    }

    private int actualizarItemsPedidoDespachados() throws Exception {
        int cantidadAActualizar = 0;
        DetallePedidoDAO detallePedidoDAO = new DetallePedidoDAO(getApplicationContext());
        Object requestObject = null;
        String result;

        String GET_URI = mUrlServer + "restaurante/ObtenerPedidosDespachados/?"
                + "codMozo=%s&codCia=%s&cadenaConexion=%s";
        String url = String.format(GET_URI, mCodMozo, mCodCia, mAmbiente);

        Log.d(SmartWaiterDB.TAG, url);
        if (Funciones.hasActiveInternetConnection(getApplicationContext())) {
            RestConnector restConnector = RestUtil.obtainGetConnection(url);
            requestObject = restConnector.doRequest(url);

            if (requestObject instanceof String) {
                // Only if the request was successful parse the returned value otherwise re-throw the exception
                result = (String) requestObject;
                Gson gson = new Gson();

                JsonArray jsonArrayResponse = gson.fromJson(result, JsonArray.class);
                cantidadAActualizar = jsonArrayResponse.size();
                if (cantidadAActualizar > 0) {
                    detallePedidoDAO.updateEstadoItemsPedido(jsonArrayResponse, 1, 2);
                }

            } else if (requestObject instanceof Exception) {
                Log.d(SmartWaiterDB.TAG, "Error al guardar data de sincronizacion: "
                        + ((Exception) requestObject).getMessage());
                throw new Exception(((Exception) requestObject).getMessage());
            }
        }


        return cantidadAActualizar;

    }
}
