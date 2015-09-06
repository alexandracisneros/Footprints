package com.neversoft.smartwaiter.model.business;

import android.content.Context;
import android.database.sqlite.SQLiteStatement;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.neversoft.smartwaiter.database.SmartWaiterDB;
import com.neversoft.smartwaiter.database.SmartWaiterDB.Cliente;
import com.neversoft.smartwaiter.database.SmartWaiterDB.Tables;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Created by Usuario on 05/09/2015.
 */
public class ClienteDAO {
    private Context mContext;

    public ClienteDAO(Context context) {
        mContext = context;
    }

    public int saveClienteData(JsonArray jsonArrayCliente) throws Exception {

        int numInserted = 0;
        SmartWaiterDB db = new SmartWaiterDB(this.mContext);
        String insertQuery = "INSERT INTO " + Tables.CLIENTE + "( " +
                Cliente.ID + "," +
                Cliente.RAZON_SOCIAL + "," + Cliente.RAZON_SOCIAL_NORM + "," +
                Cliente.TIPO_PERSONA + "," + Cliente.NRO_DOCUMENTO + "," +
                Cliente.DIRECCION + ") " +
                "VALUES (?,?,?,?,?,?)";
        try {
            db.openWriteableDB();
            SQLiteStatement statement = db.compileStatement(insertQuery);
            db.getDb().beginTransaction();
            if (jsonArrayCliente.size() > 0) {
                for (int i = 0; i < jsonArrayCliente.size(); i++) {
                    JsonObject jsonObjItem = jsonArrayCliente.get(i).getAsJsonObject();
                    statement.clearBindings();
                    statement.bindLong(1, jsonObjItem.get("CODCLI").getAsLong());
                    statement.bindString(2, jsonObjItem.get("RAZONSOCIAL").getAsString());
                    String stringToStore = Normalizer.normalize(jsonObjItem.get("RAZONSOCIAL").getAsString()
                                    .toLowerCase(Locale.getDefault()),
                            Normalizer.Form.NFC);
                    statement.bindString(3, stringToStore);
                    statement.bindString(4, jsonObjItem.get("TIPOPERSONA").getAsString());
                    statement.bindString(5, jsonObjItem.get("NROID").getAsString());
                    statement.bindString(6, jsonObjItem.get("DIRECCION").getAsString());
                    statement.execute();
                }
                db.getDb().setTransactionSuccessful();
                numInserted = jsonArrayCliente.size();
            } else {
                throw new Exception("No hay 'Clientes'.");
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
