package com.neversoft.smartwaiter.model.business;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.neversoft.smartwaiter.database.DBHelper;
import com.neversoft.smartwaiter.database.DBHelper.Articulo;
import com.neversoft.smartwaiter.database.DBHelper.Carta;
import com.neversoft.smartwaiter.database.DBHelper.Tables;
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
        String insertQuery = "INSERT INTO " + Tables.ARTICULO + "( " +
                Articulo.ID + "," + Articulo.DESCRIPCION + "," +
                Articulo.DESCRIPCION_NORM + "," + Articulo.UM + "," +
                Articulo.UM_DESC + "," + Articulo.PRECIO + "," + Articulo.URL + ") " +
                "VALUES (?,?,?,?,?,?,?)";
        DBHelper dbHelper;
        SQLiteDatabase db=null;
        try {
            dbHelper=DBHelper.getInstance(ArticuloDAO.this.mContext);
            db=dbHelper.getWritableDatabase();
            SQLiteStatement stmt = db.compileStatement(insertQuery);
            db.beginTransaction();
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
                    stmt.bindString(7, jsonObjItem.get("url").getAsString());
                    stmt.execute();
                }
                db.setTransactionSuccessful();
                numInserted = jsonArrayArticulo.size();
            } else {
                throw new Exception("No hay 'Articulos'.");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if(db!=null) {
                db.endTransaction();
                db.close();
            }
        }
        return numInserted;
    }

    public void getArticuloPorFamiliaAsync(final WeakReference<Activity> mReference, final int familiaId) {
        final Activity activity = mReference.get();
        final DBHelper dbHelper=DBHelper.getInstance(ArticuloDAO.this.mContext);
        final SQLiteDatabase db=dbHelper.getReadableDatabase();
        new AsyncTask<Void, Void, Cursor>() {
            @Override
            protected Cursor doInBackground(Void... params) {
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
                db.close();
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
