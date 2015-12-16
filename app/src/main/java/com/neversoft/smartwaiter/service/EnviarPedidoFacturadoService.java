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
import com.neversoft.smartwaiter.database.DBHelper;
import com.neversoft.smartwaiter.io.RestConnector;
import com.neversoft.smartwaiter.io.RestUtil;
import com.neversoft.smartwaiter.model.business.DetallePedidoDAO;
import com.neversoft.smartwaiter.model.business.PedidoDAO;
import com.neversoft.smartwaiter.model.entity.DetallePedidoEE;
import com.neversoft.smartwaiter.preference.ConexionSharedPref;
import com.neversoft.smartwaiter.ui.PedidosARecogerActivity;
import com.neversoft.smartwaiter.ui.PedidosFacturarActivity;
import com.neversoft.smartwaiter.util.Funciones;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Usuario on 13/12/2015.
 */
public class EnviarPedidoFacturadoService extends IntentService {
    public static final String ACTION_SEND_ORDERS_TO_INVOICE = "com.neversoft.smartwaiter.service.SEND_ORDERS_TO_INVOICE";
    private static final String NAME = "EnviarPedidoFacturado";
    private static int NOTIFY_ID = 1338;
    private boolean exito = false;
    private String mensaje = "";
    private String mUrlServer;

    private String mAmbiente;

    // define SharedPreferences object
    private SharedPreferences mPrefConexion;

    public EnviarPedidoFacturadoService() {
        super(NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Cabecera Pedido = 040 'SE REALIZO LA PREFACTURACION'
        //Detalle Pedido = 3 'SE REALIZO LA PREFACTURACION'
        boolean procesoOK = false;
        boolean actualizarListView=false;

        // get SharedPreferences
        mPrefConexion = getApplication().getSharedPreferences(ConexionSharedPref.NAME, Context.MODE_PRIVATE);
        mAmbiente = mPrefConexion.getString(ConexionSharedPref.AMBIENTE, "");
        mUrlServer = RestUtil.obtainURLServer(getApplicationContext());

        String idPedido = intent.getStringExtra(PedidosFacturarActivity.EXTRA_ID_PEDIDO);
        String idPedidoServidor = intent.getStringExtra(PedidosFacturarActivity.EXTRA_ID_PEDIDO_SERV);
        String tipoVenta = intent.getStringExtra(PedidosFacturarActivity.EXTRA_TIPO_VENTA);
        String tipoPago = intent.getStringExtra(PedidosFacturarActivity.EXTRA_TIPO_PAGO);
        String ruc = intent.getStringExtra(PedidosFacturarActivity.EXTRA_RUC);
        try {
            if (Funciones.hasActiveInternetConnection(getApplicationContext())) {
                String dataToSend = getEnvio(idPedido,idPedidoServidor, tipoVenta, tipoPago, ruc);
                procesoOK = sendDataToServer(dataToSend);
                if (procesoOK) {
                    PedidoDAO pedidoDAO = new PedidoDAO(getApplicationContext());

                    int resultadoPedido = pedidoDAO.updateEstadoPedidoDetalle(Integer.parseInt(idPedido), "040", 2, 3);
                    if (resultadoPedido > 0) {
                        actualizarListView = true;
                    }
                    Intent event = new Intent(EnviarPedidoFacturadoService.ACTION_SEND_ORDERS_TO_INVOICE);
                    event.putExtra(PedidosFacturarActivity.EXTRA_REFRESCAR_LIST_VIEW, actualizarListView); //TODO: <--- ACA ME QUEDÉ 15/12/2015
                    if (!LocalBroadcastManager.getInstance(this).sendBroadcast(event)) {

                        NotificationCompat.Builder b = new NotificationCompat.Builder(this);
                        Intent ui = new Intent(this, PedidosFacturarActivity.class);

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
            }

        } catch (Exception e) {
            mensaje = e.getMessage();
            exito = false;
        }
    }

    private String getEnvio(String idPedido,String idPedidoServ, String tipoVenta, String tipoPago, String ruc) throws Exception {
        String result;
        JsonObject jsonObjCab = new JsonObject();

        jsonObjCab.addProperty("idPedOri", idPedidoServ); //consultar
        jsonObjCab.addProperty("tipoVent", tipoVenta);
        jsonObjCab.addProperty("tipoPago", tipoPago);
        jsonObjCab.addProperty("ruc", ruc);
        JsonArray jsArrayItems = new JsonArray();

        DetallePedidoDAO detallePedidoDAO = new DetallePedidoDAO(getApplicationContext());
        List<DetallePedidoEE> detalle = detallePedidoDAO.getDetallePorEstado(idPedido, 3);


        for (DetallePedidoEE item : detalle) {
            JsonObject jsItem = getItem(String.valueOf(item.getItem()));
            jsArrayItems.add(jsItem);
        }
        jsonObjCab.add("detalle", jsArrayItems);
        jsonObjCab.addProperty("cadenaConexion", mAmbiente);
        result = jsonObjCab.toString();
        return result;
    }

    private JsonObject getItem(String item) throws Exception {

        JsonObject jsonObjItem = new JsonObject();
        jsonObjItem.addProperty("item", item);

        return jsonObjItem;
    }

    // Methods used in sending all the orders back to the server for processing
    private boolean sendDataToServer(String dataToSend) throws Exception {
        boolean procesoOK = false;
        Object requestObject = null;
        String resultado;

        String urlServer = RestUtil.obtainURLServer(getApplicationContext());
        String POST_URI = urlServer + "restaurante/PedidosPrefacturados/";


        Log.d(DBHelper.TAG, POST_URI);

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
                procesoOK = Boolean.parseBoolean(resultado);

            } else if (requestObject instanceof Exception) {
                throw new Exception(((Exception) requestObject).getMessage());
            }
        }
        return procesoOK;
    }

}
