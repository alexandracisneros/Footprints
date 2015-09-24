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
import com.neversoft.smartwaiter.model.entity.DetallePedidoEE;
import com.neversoft.smartwaiter.model.entity.PedidoEE;
import com.neversoft.smartwaiter.util.Funciones;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Usuario on 17/09/2015.
 */
public class EnviarPedidoService extends IntentService {
    public static final String ACTION_SEND_DATA = "com.neversoft.smartwaiter.ENVIAR_PEDIDO";
    private static final String NAME = "EnviarPedidoService";

    public EnviarPedidoService() {
        super(NAME);
        // We don’t want intents redelivered
        // in case we’re shut down unexpectedly
        setIntentRedelivery(false);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String mensaje = "";
        int procesoOK = 0;
        boolean exito = false;
        try {
            if (Funciones.hasActiveInternetConnection(getApplicationContext())) {


                ArrayList<PedidoEE> listaPedidosRegistrados = new ArrayList<>();
                //todo
                //get data as string convert it back to an Order and again to JSON???
                //wouldn't it be better if they all already had the same names as the WebApi Project?
                String stringPedido = intent.getStringExtra("json");
                Gson gson = new Gson();
                PedidoEE pedido = gson.fromJson(stringPedido,
                        PedidoEE.class);
                listaPedidosRegistrados.add(pedido);
                String dataToSend = getEnvio(listaPedidosRegistrados);//TODO  //PASA ARRAY
                // Log.d("QuickOrder", dataToSend);
                //TODO : BEFORE YOU SEND THE ORDER REGISTER IT IN THE DATABASE SO YOU HAVE  AN ORDER ID
                procesoOK = sendDataToServer(dataToSend);
                if (procesoOK == 1) { // See 'RegistrarPedidoMovil' Method in SS
                    exito = true;

                } else {
                    throw new Exception(
                            "Error al registrar el pedido en el servidor.");
                }

            }
        } catch (Exception e) {
            mensaje = e.getMessage();
            exito = false;
        }

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(EnviarPedidoService.ACTION_SEND_DATA);

        broadcastIntent.putExtra("exito", exito);
        broadcastIntent.putExtra("resultado", procesoOK);
        broadcastIntent.putExtra("mensaje", mensaje);

        Log.d(SmartWaiterDB.TAG, "El resultado de la operacion fue :  " + procesoOK);

        sendOrderedBroadcast(broadcastIntent, null);


    }


    private String getEnvio(ArrayList<PedidoEE> pedidosRegistrados) throws Exception {
        String result = "";
        JsonObject jsonObjEnvio = new JsonObject();

        jsonObjEnvio.addProperty("cadenaConexion", "Initial Catalog=PRUEBAMOVILJHAV");
        JsonArray jsArrayPedidos = new JsonArray();
        for (PedidoEE pedido : pedidosRegistrados) {
            JsonObject jsPedido = getPedido(pedido);
            jsArrayPedidos.add(jsPedido);
        }
        jsonObjEnvio.add("pedidos", jsArrayPedidos);
        result = jsonObjEnvio.toString();
        return result;
    }

    // Methods used in sending all the orders back to the server for processing
    private int sendDataToServer(String dataToSend) throws Exception {
        int procesoOK = 0;
        Object requestObject = null;
        String resultado;
        String url = "http://siempresoftqa.cloudapp.net/PruebaMovilAlex/api/restaurante/EnviarListaPedidoMV/";

        Log.d(SmartWaiterDB.TAG, url);

        // Simple Post
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("dataToSend", dataToSend));
        if (Funciones.hasActiveInternetConnection(getApplicationContext())) {
            RestConnector restConnector = RestUtil
                    .obtainFormPostConnection(url, parameters);
            requestObject = restConnector.doRequest(url);
            if (requestObject instanceof String) {
                // Only if the request was successful parse the returned value
                // otherwise re-throw the exception
                resultado = (String) requestObject;
                procesoOK = Integer.parseInt(resultado);

            } else if (requestObject instanceof Exception) {
                throw new Exception(((Exception) requestObject).getMessage());
            }
        }
        return procesoOK;
    }

    private JsonObject getPedido(PedidoEE ped) throws Exception {
        JsonObject jsonObjPed = new JsonObject();
        JsonArray jsonArrayPedDetalle;
        jsonObjPed.addProperty("Id", ped.getId());
        jsonObjPed.addProperty("fecha", ped.getFecha());
        jsonObjPed.addProperty("nroMesa", ped.getNroMesa());
        jsonObjPed.addProperty("ambiente", ped.getAmbiente());
        jsonObjPed.addProperty("codUsuario", ped.getCodUsuario());
        jsonObjPed.addProperty("codCliente", ped.getCodCliente());
        jsonObjPed.addProperty("tipoVenta", ped.getTipoVenta());  //VA VACIO AL ENVIAR
        jsonObjPed.addProperty("tipoPago", ped.getTipoPago()); // VA VACIO AL ENVIAR
        jsonObjPed.addProperty("moneda", ped.getMoneda());
        jsonObjPed.addProperty("montoTotal", ped.getMontoTotal());
        jsonObjPed.addProperty("montoRecibido", ped.getMontoRecibido());
        jsonObjPed.addProperty("estado", String.valueOf(ped.getEstado()));
        jsonObjPed.addProperty("codcia", "001"); // ped.getCodCia() //De donde saco esto?
        jsonArrayPedDetalle = getDetallePedido(ped.getDetalle());
        jsonObjPed.add("detalle", jsonArrayPedDetalle);

        return jsonObjPed;
    }

    private JsonArray getDetallePedido(ArrayList<DetallePedidoEE> listaDetallePedido) throws Exception {
        JsonArray jsonArrayDetalle = new JsonArray();
        JsonObject jsonObjItem;
        for (DetallePedidoEE item : listaDetallePedido) {
            jsonObjItem = new JsonObject();
            jsonObjItem.addProperty("Id", item.getPedidoId());
            jsonObjItem.addProperty("item", item.getId());
            jsonObjItem.addProperty("codArticulo", item.getCodArticulo());
            jsonObjItem.addProperty("um", item.getUm());
            jsonObjItem.addProperty("cantidad", item.getCantidad());
            jsonObjItem.addProperty("precio", item.getPrecio());
            jsonObjItem.addProperty("tipoArticulo", item.getTipoArticulo());
            jsonObjItem.addProperty("codArticuloPrincipal", item.getCodArticuloPrincipal()); //es este??
            jsonObjItem.addProperty("comentario", item.getComentario()); //Vacio al enviar
            jsonObjItem.addProperty("estadoArticulo", item.getEstadoArticulo());  // desArticulo ????? NO TIENE SENTIDO!!! NO SERA estAriculo???

            jsonArrayDetalle.add(jsonObjItem);
        }

        return jsonArrayDetalle;
    }
}
