package com.neversoft.smartwaiter.model.business;

import android.content.Context;
import android.database.sqlite.SQLiteStatement;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.neversoft.smartwaiter.database.SmartWaiterDB;
import com.neversoft.smartwaiter.database.SmartWaiterDB.Familia;
import com.neversoft.smartwaiter.database.SmartWaiterDB.Tables;

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
}
