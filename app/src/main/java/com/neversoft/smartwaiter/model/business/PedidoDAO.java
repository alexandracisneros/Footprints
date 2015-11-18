package com.neversoft.smartwaiter.model.business;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.neversoft.smartwaiter.database.SmartWaiterDB;
import com.neversoft.smartwaiter.database.SmartWaiterDB.DetallePedido;
import com.neversoft.smartwaiter.database.SmartWaiterDB.Pedido;
import com.neversoft.smartwaiter.model.entity.DetallePedidoEE;
import com.neversoft.smartwaiter.model.entity.PedidoEE;
import com.neversoft.smartwaiter.util.Funciones;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by Usuario on 15/09/2015.
 */
public class PedidoDAO {
    private Context mContext;

    public PedidoDAO(Context context) {
        this.mContext = context;
    }

    public long savePedido(final PedidoEE pedido, int estadoArticulo) throws Exception {
        final SmartWaiterDB db = new SmartWaiterDB(PedidoDAO.this.mContext);

        long result = 0;
        ContentValues cvPedido = new ContentValues();
        ContentValues cvItem;
        cvPedido.put(Pedido.FECHA, pedido.getFecha());
        cvPedido.put(Pedido.NRO_MESA, pedido.getNroMesa());
        cvPedido.put(Pedido.NRO_PISO,pedido.getNroPiso());
        cvPedido.put(Pedido.CANT_RECOGIDA,pedido.getCantRecogida());
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
            //TODO :CREO QUE VOY A NECESITAR ESTO http://stackoverflow.com/questions/418898/sqlite-upsert-not-insert-or-replace/4330694#4330694
            long idPedido = db.insertOrThrow(SmartWaiterDB.Tables.PEDIDO, null, cvPedido);
            long idItemPedido = 0;
            if (idPedido > 0) {
                int count=1;
                for (DetallePedidoEE det : pedido.getDetalle()) {
                    cvItem = new ContentValues();
                    cvItem.put(DetallePedido.PEDIDO_ID, idPedido);
                    cvItem.put(DetallePedido.ITEM,count);
                    cvItem.put(DetallePedido.COD_ART, det.getCodArticulo());
                    cvItem.put(DetallePedido.UM, det.getUm());
                    cvItem.put(DetallePedido.CANTIDAD, det.getCantidad());
                    cvItem.put(DetallePedido.PRECIO, det.getPrecio());
                    cvItem.put(DetallePedido.TIPO_ART, det.getTipoArticulo());
                    cvItem.put(DetallePedido.COD_ART_PRINCIPAL, det.getCodArticuloPrincipal());
                    cvItem.put(DetallePedido.COMENTARIO, det.getComentario());
                    cvItem.put(DetallePedido.ESTADO_ART, estadoArticulo);
                    cvItem.put(DetallePedido.DESC_ART, det.getDescArticulo());
                    idItemPedido = db.insertOrThrow(SmartWaiterDB.Tables.DETALLE_PEDIDO, null,
                            cvItem);
                    count++;
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

    public List<PedidoEE> getPedidosDespachados() throws Exception {
        final SmartWaiterDB db = new SmartWaiterDB(PedidoDAO.this.mContext);
        List<PedidoEE> listaPedido = new ArrayList<>();
        try {
            db.openReadableDB();
            String query = "SELECT * FROM " + SmartWaiterDB.Tables.PEDIDO +
                    "  WHERE (" +
                    " SELECT COUNT(*) FROM " + SmartWaiterDB.Tables.DETALLE_PEDIDO +
                    "  WHERE " + SmartWaiterDB.Tables.PEDIDO + "." + Pedido.ID + "= " + SmartWaiterDB.Tables.DETALLE_PEDIDO + "." + DetallePedido.PEDIDO_ID
                    + " AND " + DetallePedido.ESTADO_ART + " = ?" +
                    "    ) > 0";
            Cursor cursor = db.rawQuery(query, new String[]{"2"});
            while (cursor.moveToNext()) {
                PedidoEE ped = new PedidoEE();
                //TODO: Aca seria bueno tener la fecha es que se actualizo a despachado de cocina para order por esa fecha
                ped.setId(cursor.getInt(cursor.getColumnIndex(Pedido.ID)));
                ped.setNroMesa(cursor.getInt(cursor.getColumnIndex(Pedido.NRO_MESA)));
                ped.setNroPiso(cursor.getInt(cursor.getColumnIndex(Pedido.NRO_PISO)));
                ped.setAmbiente(cursor.getInt(cursor.getColumnIndex(Pedido.AMBIENTE)));
                ped.setCantRecogida(cursor.getString(cursor.getColumnIndex(Pedido.CANT_RECOGIDA)));
                ped.setCodCliente(cursor.getInt(cursor.getColumnIndex(Pedido.CODIGO_CLIENTE)));
                listaPedido.add(ped);
            }
            cursor.close();

        } finally {
            db.closeDB();
        }
        return listaPedido;
    }
    public static void updateCantidadRecoger(String idPedido,String nuevaCantidad, SmartWaiterDB db) throws Exception {
        ContentValues cv = new ContentValues();
        cv.put(SmartWaiterDB.Pedido.CANT_RECOGIDA, nuevaCantidad.trim());
        String updateWhere = null;
        String[] updateWhereArgs = null;


        updateWhere = SmartWaiterDB.Pedido.ID + " =? ";
        updateWhereArgs = new String[]{idPedido};

        db.update(SmartWaiterDB.Tables.PEDIDO, cv, updateWhere,
                updateWhereArgs);
    }

}
