package com.neversoft.smartwaiter.model.business;

import android.content.Context;
import android.database.sqlite.SQLiteStatement;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.neversoft.smartwaiter.database.SmartWaiterDB;
import com.neversoft.smartwaiter.database.SmartWaiterDB.Carta;
import com.neversoft.smartwaiter.database.SmartWaiterDB.Tables;

/**
 * Created by Usuario on 05/09/2015.
 */
public class CartaDAO {
    private Context mContext;

    public CartaDAO(Context context) {
        mContext = context;
    }
    public int saveCartaData(JsonArray jsonArrayCarta) throws Exception {

        int numInserted = 0;
        SmartWaiterDB db = new SmartWaiterDB(this.mContext);
        String insertQuery="INSERT INTO " + Tables.CARTA + "( "+
                Carta.COD_FAMILIA + "," + Carta.COD_PRIORIDAD + "," +
                Carta.COD_ARTICULO + "," + Carta.COD_ARTICULO_PRINC + ") " +
                "VALUES (?,?,?,?)";
        try {
            db.openWriteableDB();
            SQLiteStatement statement = db.compileStatement(insertQuery);
            db.getDb().beginTransaction();
            if (jsonArrayCarta.size() > 0) {
                for (int i = 0; i < jsonArrayCarta.size(); i++) {
                    JsonObject jsonObjItem = jsonArrayCarta.get(i).getAsJsonObject();
                    statement.clearBindings();
                    statement.bindString(1, jsonObjItem.get("CODTIPO").getAsString());
                    statement.bindString(2, jsonObjItem.get("CODPRE").getAsString());
                    statement.bindLong(3, jsonObjItem.get("CODART").getAsInt());
                    statement.bindLong(4, jsonObjItem.get("CODPRIN").getAsInt());
                    statement.execute();
                }
                db.getDb().setTransactionSuccessful();
                numInserted = jsonArrayCarta.size();
            } else {
                throw new Exception("No hay 'Cartas'.");
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
