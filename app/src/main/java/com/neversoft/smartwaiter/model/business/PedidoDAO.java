package com.neversoft.smartwaiter.model.business;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.neversoft.smartwaiter.database.DBHelper;
import com.neversoft.smartwaiter.database.DBHelper.DetallePedido;
import com.neversoft.smartwaiter.database.DBHelper.Pedido;
import com.neversoft.smartwaiter.database.DBHelper.Tables;
import com.neversoft.smartwaiter.model.entity.DetallePedidoEE;
import com.neversoft.smartwaiter.model.entity.PedidoEE;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Usuario on 15/09/2015.
 */
public class PedidoDAO {
    private Context mContext;

    public PedidoDAO(Context context) {
        this.mContext = context;
    }

    public static void updatePedidoRecoger(String idPedido, String idPedidoServidor, String nuevaCantidad, SQLiteDatabase db) throws Exception {

        ContentValues cv = new ContentValues();
        cv.put(Pedido.CANT_RECOGIDA, nuevaCantidad.trim());
        cv.put(Pedido.NRO_PED_SERVIDOR, idPedidoServidor);
        String updateWhere = null;
        String[] updateWhereArgs = null;


        updateWhere = Pedido.ID + " =? ";
        updateWhereArgs = new String[]{idPedido};

        db.update(Tables.PEDIDO, cv, updateWhere,
                updateWhereArgs);
    }

    public long savePedido(final PedidoEE pedido, int estadoArticulo) throws Exception {

        long result = 0;
        ContentValues cvPedido = new ContentValues();
        ContentValues cvItem;
        cvPedido.put(Pedido.FECHA, pedido.getFecha());
        cvPedido.put(Pedido.NRO_MESA, pedido.getNroMesa());
        cvPedido.put(Pedido.NRO_PISO, pedido.getNroPiso());
        cvPedido.put(Pedido.CANT_RECOGIDA, pedido.getCantRecogida());
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
        DBHelper dbHelper;
        SQLiteDatabase db = null;
        try {

            dbHelper = DBHelper.getInstance(PedidoDAO.this.mContext);
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            //TODO :CREO QUE VOY A NECESITAR ESTO http://stackoverflow.com/questions/418898/sqlite-upsert-not-insert-or-replace/4330694#4330694
            long idPedido = db.insertOrThrow(Tables.PEDIDO, null, cvPedido);
            long idItemPedido = 0;
            if (idPedido > 0) {
                int count = 1;
                for (DetallePedidoEE det : pedido.getDetalle()) {
                    cvItem = new ContentValues();
                    cvItem.put(DetallePedido.PEDIDO_ID, idPedido);
                    cvItem.put(DetallePedido.ITEM, count);
                    cvItem.put(DetallePedido.COD_ART, det.getCodArticulo());
                    cvItem.put(DetallePedido.UM, det.getUm());
                    cvItem.put(DetallePedido.CANTIDAD, det.getCantidad());
                    cvItem.put(DetallePedido.PRECIO, det.getPrecio());
                    cvItem.put(DetallePedido.TIPO_ART, det.getTipoArticulo());
                    cvItem.put(DetallePedido.COD_ART_PRINCIPAL, det.getCodArticuloPrincipal());
                    cvItem.put(DetallePedido.COMENTARIO, det.getComentario());
                    cvItem.put(DetallePedido.ESTADO_ART, estadoArticulo);
                    cvItem.put(DetallePedido.DESC_ART, det.getDescArticulo());
                    idItemPedido = db.insertOrThrow(Tables.DETALLE_PEDIDO, null,
                            cvItem);
                    count++;
                }
                if (idItemPedido > 0) {
                    result = idPedido;
                    db.setTransactionSuccessful();
                }

            }
            return result;

        } catch (Exception e) {
            throw e;
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
    }

    public long getNumeroPedidos(int estadoEnviado) throws Exception {
        long count = 0;
        DBHelper dbHelper;
        SQLiteDatabase db = null;
        try {
            dbHelper = DBHelper.getInstance(PedidoDAO.this.mContext);
            db = dbHelper.getReadableDatabase();
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
            count = dbHelper.count(Tables.PEDIDO, where, whereArgs);

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return count;
    }

    public List<PedidoEE> getPedidosDespachados() throws Exception {
        List<PedidoEE> listaPedido = new ArrayList<>();
        DBHelper dbHelper;
        SQLiteDatabase db = null;
        try {
            dbHelper = DBHelper.getInstance(PedidoDAO.this.mContext);
            db = dbHelper.getReadableDatabase();
            String query = "SELECT * FROM " + Tables.PEDIDO +
                    "  WHERE (" +
                    " SELECT COUNT(*) FROM " + Tables.DETALLE_PEDIDO +
                    "  WHERE " + Tables.PEDIDO + "." + Pedido.ID + "= " + Tables.DETALLE_PEDIDO + "." + DetallePedido.PEDIDO_ID
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
                ped.setNroPedidoServidor(cursor.getInt(cursor.getColumnIndex(Pedido.NRO_PED_SERVIDOR)));
                listaPedido.add(ped);
            }
            cursor.close();

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return listaPedido;
    }

    public List<PedidoEE> getPedidosPorFacturar() throws Exception {
        List<PedidoEE> listaPedido = new ArrayList<>();
        DBHelper dbHelper;
        SQLiteDatabase db = null;
        try {
            dbHelper = DBHelper.getInstance(PedidoDAO.this.mContext);
            db = dbHelper.getReadableDatabase();
            String query = "SELECT * FROM " + Tables.PEDIDO +
                    "  WHERE (" +
                    " SELECT COUNT(*) FROM " + Tables.DETALLE_PEDIDO +
                    "  WHERE " + Tables.PEDIDO + "." + Pedido.ID + "= " + Tables.DETALLE_PEDIDO + "." + DetallePedido.PEDIDO_ID
                    + " AND " + DetallePedido.ESTADO_ART + " = ?" +
                    "    ) > 0 AND " + Pedido.ESTADO + " =?";
            Cursor cursor = db.rawQuery(query, new String[]{"3", "020"});
            while (cursor.moveToNext()) {
                PedidoEE ped = new PedidoEE();
                ped.setId(cursor.getInt(cursor.getColumnIndex(Pedido.ID)));
                ped.setNroMesa(cursor.getInt(cursor.getColumnIndex(Pedido.NRO_MESA)));
                ped.setNroPiso(cursor.getInt(cursor.getColumnIndex(Pedido.NRO_PISO)));
                ped.setAmbiente(cursor.getInt(cursor.getColumnIndex(Pedido.AMBIENTE)));
                ped.setCantRecogida(cursor.getString(cursor.getColumnIndex(Pedido.CANT_RECOGIDA)));
                ped.setCodCliente(cursor.getInt(cursor.getColumnIndex(Pedido.CODIGO_CLIENTE)));
                ped.setMontoTotal(cursor.getFloat(cursor.getColumnIndex(Pedido.MONTO_TOTAL)));
                ped.setNroPedidoServidor(cursor.getInt(cursor.getColumnIndex(Pedido.NRO_PED_SERVIDOR)));
                listaPedido.add(ped);
            }
            cursor.close();

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return listaPedido;
    }

    public int updateEstadoPedidoDetalle(int idPedido, String estadoPedido, int estadoArtActual, int estadoArtNuevo) {
        int resultDetalle = 0;
        int resultPedido = 0;
        DBHelper dbHelper;
        SQLiteDatabase db = null;

        ContentValues cvPedido = new ContentValues();
        cvPedido.put(Pedido.ESTADO, estadoPedido);
        String updateWherePedido = Pedido.ID + " =? ";
        String[] updateWhereArgsPedido = new String[]{String.valueOf(idPedido)};

        ContentValues cvItem = new ContentValues();
        cvItem.put(DetallePedido.ESTADO_ART, estadoArtNuevo);
        String updateWhereItem = DetallePedido.PEDIDO_ID + " =? "; //TODO : SI NECESITA SOLO LOS DE UN ESTADO USAR 'estadoArtActual' como una condicional adicional
        String[] updateWhereArgsItem = new String[]{String.valueOf(idPedido)};


        try {
            dbHelper = DBHelper.getInstance(PedidoDAO.this.mContext);
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();


            resultDetalle = db.update(Tables.DETALLE_PEDIDO, cvItem, updateWhereItem, updateWhereArgsItem);
            if (resultDetalle > 0) {
                resultPedido = db.update(Tables.PEDIDO, cvPedido, updateWherePedido, updateWhereArgsPedido);
            }

            if (resultPedido > 0) {
                db.setTransactionSuccessful();
            }
            return resultPedido;
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
    }


}
