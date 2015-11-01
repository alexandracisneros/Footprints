package com.neversoft.smartwaiter.model.business;

import android.content.Context;
import android.util.Log;

import com.neversoft.smartwaiter.database.SmartWaiterDB;

/**
 * Created by Usuario on 26/10/2015.
 */
public class SincroDAO {
    private Context mContext;

    public SincroDAO(Context context) {
        this.mContext = context;
    }

    //Cambiar a otra clase
    public void dropDataDownloaded() throws Exception {
        final SmartWaiterDB db = new SmartWaiterDB(SincroDAO.this.mContext);
        try {
            db.openWriteableDB();
            db.getDb().beginTransaction();
            db.deleteTable(SmartWaiterDB.Tables.ARTICULO);
            db.deleteTable(SmartWaiterDB.Tables.CLIENTE);
            db.deleteTable(SmartWaiterDB.Tables.CARTA);
            db.deleteTable(SmartWaiterDB.Tables.FAMILIA);
            db.deleteTable(SmartWaiterDB.Tables.MESA_PISO);
            db.deleteTable(SmartWaiterDB.Tables.PRIORIDAD);
            db.getDb().setTransactionSuccessful();
            Log.d(SmartWaiterDB.TAG, "dropDataDownloaded");

        } finally {
            db.getDb().endTransaction();
            db.closeDB();
        }

    }
}
