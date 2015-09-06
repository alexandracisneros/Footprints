package com.neversoft.smartwaiter.model.business;

import android.content.Context;
import android.database.sqlite.SQLiteStatement;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.neversoft.smartwaiter.database.SmartWaiterDB;
import com.neversoft.smartwaiter.database.SmartWaiterDB.MesaPiso;
import com.neversoft.smartwaiter.database.SmartWaiterDB.Tables;

/**
 * Created by Usuario on 05/09/2015.
 */
public class MesaPisoDAO {
    private Context mContext;

    public MesaPisoDAO(Context context) {
        mContext = context;
    }

    public int saveMesaData(JsonArray jsonArrayMesa) throws Exception {

        int numInserted = 0;
        SmartWaiterDB db = new SmartWaiterDB(this.mContext);
        String insertQuery = "INSERT INTO " + Tables.MESA_PISO + "( " +
                MesaPiso.NRO_PISO + "," + MesaPiso.COD_AMBIENTE + "," +
                MesaPiso.DESC_AMBIENTE + "," + MesaPiso.NRO_MESA + "," +
                MesaPiso.NRO_ASIENTOS + "," + MesaPiso.COD_ESTADO_MESA + "," +
                MesaPiso.DESC_ESTADO_MESA + "," + MesaPiso.COD_RESERVA + " ) " +
                "VALUES (?,?,?,?,?,?,?,?)";
        try {
            db.openWriteableDB();
            SQLiteStatement statement = db.compileStatement(insertQuery);
            db.getDb().beginTransaction();
            if (jsonArrayMesa.size() > 0) {
                for (int i = 0; i < jsonArrayMesa.size(); i++) {
                    JsonObject jsonObjItem = jsonArrayMesa.get(i).getAsJsonObject();
                    statement.clearBindings();
                    statement.bindLong(1, jsonObjItem.get("NROPISO").getAsInt());
                    statement.bindLong(2, jsonObjItem.get("CAMBIENTE").getAsInt());
                    statement.bindString(3, jsonObjItem.get("DAMBIENTE").getAsString());
                    statement.bindLong(4, jsonObjItem.get("NROMESA").getAsLong());
                    statement.bindLong(5, jsonObjItem.get("NROASIENTOS").getAsLong());
                    statement.bindString(6, jsonObjItem.get("CEMESA").getAsString());
                    statement.bindString(7, jsonObjItem.get("DEMESA").getAsString());
                    statement.bindLong(8, jsonObjItem.get("CODRESERVA").getAsLong());
                    statement.execute();
                }
                db.getDb().setTransactionSuccessful();
                numInserted = jsonArrayMesa.size();
            } else {
                throw new Exception("No hay 'Mesas'.");
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
