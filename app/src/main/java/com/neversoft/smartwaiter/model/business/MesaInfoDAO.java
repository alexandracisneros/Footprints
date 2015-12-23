package com.neversoft.smartwaiter.model.business;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.neversoft.smartwaiter.database.DBHelper;

/**
 * Created by Usuario on 22/12/2015.
 */
public class MesaInfoDAO {
    private Context mContext;

    public MesaInfoDAO(Context context) {
        mContext = context;
    }

    public int saveMesaInfoData(JsonArray jsonArrayMesaInfo) throws Exception {

        int numInserted = 0;
        String insertQuery = "INSERT INTO " + DBHelper.Tables.MESA_INFO + " ("
                + DBHelper.MesaInfo.COD_ESTADO + ","
                + DBHelper.MesaInfo.DESC_ESTADO + ","
                + DBHelper.MesaInfo.COD_COLOR + ","
                + DBHelper.MesaInfo.DESC_COLOR + ") VALUES (?,?,?,?)";
        DBHelper dbHelper;
        SQLiteDatabase db = null;
        try {
            dbHelper = DBHelper.getInstance(MesaInfoDAO.this.mContext);
            db = dbHelper.getWritableDatabase();
            SQLiteStatement statement = db.compileStatement(insertQuery);
            db.beginTransaction();
            if (jsonArrayMesaInfo.size() > 0) {
                for (int i = 0; i < jsonArrayMesaInfo.size(); i++) {
                    JsonObject jsonObjItem = jsonArrayMesaInfo.get(i).getAsJsonObject();
                    statement.clearBindings();
                    statement.bindString(1, jsonObjItem.get("CEMESA").getAsString());
                    statement.bindString(2, jsonObjItem.get("DEMESA").getAsString());
                    statement.bindString(3, jsonObjItem.get("CCOLOR").getAsString());
                    statement.bindString(4, jsonObjItem.get("DCOLOR").getAsString());
                    statement.execute();
                }
                db.setTransactionSuccessful();
                numInserted = jsonArrayMesaInfo.size();
            } else {
                throw new Exception("No hay 'MesaInfo'.");
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
}
