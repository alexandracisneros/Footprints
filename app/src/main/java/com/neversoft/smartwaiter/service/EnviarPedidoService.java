package com.neversoft.smartwaiter.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.neversoft.smartwaiter.database.SmartWaiterDB;
import com.neversoft.smartwaiter.io.RestConnector;
import com.neversoft.smartwaiter.io.RestUtil;
import com.neversoft.smartwaiter.model.business.PedidoDAO;
import com.neversoft.smartwaiter.model.entity.DetallePedidoEE;
import com.neversoft.smartwaiter.model.entity.PedidoEE;
import com.neversoft.smartwaiter.preference.ConexionSharedPref;
import com.neversoft.smartwaiter.ui.LoginActivity;
import com.neversoft.smartwaiter.util.Funciones;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Usuario on 17/09/2015.
 */
public class EnviarPedidoService extends IntentService {
    public static final String ACTION_SEND_DATA = "com.neversoft.smartwaiter.ENVIAR_PEDIDO";
    private static final String NAME = "EnviarPedidoService";
    // define SharedPreferences object
    private SharedPreferences mPrefConfig;
    private SharedPreferences mPrefConexion;

    private String mAmbiente;
    private String mCodMozo;
    private String mCodCia;
    private String mUsuario;

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
        long idPedido = 0;

        // get SharedPreferences
        mPrefConfig = getApplicationContext().getSharedPreferences(
                LoginActivity.PREF_CONFIG, Context.MODE_PRIVATE);
        mPrefConexion = getApplicationContext().getSharedPreferences(
                ConexionSharedPref.NAME, Context.MODE_PRIVATE);
        mAmbiente = mPrefConexion.getString(ConexionSharedPref.AMBIENTE, "");

        mCodCia = mPrefConfig.getString("CodCia", "");
        mCodMozo =mPrefConfig.getString("CodMozo","");
        mUsuario = mPrefConfig.getString("Usuario", "").toUpperCase(
                Locale.getDefault());
        try {
            if (Funciones.hasActiveInternetConnection(getApplicationContext())) {


                ArrayList<PedidoEE> listaPedidosRegistrados = new ArrayList<>();
                PedidoDAO pedidoDAO = new PedidoDAO(getApplicationContext());
                //todo
                //get data as string convert it back to an Order and again to JSON???
                //wouldn't it be better if they all already had the same names as the WebApi Project?
                String stringPedido = intent.getStringExtra("json");
                Gson gson = new Gson();
                PedidoEE pedido = gson.fromJson(stringPedido,
                        PedidoEE.class);
                listaPedidosRegistrados.add(pedido);
                try {
                    idPedido = pedidoDAO.savePedido(pedido,1); // 1=ENVIADO A COCINA
                } catch (Exception e) {
                    throw new Exception("No se pudo guardar el pedido. Excepcion: " + e.getMessage());
                }
                //TODO : VERIFICAR ANTES DE INSERTAR SI EL PEDIDO NO HA SIDO YA INSERTADO, SI YA ESTA INSERTADO SOLO ENVIAR
                if (idPedido > 0) {
                    String dataToSend = getEnvio(listaPedidosRegistrados, idPedido);//TODO  //PASA ARRAY
                    // Log.d("QuickOrder", dataToSend);
                    //TODO : BEFORE YOU SEND THE ORDER REGISTER IT IN THE DATABASE SO YOU HAVE  AN ORDER ID
                    procesoOK = sendDataToServer(dataToSend);
                    if (procesoOK > 0) { // Return the id of the order
                        exito = true;

                    } else {
                        throw new Exception(
                                "Pedido enviado pero no guardado");
                    }
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


    private String getEnvio(ArrayList<PedidoEE> pedidosRegistrados, long idPedido) throws Exception {
        String result;
        JsonObject jsonObjEnvio = new JsonObject();

        jsonObjEnvio.addProperty("cadenaConexion", mAmbiente);
        JsonArray jsArrayPedidos = new JsonArray();
        for (PedidoEE pedido : pedidosRegistrados) {
            JsonObject jsPedido = getPedido(pedido, idPedido);
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

        String urlServer = RestUtil.obtainURLServer(getApplicationContext());
        String POST_URI = urlServer + "restaurante/EnviarListaPedidoMV/";  //ACA ME QUEDE


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
                procesoOK = Integer.parseInt(resultado);

            } else if (requestObject instanceof Exception) {
                throw new Exception(((Exception) requestObject).getMessage());
            }
        }
        return procesoOK;
    }

    private JsonObject getPedido(PedidoEE ped, long idPedido) throws Exception {
        //TODO: Necesita haber una forma de comunicacion entre los pedidos de la bd sqLite y la bd SQL
        //Tal vez agregar un campo en la bd SQL para guardar los correspondientes IDs del pedido y detalles de sqLite
        //yo debo tener un campo en la bd Sqlite tb para guardar los ids del pedido y detalle de SQL
        JsonObject jsonObjPed = new JsonObject();
        JsonArray jsonArrayPedDetalle;
        jsonObjPed.addProperty("id", idPedido);
        jsonObjPed.addProperty("fecha", ped.getFecha());
        jsonObjPed.addProperty("nroMesa", ped.getNroMesa());
        jsonObjPed.addProperty("nropiso", ped.getNroPiso());
        jsonObjPed.addProperty("ambiente", ped.getAmbiente());
        jsonObjPed.addProperty("codMozo", mCodMozo);
        jsonObjPed.addProperty("codUsuario", ped.getCodUsuario());
        jsonObjPed.addProperty("codCliente", ped.getCodCliente());
        jsonObjPed.addProperty("tipoVenta", ped.getTipoVenta());  //VA VACIO AL ENVIAR
        jsonObjPed.addProperty("tipoPago", ped.getTipoPago()); // VA VACIO AL ENVIAR
        jsonObjPed.addProperty("moneda", ped.getMoneda());
        jsonObjPed.addProperty("montoTotal", ped.getMontoTotal());
        jsonObjPed.addProperty("montoRecibido", ped.getMontoRecibido());
        jsonObjPed.addProperty("estado", String.valueOf(ped.getEstado()));
        jsonObjPed.addProperty("codcia", mCodCia); // ped.getCodCia() //De donde saco esto?
        jsonArrayPedDetalle = getDetallePedido(ped.getDetalle(), idPedido);
        jsonObjPed.add("detalle", jsonArrayPedDetalle);

        return jsonObjPed;
    }

    private JsonArray getDetallePedido(ArrayList<DetallePedidoEE> listaDetallePedido, long idPedido) throws Exception {
        JsonArray jsonArrayDetalle = new JsonArray();
        JsonObject jsonObjItem;
        for (int i = 0; i < listaDetallePedido.size(); i++) {
            jsonObjItem = new JsonObject();
            jsonObjItem.addProperty("id", idPedido);//TODO : Cambiar a id en minusculas
            jsonObjItem.addProperty("idPedido", i + 1);//TODO : Cambiar a idPedido
            jsonObjItem.addProperty("codArticulo", listaDetallePedido.get(i).getCodArticulo());
            jsonObjItem.addProperty("um", listaDetallePedido.get(i).getUm());
            jsonObjItem.addProperty("cantidad", listaDetallePedido.get(i).getCantidad());
            jsonObjItem.addProperty("precio", listaDetallePedido.get(i).getPrecio());
            jsonObjItem.addProperty("tipoArticulo", listaDetallePedido.get(i).getTipoArticulo());
            jsonObjItem.addProperty("codArticuloPrincipal", listaDetallePedido.get(i).getCodArticuloPrincipal()); //es este??
            jsonObjItem.addProperty("comentario", listaDetallePedido.get(i).getComentario()); //Vacio al enviar
            jsonObjItem.addProperty("estadoArticulo", listaDetallePedido.get(i).getEstadoArticulo());  // desArticulo ????? NO TIENE SENTIDO!!! NO SERA estAriculo???

            jsonArrayDetalle.add(jsonObjItem);
        }

        return jsonArrayDetalle;
    }
}
