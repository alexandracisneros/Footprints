package com.neversoft.smartwaiter.model.business;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.neversoft.smartwaiter.database.DBHelper;
import com.neversoft.smartwaiter.database.DBHelper.Concepto;
import com.neversoft.smartwaiter.database.DBHelper.DetallePedido;
import com.neversoft.smartwaiter.database.DBHelper.Tables;
import com.neversoft.smartwaiter.model.entity.DetallePedidoEE;
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
        int rowCountUpdate = 0;
        DBHelper dbHelper;
        SQLiteDatabase db = null;
        try {
            dbHelper = DBHelper.getInstance(DetallePedidoDAO.this.mContext);
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            List<String> IDsArray = new ArrayList();

            for (int i = 0; i < jsonPedidosDespachados.size(); i++) {
                JsonObject jsonObjPed = jsonPedidosDespachados.get(i).getAsJsonObject();
                String idPedido = jsonObjPed.get("idPedOri").getAsString();
                String idPedidoServidor = jsonObjPed.get("idPed").getAsString();
                String cantRecoger = jsonObjPed.get("cantrec").getAsString();
                IDsArray = getIdsDetalleActualizar(jsonObjPed.get("detalle").getAsJsonArray());
                PedidoDAO.updatePedidoRecoger(idPedido, idPedidoServidor, cantRecoger, db);
                updateEstadoArticulo(idPedido, IDsArray, estadoOriginal, nuevoEstado, db);

            }
            db.setTransactionSuccessful();

        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
        return rowCountUpdate;
    }

    public int confirmRecojoItemsPedido(String idPedido, List<String> IDsArray) throws Exception {
        int rowCountUpdate = 0;
        DBHelper dbHelper;
        SQLiteDatabase db = null;
        try {
            dbHelper = DBHelper.getInstance(DetallePedidoDAO.this.mContext);
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            updateEstadoArticulo(idPedido, IDsArray, 2, 3, db);
            db.setTransactionSuccessful();
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
        return rowCountUpdate;
    }

    private void updateEstadoArticulo(String idPedido, List<String> IDsArray, int estadoOriginal, int nuevoEstado, SQLiteDatabase db) throws Exception {

        ContentValues cv = new ContentValues();
        cv.put(DetallePedido.ESTADO_ART, nuevoEstado);
        String updateWhere;
        String[] updateWhereArgs;

        updateWhere = DetallePedido.ITEM + " IN (" + Funciones.makePlaceholders(IDsArray.size())
                + ") AND " + DetallePedido.PEDIDO_ID + "=? AND " + DetallePedido.ESTADO_ART + "=?";
        IDsArray.add(idPedido);
        IDsArray.add(String.valueOf(estadoOriginal));

        updateWhereArgs = IDsArray.toArray(new String[IDsArray.size()]);

        db.update(Tables.DETALLE_PEDIDO, cv, updateWhere, updateWhereArgs);

    }

    private List<String> getIdsDetalleActualizar(JsonArray items) {
        List<String> IDsArray = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            JsonObject item = items.get(i).getAsJsonObject();
            IDsArray.add(item.get("item").getAsString());
        }
        return IDsArray;
    }

    public List<DetallePedidoEE> getDetallePorEstado(String idPedido, int estado) throws Exception {

        List<DetallePedidoEE> lista = new ArrayList<>();
        DBHelper dbHelper;
        SQLiteDatabase db = null;
        try {
            dbHelper = DBHelper.getInstance(DetallePedidoDAO.this.mContext);
            db = dbHelper.getReadableDatabase();
            String where = DetallePedido.PEDIDO_ID + "=? ";
            ArrayList<String> whereArgsArrayList = new ArrayList<String>();
            whereArgsArrayList.add(idPedido);
            if (estado != -1) {
                where += " AND " + DetallePedido.ESTADO_ART + "=? ";
                whereArgsArrayList.add(String.valueOf(estado));
            }
            String[] whereArgs = new String[whereArgsArrayList.size()];
            whereArgs = whereArgsArrayList.toArray(whereArgs);
            Cursor cursor = db.query(true, Tables.DETALLE_PEDIDO, null, where,
                    whereArgs, null, null, null, null);

            //TODO:  DETALLE_PEDIDO JOIN CONCPETO ON estado_articulo=cod AND tipo=2
            while (cursor.moveToNext()) {
                DetallePedidoEE item = new DetallePedidoEE();
                item.setId(cursor.getInt(cursor.getColumnIndex(DetallePedido.ID)));
                item.setItem(cursor.getInt(cursor.getColumnIndex(DetallePedido.ITEM)));
                item.setPedidoId(cursor.getInt(cursor.getColumnIndex(DetallePedido.PEDIDO_ID)));
                item.setCodArticulo(cursor.getInt(cursor.getColumnIndex(DetallePedido.COD_ART)));
                item.setUm(cursor.getString(cursor.getColumnIndex(DetallePedido.UM)));
                item.setCantidad(cursor.getFloat(cursor.getColumnIndex(DetallePedido.CANTIDAD)));
                item.setPrecio(cursor.getFloat(cursor.getColumnIndex(DetallePedido.PRECIO)));
                item.setTipoArticulo(cursor.getInt(cursor.getColumnIndex(DetallePedido.TIPO_ART)));
                item.setEstadoArticulo(cursor.getInt(cursor.getColumnIndex(DetallePedido.ESTADO_ART)));
                item.setDescEstadoArticulo(cursor.getString(cursor.getColumnIndex(DetallePedido.DESC_ART   )));
                item.setDescArticulo(cursor.getString(cursor.getColumnIndex(DetallePedido.DESC_ART)));
                lista.add(item);
            }
            cursor.close();
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return lista;

        //SELECT * FROM detalle_pedido
        //WHERE pedido_id = 1 AND estado_articulo=3 --3 recogido

    }
}
