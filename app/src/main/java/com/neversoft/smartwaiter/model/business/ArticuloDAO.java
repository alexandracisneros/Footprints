package com.neversoft.smartwaiter.model.business;

import android.content.Context;
import android.database.sqlite.SQLiteStatement;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.neversoft.smartwaiter.database.SmartWaiterDB;
import com.neversoft.smartwaiter.database.SmartWaiterDB.Articulo;
import com.neversoft.smartwaiter.database.SmartWaiterDB.Tables;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Usuario on 03/09/2015.
 */
public class ArticuloDAO {
    private SmartWaiterDB mDB;
    private Context mContext;

    public ArticuloDAO(Context context) {
        this.mDB = new SmartWaiterDB(context);
        this.mContext = context;
    }

    public int saveArticuloPrecioData(JsonArray jsonArrayArticulo) throws Exception {
        int numInserted = 0;
        String insertQuery = "INSERT INTO " + Tables.ARTICULO + "( " +
                Articulo.ID + "," + Articulo.DESCRIPCION + "," +
                Articulo.DESCRIPCION_NORM + "," + Articulo.UM + "," +
                Articulo.UM_DESC + "," + Articulo.PRECIO + "," +
                Articulo.COD_LISTAPRECIO + "," + Articulo.URL + ") " +
                "VALUES (?,?,?,?,?,?,?,?)";
        try {
            mDB.openWriteableDB();
            SQLiteStatement stmt = mDB.compileStatement(insertQuery);
            mDB.getDb().beginTransaction();
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
                mDB.getDb().setTransactionSuccessful();
                numInserted=jsonArrayArticulo.size();
            } else {
                throw new Exception("No hay 'Articulos'.");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            mDB.getDb().endTransaction();
            mDB.getDb().close();
        }
        return numInserted;
    }
}
