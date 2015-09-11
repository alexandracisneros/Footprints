package com.neversoft.smartwaiter.model.business;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.neversoft.smartwaiter.database.SmartWaiterDB;
import com.neversoft.smartwaiter.database.SmartWaiterDB.Articulo;
import com.neversoft.smartwaiter.database.SmartWaiterDB.Carta;
import com.neversoft.smartwaiter.database.SmartWaiterDB.Tables;
import com.neversoft.smartwaiter.model.entity.ArticuloEE;
import com.neversoft.smartwaiter.ui.ArticuloItemAdapter;
import com.neversoft.smartwaiter.ui.TomarPedidoActivity;

import java.lang.ref.WeakReference;
import java.text.Normalizer;
import java.util.Locale;

/**
 * Created by Usuario on 03/09/2015.
 */
public class ArticuloDAO {
    private Context mContext;

    public ArticuloDAO(Context context) {
        this.mContext = context;
    }

    public int saveArticuloPrecioData(JsonArray jsonArrayArticulo) throws Exception {
        int numInserted = 0;
        SmartWaiterDB db = new SmartWaiterDB(this.mContext);
        String insertQuery = "INSERT INTO " + Tables.ARTICULO + "( " +
                Articulo.ID + "," + Articulo.DESCRIPCION + "," +
                Articulo.DESCRIPCION_NORM + "," + Articulo.UM + "," +
                Articulo.UM_DESC + "," + Articulo.PRECIO + "," +
                Articulo.COD_LISTAPRECIO + "," + Articulo.URL + ") " +
                "VALUES (?,?,?,?,?,?,?,?)";
        try {
            db.openWriteableDB();
            SQLiteStatement stmt = db.compileStatement(insertQuery);
            db.getDb().beginTransaction();
            if (jsonArrayArticulo.size() > 0) {
                for (int i = 0; i < jsonArrayArticulo.size(); i++) {
                    JsonObject jsonObjItem = jsonArrayArticulo.get(i).getAsJsonObject();
                    stmt.clearBindings();
                    stmt.bindLong(1, jsonObjItem.get("CODART").getAsLong());
                    stmt.bindString(2, jsonObjItem.get("desart").getAsString());
                    String stringToStore = Normalizer.normalize(jsonObjItem.get("desart").getAsString()
                                    .toLowerCase(Locale.getDefault()),
                            Normalizer.Form.NFC);
                    stmt.bindString(3, stringToStore);
                    stmt.bindString(4, jsonObjItem.get("UM").getAsString());
                    stmt.bindString(5, jsonObjItem.get("desum").getAsString());
                    stmt.bindDouble(6, jsonObjItem.get("PRECIO").getAsDouble());
                    stmt.bindLong(7, jsonObjItem.get("idlistaprecio").getAsInt());
                    stmt.bindString(8, jsonObjItem.get("url").getAsString());
                    stmt.execute();
                }
                db.getDb().setTransactionSuccessful();
                numInserted = jsonArrayArticulo.size();
            } else {
                throw new Exception("No hay 'Articulos'.");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            db.getDb().endTransaction();
            db.getDb().close();
        }
        return numInserted;
    }

    public void getArticuloPorFamiliaAsync(final WeakReference<Activity> mReference, final int familiaId) {
        final Activity activity = mReference.get();
        final SmartWaiterDB db = new SmartWaiterDB(ArticuloDAO.this.mContext);
        new AsyncTask<Void, Void, Cursor>() {
            @Override
            protected Cursor doInBackground(Void... params) {
                db.openReadableDB();
                Cursor cursor = db.query(true, Tables.ARTICULOS_JOIN_CARTA, ArticulosQuery.PROJECTION, Tables.CARTA + "." + Carta.COD_FAMILIA + "=?",
                        new String[]{Long.toString(familiaId)}, null, null, null, null);
                return cursor;
            }

            @Override
            protected void onPostExecute(Cursor cursor) {
                while (cursor.moveToNext()) {
                    ArticuloEE item = new ArticuloEE();
                    item.setId(cursor.getInt(ArticulosQuery.ID));
                    item.setDescripcionNorm(cursor.getString(ArticulosQuery.DESCRIPCION_NORM));
                    item.setUm(cursor.getString(ArticulosQuery.UM));
                    item.setUmDescripcion(cursor.getString(ArticulosQuery.UM_DESC));
                    item.setPrecio(cursor.getFloat(ArticulosQuery.PRECIO));
                    item.setUrl(cursor.getString(ArticulosQuery.URL));

                    ((TomarPedidoActivity) activity).getListaArticulos().add(item);
                }
                cursor.close();
                db.closeDB();
                ((TomarPedidoActivity) activity).getArticulosListView().setAdapter(
                        new ArticuloItemAdapter(ArticuloDAO.this.mContext, ((TomarPedidoActivity) activity).getListaArticulos()));


            }
        }.execute();
    }

    private interface ArticulosQuery {
        String[] PROJECTION = {
                Tables.ARTICULO + "." + Articulo.ID,
                Articulo.DESCRIPCION_NORM,
                Articulo.UM,
                Articulo.UM_DESC,
                Articulo.PRECIO,
                Articulo.URL
        };
        int ID = 0;
        int DESCRIPCION_NORM = 1;
        int UM = 2;
        int UM_DESC = 3;
        int PRECIO = 4;
        int URL = 5;
    }
}
