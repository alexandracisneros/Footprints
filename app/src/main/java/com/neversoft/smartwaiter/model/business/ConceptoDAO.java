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
public class ConceptoDAO {
    private Context mContext;

    public ConceptoDAO(Context context) {
        mContext = context;
    }

    public int saveConceptoData(JsonArray jsonArrayConcepto, int tipo) throws Exception {

        int numInserted = 0;
        String insertQuery = "INSERT INTO " + DBHelper.Tables.CONCEPTO + " ("
                + DBHelper.Concepto.COD_ITEM + ","
                + DBHelper.Concepto.DESC_ITEM + ","
                + DBHelper.Concepto.TIPO_ITEM + ") VALUES (?,?,?)";
        DBHelper dbHelper;
        SQLiteDatabase db = null;
        try {
            dbHelper = DBHelper.getInstance(ConceptoDAO.this.mContext);
            db = dbHelper.getWritableDatabase();
            SQLiteStatement statement = db.compileStatement(insertQuery);
            db.beginTransaction();
            if (jsonArrayConcepto.size() > 0) {
                for (int i = 0; i < jsonArrayConcepto.size(); i++) {
                    JsonObject jsonObjItem = jsonArrayConcepto.get(i).getAsJsonObject();
                    statement.clearBindings();
                    statement.bindString(1, jsonObjItem.get("codelemento").getAsString());
                    statement.bindString(2, jsonObjItem.get("descripcion").getAsString());
                    statement.bindLong(3, tipo);
                    statement.execute();
                }
                db.setTransactionSuccessful();
                numInserted = jsonArrayConcepto.size();
            } else {
                throw new Exception("No hay 'Conceptos' del tipo : " + tipo + ".");
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
