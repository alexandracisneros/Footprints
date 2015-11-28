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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.neversoft.smartwaiter.R;
import com.neversoft.smartwaiter.database.SmartWaiterDB;
import com.neversoft.smartwaiter.io.RestConnector;
import com.neversoft.smartwaiter.io.RestUtil;
import com.neversoft.smartwaiter.model.business.DetallePedidoDAO;
import com.neversoft.smartwaiter.model.entity.PedidoEE;
import com.neversoft.smartwaiter.preference.ConexionSharedPref;
import com.neversoft.smartwaiter.ui.LoginActivity;
import com.neversoft.smartwaiter.ui.PedidosARecogerActivity;
import com.neversoft.smartwaiter.util.Funciones;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class NotificarPedidosRecogidosService extends IntentService {
    public static final String ACTION_NOTIFICAR_RECOJO_PEDIDO = "com.neversoft.smartwaiter.NOTIFICAR_RECOJO_PEDIDO";
    private static final String NAME = "NotificarPedidosRecogidosService";
    private static int NOTIFY_ID = 1338;
    private SharedPreferences mPrefConfig;
    private SharedPreferences mPrefConexion;

    private String mCodCia;
    private String mUsuario;
    private String mAmbiente;

    public NotificarPedidosRecogidosService() {
        super(NAME);
        setIntentRedelivery(false);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String mensaje = "";
        boolean procesoOK = false;
        boolean exito = false;
        // get SharedPreferences
        mPrefConfig = getApplicationContext().getSharedPreferences(
                LoginActivity.PREF_CONFIG, Context.MODE_PRIVATE);
        mPrefConexion = getApplicationContext().getSharedPreferences(
                ConexionSharedPref.NAME, Context.MODE_PRIVATE);
        mAmbiente = mPrefConexion.getString(ConexionSharedPref.AMBIENTE, "");

        mCodCia = mPrefConfig.getString("CodCia", "");
        mUsuario = mPrefConfig.getString("Usuario", "").toUpperCase(
                Locale.getDefault());
        String idPedido=intent.getStringExtra(PedidosARecogerActivity.EXTRA_ID_PEDIDO);
        String idPedidoServidor=intent.getStringExtra(PedidosARecogerActivity.EXTRA_ID_PEDIDO_SERV);
        ArrayList<String> selectedItems =intent.getExtras().getStringArrayList(PedidosARecogerActivity.EXTRA_SELECTED_ITEMS_ARRAY);
        String[] items=selectedItems.toArray(new String[selectedItems.size()]);
        int totalPedidosXRecoger=intent.getIntExtra(PedidosARecogerActivity.EXTRA_TOTAL_ITEMS_RECOGER, 0);
        int totalPedidoNoRecogidos=totalPedidosXRecoger-items.length;
        String idPedidoRefrescar="0";
        try {
            if (Funciones.hasActiveInternetConnection(getApplicationContext())) {
                String dataToSend = getEnvio(idPedido,idPedidoServidor,items);
                procesoOK = sendDataToServer(dataToSend);
                if(procesoOK) {
                    DetallePedidoDAO detallePedidoDAO=new DetallePedidoDAO(getApplicationContext());
                    List<String> arrayIDs= Arrays.asList(items);
                    detallePedidoDAO.confirmRecojoItemsPedido(idPedido,arrayIDs);
                    if (totalPedidoNoRecogidos >= 1) {
                        //refresh_only_detail
                        //otherwise refresh all the orders and select the one with the oder_id=0
                        //with that the details list should be clear
                        idPedidoRefrescar=idPedido;
                    }
                    Intent event = new Intent(NotificarPedidosRecogidosService.ACTION_NOTIFICAR_RECOJO_PEDIDO);
                    event.putExtra(PedidosARecogerActivity.EXTRA_ID_PEDIDO_REFRESCAR, idPedidoRefrescar);
                    if (!LocalBroadcastManager.getInstance(this).sendBroadcast(event)) {
                        Log.d(getClass().getSimpleName(), "I only run when I have to show a notification!");
                        NotificationCompat.Builder b = new NotificationCompat.Builder(this);
                        Intent ui = new Intent(this, PedidosARecogerActivity.class);

                        b.setAutoCancel(true).setDefaults(Notification.DEFAULT_SOUND)
                                .setContentTitle(getString(R.string.notif_title))
                                .setContentText(idPedidoRefrescar)
                                .setSmallIcon(android.R.drawable.stat_notify_more)
                                .setTicker(getString(R.string.notif_title))
                                .setContentIntent(PendingIntent.getActivity(this, 0, ui, 0));

                        NotificationManager mgr =
                                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                        mgr.notify(NOTIFY_ID, b.build());
                    }
                }
            }

        } catch (Exception e) {
            mensaje = e.getMessage();
            exito = false;
        }
    }
    private String getEnvio(String idPedido, String idPedidoServidor,String[] itemsSeleccionados) throws Exception {
        String result;
        JsonObject jsonObjCab = new JsonObject();

        jsonObjCab.addProperty("idPedOri", idPedido);
        jsonObjCab.addProperty("idPed", idPedidoServidor);
        jsonObjCab.addProperty("fecha", Funciones.getCurrentDate("yyyy/MM/dd hh:mm:ss"));
        jsonObjCab.addProperty("codUsuario", mUsuario); //TODO :CHEKA SI ES EL CORRECTO
        jsonObjCab.addProperty("codCia", mCodCia);
        JsonArray jsArrayItems = new JsonArray();
        for (String idItem : itemsSeleccionados) {
            JsonObject jsItem = getItem(idItem, idPedidoServidor);
            jsArrayItems.add(jsItem);
        }
        jsonObjCab.add("detalle", jsArrayItems);
        jsonObjCab.addProperty("cadenaConexion", mAmbiente);
        result = jsonObjCab.toString();
        return result;
    }
    private JsonObject getItem(String item, String idPedidoServidor) throws Exception {

        JsonObject jsonObjItem= new JsonObject();
        jsonObjItem.addProperty("idPed", idPedidoServidor);
        jsonObjItem.addProperty("item", item);

        return jsonObjItem;
    }
    // Methods used in sending all the orders back to the server for processing
    private boolean sendDataToServer(String dataToSend) throws Exception {
        boolean procesoOK = false;
        Object requestObject = null;
        String resultado;

        String urlServer = RestUtil.obtainURLServer(getApplicationContext());
        String POST_URI = urlServer + "restaurante/EnviarListaPedidoRecibidos/";


        Log.d(SmartWaiterDB.TAG, POST_URI);

        // Simple Post
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("dataToSend", dataToSend));
        if (Funciones.hasActiveInternetConnection(getApplicationContext())) {
            RestConnector restConnector = RestUtil
                    .obtainFormPostConnection(POST_URI, parameters);
            requestObject = restConnector.doRequest(POST_URI);
            if (requestObject instanceof String) {
                // Only if the request was successful parse the returned value
                // otherwise re-throw the exception
                resultado = (String) requestObject;
                procesoOK= Boolean.parseBoolean(resultado);

            } else if (requestObject instanceof Exception) {
                throw new Exception(((Exception) requestObject).getMessage());
            }
        }
        return procesoOK;
    }

}
