package com.neversoft.smartwaiter.model.business;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.neversoft.smartwaiter.database.SmartWaiterDB;
import com.neversoft.smartwaiter.database.SmartWaiterDB.DetallePedido;
import com.neversoft.smartwaiter.model.entity.DetallePedidoEE;
import com.neversoft.smartwaiter.ui.SmartWaiter;
import com.neversoft.smartwaiter.util.Funciones;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Usuario on 12/11/2015.
 */
public class DetallePedidoDAO {
    private Context mContext;

    public DetallePedidoDAO(Context context) {
        mContext = context;
    }

    public int updateEstadoItemsPedido(JsonArray jsonPedidosDespachados, int estadoOriginal, int nuevoEstado) throws Exception {
        final SmartWaiterDB db = new SmartWaiterDB(DetallePedidoDAO.this.mContext);
        int rowCountUpdate = 0;

        try {
            db.openWriteableDB();
            db.getDb().beginTransaction();
            List<String> IDsArray = new ArrayList();

            for (int i = 0; i < jsonPedidosDespachados.size(); i++) {
                JsonObject jsonObjPed = jsonPedidosDespachados.get(i).getAsJsonObject();
                String idPedido = jsonObjPed.get("idPedOri").getAsString();
                String cantRecoger = jsonObjPed.get("cantrec").getAsString();
                IDsArray = getIdsDetalleActualizar(jsonObjPed.get("detalle").getAsJsonArray());
                PedidoDAO.updateCantidadRecoger(idPedido,cantRecoger,db);
                updateEstadoArticulo(idPedido, IDsArray, estadoOriginal, nuevoEstado, db);

            }
            db.getDb().setTransactionSuccessful();

        } finally {
            db.getDb().endTransaction();
            db.getDb().close();
        }
        return rowCountUpdate;
    }

    private void updateEstadoArticulo(String idPedido, List<String> IDsArray, int estadoOriginal, int nuevoEstado, SmartWaiterDB db) throws Exception {
        ContentValues cv = new ContentValues();
        cv.put(SmartWaiterDB.DetallePedido.ESTADO_ART, nuevoEstado);
        String updateWhere = null;
        String[] updateWhereArgs = null;


        updateWhere = DetallePedido. ITEM + " IN ("
                + Funciones.makePlaceholders(IDsArray.size()) + ") AND "
                + DetallePedido.PEDIDO_ID + "=? AND "
                + DetallePedido.ESTADO_ART + "=?";
        IDsArray.add(idPedido);
        IDsArray.add(String.valueOf(estadoOriginal));

        updateWhereArgs = IDsArray.toArray(new String[IDsArray.size()]);

        db.update(SmartWaiterDB.Tables.DETALLE_PEDIDO, cv, updateWhere,
                updateWhereArgs);
    }

    private List<String> getIdsDetalleActualizar(JsonArray items) {
        List<String> IDsArray = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            JsonObject item = items.get(i).getAsJsonObject();
            IDsArray.add(item.get("item").getAsString());
        }
        return IDsArray;
    }

    public List<DetallePedidoEE> getDetallePorEstado(int idPedido, int estado) throws Exception {
        final SmartWaiterDB db = new SmartWaiterDB(DetallePedidoDAO.this.mContext);
        List<DetallePedidoEE> lista = new ArrayList<>();
        try {
            db.openReadableDB();

            Cursor cursor = db.query(true, SmartWaiterDB.Tables.DETALLE_PEDIDO, null, DetallePedido.PEDIDO_ID + "=? and "
                            + DetallePedido.ESTADO_ART + "=? ",
                    new String[]{String.valueOf(idPedido),
                            String.valueOf(estado)}, null, null, null, null);
            while (cursor.moveToNext()) {
                DetallePedidoEE item = new DetallePedidoEE();
                item.setId(cursor.getInt(cursor.getColumnIndex(DetallePedido.ID)));
                item.setPedidoId(cursor.getInt(cursor.getColumnIndex(DetallePedido.PEDIDO_ID)));
                item.setCodArticulo(cursor.getInt(cursor.getColumnIndex(DetallePedido.COD_ART)));
                item.setUm(cursor.getString(cursor.getColumnIndex(DetallePedido.UM)));
                item.setCantidad(cursor.getFloat(cursor.getColumnIndex(DetallePedido.CANTIDAD)));
                item.setPrecio(cursor.getFloat(cursor.getColumnIndex(DetallePedido.PRECIO)));
                item.setTipoArticulo(cursor.getInt(cursor.getColumnIndex(DetallePedido.TIPO_ART)));
                item.setEstadoArticulo(cursor.getInt(cursor.getColumnIndex(DetallePedido.ESTADO_ART)));
                item.setDescArticulo(cursor.getString(cursor.getColumnIndex(DetallePedido.DESC_ART)));
                lista.add(item);
            }
            cursor.close();
        } finally {
            db.closeDB();
        }
        return lista;

    }
}
