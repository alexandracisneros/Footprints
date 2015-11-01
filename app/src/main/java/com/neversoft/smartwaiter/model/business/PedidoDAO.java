package com.neversoft.smartwaiter.model.business;


import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.neversoft.smartwaiter.database.SmartWaiterDB;
import com.neversoft.smartwaiter.database.SmartWaiterDB.DetallePedido;
import com.neversoft.smartwaiter.database.SmartWaiterDB.Pedido;
import com.neversoft.smartwaiter.model.entity.DetallePedidoEE;
import com.neversoft.smartwaiter.model.entity.PedidoEE;

import java.util.ArrayList;


/**
 * Created by Usuario on 15/09/2015.
 */
public class PedidoDAO {
    private Context mContext;

    public PedidoDAO(Context context) {
        this.mContext = context;
    }

    public long savePedido(final PedidoEE pedido) throws Exception {
        final SmartWaiterDB db = new SmartWaiterDB(PedidoDAO.this.mContext);

        long result = 0;
        ContentValues cvPedido = new ContentValues();
        ContentValues cvItem;
        cvPedido.put(Pedido.FECHA, pedido.getFecha());
        cvPedido.put(Pedido.NRO_MESA, pedido.getNroMesa());
        cvPedido.put(Pedido.AMBIENTE, pedido.getAmbiente());
        cvPedido.put(Pedido.CODIGO_USUARIO, pedido.getCodUsuario());
        cvPedido.put(Pedido.CODIGO_CLIENTE, pedido.getCodCliente());
        cvPedido.put(Pedido.TIPO_VENTA, pedido.getTipoVenta());
        cvPedido.put(Pedido.TIPO_PAGO, pedido.getTipoPago());
        cvPedido.put(Pedido.MONEDA, pedido.getMoneda());
        cvPedido.put(Pedido.MONTO_TOTAL, pedido.getMontoTotal());
        cvPedido.put(Pedido.MONTO_RECIBIDO, pedido.getMontoRecibido());
        cvPedido.put(Pedido.ESTADO, pedido.getEstado());
        cvPedido.put(Pedido.CODIGO_CIA, pedido.getCodCia());
        try {

            db.openWriteableDB();
            db.getDb().beginTransaction();
            long idPedido = db.insertOrThrow(SmartWaiterDB.Tables.PEDIDO, null, cvPedido);
            long idItemPedido = 0;
            if (idPedido > 0) {
                for (DetallePedidoEE det : pedido.getDetalle()) {
                    cvItem = new ContentValues();
                    cvItem.put(DetallePedido.PEDIDO_ID, idPedido);
                    cvItem.put(DetallePedido.COD_ART, det.getCodArticulo());
                    cvItem.put(DetallePedido.UM, det.getUm());
                    cvItem.put(DetallePedido.CANTIDAD, det.getCantidad());
                    cvItem.put(DetallePedido.PRECIO, det.getPrecio());
                    cvItem.put(DetallePedido.TIPO_ART, det.getTipoArticulo());
                    cvItem.put(DetallePedido.COD_ART_PRINCIPAL, det.getCodArticuloPrincipal());
                    cvItem.put(DetallePedido.COMENTARIO, det.getComentario());
                    cvItem.put(DetallePedido.ESTADO_ART, det.getEstadoArticulo());
                    cvItem.put(DetallePedido.DESC_ART, det.getDescArticulo());
                    idItemPedido = db.insertOrThrow(SmartWaiterDB.Tables.DETALLE_PEDIDO, null,
                            cvItem);
                }
                if (idItemPedido > 0) {
                    result = idPedido;
                    db.getDb().setTransactionSuccessful();
                }

            }
            return result;

        } catch (Exception e) {
            throw e;
        } finally {
            db.getDb().endTransaction();
            db.closeDB();
        }


//                    Toast.makeText(activity, "Operación completada con exito. Id =" + result, Toast.LENGTH_SHORT).show();
//
//                    Toast.makeText(activity, "Se produjo la excepción: " + result, Toast.LENGTH_SHORT).show();


    }

    public long getNumeroPedidos(int estadoEnviado) throws Exception {
        final SmartWaiterDB db = new SmartWaiterDB(PedidoDAO.this.mContext);
        long count = 0;
        try {
            db.openReadableDB();
            // estadoEnviado=-1 =No tener en cuenta estado
            // estadoEnviado= 0 =Pedidos que no han sido enviados
            String where = Pedido.CONFIRMADO + " =?";

            ArrayList<String> whereArgsArrayList = new ArrayList<String>();
            if (estadoEnviado != -1) {
                where += " AND " + Pedido.ENVIADO + " =?";
                whereArgsArrayList.add("1");
                whereArgsArrayList.add(String.valueOf(estadoEnviado));
            } else {
                whereArgsArrayList.add("1");
            }
            String[] whereArgs = new String[whereArgsArrayList.size()];
            whereArgs = whereArgsArrayList.toArray(whereArgs);
            count = db.count(SmartWaiterDB.Tables.PEDIDO, where, whereArgs);

        } finally {
            db.closeDB();
        }
        return count;
    }

}
