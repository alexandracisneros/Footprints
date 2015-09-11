package com.neversoft.smartwaiter.model.business;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.neversoft.smartwaiter.database.SmartWaiterDB;
import com.neversoft.smartwaiter.database.SmartWaiterDB.Familia;
import com.neversoft.smartwaiter.database.SmartWaiterDB.Tables;
import com.neversoft.smartwaiter.model.entity.CategoriaEE;
import com.neversoft.smartwaiter.ui.CategoriaItemAdapter;
import com.neversoft.smartwaiter.ui.TomarPedidoActivity;

import java.lang.ref.WeakReference;

/**
 * Created by Usuario on 05/09/2015.
 */
public class CategoriaDAO {
    private Context mContext;


    public CategoriaDAO(Context context) {
        mContext = context;
    }

    public int saveCategoriaData(JsonArray jsonArrayFamilia) throws Exception {

        int numInserted = 0;
        SmartWaiterDB db = new SmartWaiterDB(this.mContext);
        String insertQuery = "INSERT INTO " + Tables.FAMILIA + "(" +
                Familia.CODIGO + "," + Familia.DESCRIPCION + "," + Familia.URL +
                ") VALUES (?,?,?)";
        try {
            db.openWriteableDB();
            SQLiteStatement statement = db.compileStatement(insertQuery);
            db.getDb().beginTransaction();
            if (jsonArrayFamilia.size() > 0) {
                for (int i = 0; i < jsonArrayFamilia.size(); i++) {
                    JsonObject jsonObjItem = jsonArrayFamilia.get(i).getAsJsonObject();
                    statement.clearBindings();
                    statement.bindString(1, jsonObjItem.get("codelemento").getAsString());
                    statement.bindString(2, jsonObjItem.get("descripcion").getAsString());
                    statement.bindString(3, jsonObjItem.get("factorc").getAsString());
                    statement.execute();
                }
                db.getDb().setTransactionSuccessful();
                numInserted = jsonArrayFamilia.size();
            } else {
                throw new Exception("No hay 'Familias'.");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            db.getDb().endTransaction();
            db.getDb().close();
        }
        return numInserted;

    }

    public void getCategoriasAsync(final WeakReference<Activity> mReference) {
        final Activity activity = mReference.get();
        final SmartWaiterDB db = new SmartWaiterDB(CategoriaDAO.this.mContext);
        new AsyncTask<Void, Void, Cursor>() {
            @Override
            protected Cursor doInBackground(Void... params) {
                try {
                    db.openReadableDB();
                    Cursor cursor = db.query(true, Tables.FAMILIA, CategoriasQuery.PROJECTION, null, null, null, null, null, null);
                    return cursor;
                } finally {

                }
            }

            @Override
            protected void onPostExecute(Cursor cursor) {
                while (cursor.moveToNext()) {
                    CategoriaEE item = new CategoriaEE();
                    item.setId(cursor.getInt(CategoriasQuery.ID));
                    item.setCodigo(cursor.getString(CategoriasQuery.CODIGO));
                    item.setDescripcion(cursor.getString(CategoriasQuery.DESCRIPCION));
                    item.setUrl(cursor.getString(CategoriasQuery.URL));
                    ((TomarPedidoActivity) activity).getListaCategorias().add(item);
                }
                cursor.close();
                db.closeDB();
                ((TomarPedidoActivity) activity).getCategoriasListView().setAdapter(
                        new CategoriaItemAdapter(CategoriaDAO.this.mContext,((TomarPedidoActivity)activity).getListaCategorias()));
//                fragment.setAdapterCateg(new CategoriaItemAdapter(mContext,((TomarPedidoActivity)activity).getListaCategorias()));
//                fragment.getRecyclerViewCateg().setAdapter(fragment.getAdapterCateg());
                //By default load all the dishes under the first category

                //fragment.loadArticulosObject(familiaId);

            }
        }.execute();
    }

    private interface CategoriasQuery {
        String[] PROJECTION = {
                Familia.ID,
                Familia.CODIGO,
                Familia.DESCRIPCION,
                Familia.URL
        };
        int ID = 0;
        int CODIGO = 1;
        int DESCRIPCION = 2;
        int URL = 3;
    }
}
