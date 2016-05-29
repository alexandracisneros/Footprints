package com.neversoft.smartwaiter.model.business;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.neversoft.smartwaiter.database.DBHelper;
import com.neversoft.smartwaiter.database.DBHelper.Tables;

/**
 * Created by Usuario on 26/10/2015.
 */
public class SincroDAO {
    private Context mContext;

    public SincroDAO(Context context) {
        this.mContext = context;
    }


    public void dropDataDownloaded() throws Exception {
        final DBHelper dbHelper = DBHelper.getInstance(SincroDAO.this.mContext);
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            dbHelper.deleteTable(Tables.FAMILIA, db);
            dbHelper.deleteTable(Tables.PRIORIDAD, db);
            dbHelper.deleteTable(Tables.CLIENTE, db);
            dbHelper.deleteTable(Tables.MESA_PISO, db);
            dbHelper.deleteTable(Tables.CARTA, db);
            dbHelper.deleteTable(Tables.ARTICULO, db);
            dbHelper.deleteTable(Tables.CONCEPTO, db);
            dbHelper.deleteTable(Tables.MESA_INFO, db);
            db.setTransactionSuccessful();
            Log.d(DBHelper.TAG, "dropDataDownloaded");

        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
    }

    public void dropDataUploaded() throws Exception {
        final DBHelper dbHelper = DBHelper.getInstance(SincroDAO.this.mContext);
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            dbHelper.deleteTable(Tables.DETALLE_PEDIDO, db);
            dbHelper.deleteTable(Tables.PEDIDO, db);
            db.setTransactionSuccessful();
            Log.d(DBHelper.TAG, "dropDataUploaded");

        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
    }
}
