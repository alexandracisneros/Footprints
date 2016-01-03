package com.neversoft.smartwaiter.model.business;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.neversoft.smartwaiter.database.DBHelper;
import com.neversoft.smartwaiter.database.DBHelper.Reserva;
import com.neversoft.smartwaiter.database.DBHelper.Tables;

/**
 * Created by Usuario on 02/01/2016.
 */
public class ReservaDAO {
    private Context mContext;

    public ReservaDAO(Context context) {
        mContext = context;
    }

    public int saveReservaData(JsonArray jsonArrayReserva) throws Exception {

        int numInserted = 0;
        String insertQuery = "INSERT INTO " + Tables.RESERVA + " ("
                + Reserva.ID + ","
                + Reserva.ID_CLIENTE + ","
                + Reserva.COD_MESA + ","
                + Reserva.EST_MESA + ","
                + Reserva.EST_RESERVA
                + ") VALUES (?,?,?,?,?)";
        DBHelper dbHelper;
        SQLiteDatabase db = null;
        try {
            dbHelper = DBHelper.getInstance(ReservaDAO.this.mContext);
            db = dbHelper.getWritableDatabase();
            SQLiteStatement statement = db.compileStatement(insertQuery);
            db.beginTransaction();
            if (jsonArrayReserva.size() > 0) {
                for (int i = 0; i < jsonArrayReserva.size(); i++) {
                    JsonObject jsonObjItem = jsonArrayReserva.get(i).getAsJsonObject();
                    statement.clearBindings();
                    statement.bindLong(1, jsonObjItem.get("CODRESERVA").getAsLong());
                    statement.bindString(2, jsonObjItem.get("NROID").getAsString());
                    statement.bindLong(3, jsonObjItem.get("CODMESA").getAsLong());
                    statement.bindString(4, jsonObjItem.get("CEMESA").getAsString());
                    statement.bindString(5, jsonObjItem.get("CERESERVA").getAsString());
                    statement.execute();
                }
                db.setTransactionSuccessful();
                numInserted = jsonArrayReserva.size();
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

    public void insertOrUpdateReservadas(JsonArray jsonArrayReserva, String id_cliente) throws Exception {
        DBHelper dbHelper;
        SQLiteDatabase db = null;
        try {
            dbHelper = DBHelper.getInstance(ReservaDAO.this.mContext);
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();

            for (JsonElement element : jsonArrayReserva) {
                JsonObject reserva = element.getAsJsonObject();
                ContentValues initialValues = new ContentValues();
                initialValues.put(Reserva.ID, reserva.get("codReserva").getAsLong());
                initialValues.put(Reserva.ID_CLIENTE, id_cliente);
                initialValues.put(Reserva.COD_MESA, reserva.get("codMesa").getAsLong());
                initialValues.put(Reserva.EST_MESA, reserva.get("ceMesa").getAsString());
                initialValues.put(Reserva.EST_RESERVA, reserva.get("ceReserva").getAsString());
                db.insertWithOnConflict(Tables.RESERVA, null, initialValues, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();

        } catch (Exception e) {
            throw e;
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
    }
}
