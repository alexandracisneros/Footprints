package com.neversoft.smartwaiter.model.business;

import android.content.Context;
import android.database.sqlite.SQLiteStatement;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.neversoft.smartwaiter.database.SmartWaiterDB;
import com.neversoft.smartwaiter.database.SmartWaiterDB.Prioridad;
import com.neversoft.smartwaiter.database.SmartWaiterDB.Tables;

/**
 * Created by Usuario on 05/09/2015.
 */
public class PrioridadDAO {
    private Context mContext;

    public PrioridadDAO(Context context) {
        mContext = context;
    }

    public int savePrioridadData(JsonArray jsonArrayPrioridad) throws Exception {

        int numInserted = 0;
        SmartWaiterDB db = new SmartWaiterDB(this.mContext);
        String insertQuery = "INSERT INTO " + Tables.PRIORIDAD +
                " (" + Prioridad.CODIGO + "," + Prioridad.DESCRIPCION + ") VALUES (?,?)";
        try {
            db.openWriteableDB();
            SQLiteStatement statement = db.compileStatement(insertQuery);
            db.getDb().beginTransaction();
            if (jsonArrayPrioridad.size() > 0) {
                for (int i = 0; i < jsonArrayPrioridad.size(); i++) {
                    JsonObject jsonObjItem = jsonArrayPrioridad.get(i).getAsJsonObject();
                    statement.clearBindings();
                    statement.bindString(1, jsonObjItem.get("codelemento").getAsString());
                    statement.bindString(2, jsonObjItem.get("descripcion").getAsString());
                    statement.execute();
                }
                db.getDb().setTransactionSuccessful();
                numInserted = jsonArrayPrioridad.size();
            } else {
                throw new Exception("No hay 'Prioridades'.");
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
