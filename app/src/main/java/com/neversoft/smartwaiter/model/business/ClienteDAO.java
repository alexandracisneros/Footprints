package com.neversoft.smartwaiter.model.business;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.neversoft.smartwaiter.database.DBHelper;
import com.neversoft.smartwaiter.database.DBHelper.Cliente;
import com.neversoft.smartwaiter.database.DBHelper.Tables;
import com.neversoft.smartwaiter.model.entity.ClienteEE;

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
        String insertQuery = "INSERT INTO " + Tables.CLIENTE + "( " +
                Cliente.ID + "," +
                Cliente.RAZON_SOCIAL + "," + Cliente.RAZON_SOCIAL_NORM + "," +
                Cliente.TIPO_PERSONA + "," + Cliente.NRO_DOCUMENTO + "," +
                Cliente.DIRECCION + ") " +
                "VALUES (?,?,?,?,?,?)";
        DBHelper dbHelper;
        SQLiteDatabase db = null;
        try {
            dbHelper = DBHelper.getInstance(ClienteDAO.this.mContext);
            db = dbHelper.getWritableDatabase();
            SQLiteStatement statement = db.compileStatement(insertQuery);
            db.beginTransaction();
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
                db.setTransactionSuccessful();
                numInserted = jsonArrayCliente.size();
            } else {
                throw new Exception("No hay 'Clientes'.");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
        return numInserted;

    }

    public ClienteEE getClienteByRuc(String ruc) throws Exception {
        ClienteEE clie = new ClienteEE();
        DBHelper dbHelper;
        SQLiteDatabase db = null;
        try {
            dbHelper = DBHelper.getInstance(ClienteDAO.this.mContext);
            db = dbHelper.getReadableDatabase();
            String where = Cliente.NRO_DOCUMENTO + " LIKE ? ";
            String[] whereArgs = {ruc};
            Cursor cursor = db.query(Tables.CLIENTE, null, where, whereArgs, null,
                    null, null);
            if (cursor.moveToFirst()) {
                clie.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.Cliente.ID)));
                clie.setRazonSocial(cursor.getString(cursor.getColumnIndex(Cliente.RAZON_SOCIAL_NORM)));
                clie.setTipoPersona(cursor.getString(cursor.getColumnIndex(Cliente.TIPO_PERSONA)));
                clie.setNroDocumento(cursor.getString(cursor.getColumnIndex(Cliente.NRO_DOCUMENTO)));
                clie.setDireccion(cursor.getString(cursor.getColumnIndex(Cliente.DIRECCION)));

            }
            cursor.close();

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return clie;
    }
}
