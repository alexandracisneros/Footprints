package com.neversoft.smartwaiter.model.business;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.neversoft.smartwaiter.database.SmartWaiterDB;
import com.neversoft.smartwaiter.database.SmartWaiterDB.DetallePedido;
import com.neversoft.smartwaiter.database.SmartWaiterDB.Pedido;
import com.neversoft.smartwaiter.model.entity.DetallePedidoEE;
import com.neversoft.smartwaiter.model.entity.PedidoEE;

import java.lang.ref.WeakReference;


/**
 * Created by Usuario on 15/09/2015.
 */
public class PedidoDAO {
    private Context mContext;

    public PedidoDAO(Context context) {
        this.mContext = context;
    }

    public void savePedido(final WeakReference<Activity> mReference, final PedidoEE pedido) {
        final Activity activity = mReference.get();
        final SmartWaiterDB db = new SmartWaiterDB(PedidoDAO.this.mContext);
        new AsyncTask<Void, Void, Object>() {
            @Override
            protected Object doInBackground(Void... voids) {
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
                            cvItem.put(DetallePedido.DESC_ART,det.getDescArticulo());
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
                    return e;
                } finally {
                    db.getDb().endTransaction();
                    db.closeDB();
                }

            }

            @Override
            protected void onPostExecute(Object result) {
                if (result instanceof Long) {
                    Toast.makeText(activity, "Operación completada con exito. Id =" + result, Toast.LENGTH_SHORT).show();
                } else if (result instanceof Exception) {
                    Toast.makeText(activity, "Se produjo la excepción: " + result, Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }
}
