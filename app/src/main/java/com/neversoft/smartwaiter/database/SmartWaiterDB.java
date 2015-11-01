package com.neversoft.smartwaiter.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

/**
 * Created by Usuario on 02/09/2015.
 */
public class SmartWaiterDB {
    // database constants
    public static final String DB_NAME = "SmartWaiter.db";
    public static final int DB_VERSION = 1;
    public static final String TAG = "SmartWaiter";
    // database object and database helper object
    private SQLiteDatabase db;
    private DBHelper dbHelper;

    // constructor
    public SmartWaiterDB(Context context) {
        dbHelper = new DBHelper(context, DB_NAME, null, DB_VERSION);
    }

    public void openReadableDB() {
        db = dbHelper.getReadableDatabase();

    }

    public SQLiteDatabase getDb() {
        return db;
    }

    public void openWriteableDB() {
        db = dbHelper.getWritableDatabase();
    }

    public void closeDB() {
        if (db != null)
            db.close();
    }

    //CRUD Operations
//    public int batchInsert(List<SQLiteStatement> stmts) {
//        int numInserted = 0;
//        try {
//            this.openWriteableDB();
//            db.beginTransaction();
//            for (int i = 0; i < stmts.size(); i++) {
//                stmts.get(i).execute();
//            }
//            db.setTransactionSuccessful();
//            numInserted = stmts.size();
//
//        } finally {
//            db.endTransaction();
//        }
//        return numInserted;
//    }

    public SQLiteStatement compileStatement(String query) {
        return db.compileStatement(query);
    }

    public long insertOrThrow(String table, String nullColumnHack, ContentValues values) {
        return db.insertOrThrow(table, nullColumnHack, values);
    }

    public int delete(String table, String whereClause, String[] whereArgs) {
        return db.delete(table, whereClause, whereArgs);
    }

    /**
     * Execute query using the current internal state as {@code WHERE} clause.
     */
    public Cursor query(boolean distinct, String table, String[] columns,
                        String selection, String[] selectionArgs, String groupBy,
                        String having, String orderBy, String limit) {
        try {
            return db.query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
        } finally {

        }
    }

    public long count(String tableName, String where, String[] whereArgs) throws Exception {
        long nroCols = 0;

        nroCols = DatabaseUtils.queryNumEntries(db, tableName, where,
                whereArgs);
        //Aqui me quede 25/10/2015 -04:29pm
        return nroCols;
    }

    public void deleteTable(String tableName) throws Exception {
        String[] whereArgs = {tableName};
        db.delete(tableName, null, null);
        db.delete("sqlite_sequence", " name=?", whereArgs);
    }



    public interface Pedido {
        String ID = "_id";
        int ID_COL = 0;

        String FECHA = "fecha";
        int FECHA_COL = 1;

        String NRO_MESA = "nro_mesa";
        int NRO_MESA_COL = 2;

        String AMBIENTE = "ambiente";
        int AMBIENTE_COL = 3;

        String CODIGO_USUARIO = "codigo_usuario";
        int CODIGO_USUARIO_COL = 4;

        String CODIGO_CLIENTE = "codigo_cliente";
        int CODIGO_COL = 5;

        String TIPO_VENTA = "tipo_venta";
        int TIPO_VENTA_COL = 6;

        String TIPO_PAGO = "tipo_pago";
        int TIPO_PAGO_COL = 7;

        String MONEDA = "moneda";
        int MONEDA_COL = 8;

        String MONTO_TOTAL = "monto_total";
        int MONTO_TOTAL_COL = 9;

        String MONTO_RECIBIDO = "moneda_recibido";
        int MONTO_RECIBIDO_COL = 10;

        String ESTADO = "estado";
        int ESTADO_COL = 11;

        String CODIGO_CIA = "cod_cia";
        int CODIGO_CIA_COL = 12;

        String CONFIRMADO = "confirmado";
        int CONFIRMADO_COL = 13;

        String ENVIADO = "enviado";
        int ENVIADO_COL = 14;
    }

    public interface DetallePedido {
        String ID = "_id";
        int ID_COL = 0;

        String PEDIDO_ID = "pedido_id";
        int PEDIDO_ID_COL = 1;

        String COD_ART = "cod_articulo";
        int COD_ART_COL = 2;

        String UM = "um";
        int UM_COL = 3;

        String CANTIDAD = "cantidad";
        int CANTIDAD_COL = 4;

        String PRECIO = "precio";
        int PRECIO_COL = 5;

        String TIPO_ART = "tipo_articulo";
        int TIPO_ART_COL = 6;

        String COD_ART_PRINCIPAL = "cod_art_principal";
        int COD_ART_PRINCIPAL_COL = 7;

        String COMENTARIO = "comentario";
        int COMENTARIO_COL = 8;

        String ESTADO_ART = "estado_articulo";
        int ESTADO_ART_COL = 9;

        String DESC_ART = "desc_articulo";
        int DESC_ART_COL = 10;
    }

    public interface Familia {
        String ID = "_id";
        int ID_COL = 0;

        String CODIGO = "codigo";
        int CODIGO_COL = 1;

        String DESCRIPCION = "descripcion";
        int DESCRIPCION_COL = 2;

        String URL = "url";
        int URL_COL = 3;
    }

    public interface Prioridad {
        String ID = "_id";
        int ID_COL = 0;

        String CODIGO = "codigo";
        int CODIGO_COL = 1;

        String DESCRIPCION = "descripcion";
        int DESCRIPCION_COL = 2;
    }

    public interface Cliente {
        String ID = "_id";
        int ID_COL = 0;

        String RAZON_SOCIAL = "razon_social";
        int RAZON_SOCIAL_COL = 1;

        String RAZON_SOCIAL_NORM = "razon_social_norm";
        int RAZON_SOCIAL_NORM_COL = 2;

        String TIPO_PERSONA = "tipo_persona";
        int TIPO_PERSONA_COL = 3;

        String NRO_DOCUMENTO = "nro_documento";
        int NRO_DOCUMENTO_COL = 4;

        String DIRECCION = "direccion";
        int DIRECCION_COL = 5;
    }

    public interface MesaPiso {
        String ID = "_id";
        int ID_COL = 0;

        String NRO_PISO = "nro_piso";
        int NRO_PISO_COL = 1;

        String COD_AMBIENTE = "cod_ambiente";
        int COD_AMBIENTE_COL = 2;

        String DESC_AMBIENTE = "desc_ambiente";
        int DESC_AMBIENTE_COL = 3;

        String NRO_MESA = "nro_mesa";
        int NRO_MESA_COL = 4;

        String NRO_ASIENTOS = "nro_asientos";
        int NRO_ASIENTOS_COL = 5;

        String COD_ESTADO_MESA = "cod_estado_mesa";
        int COD_ESTADO_MESA_COL = 6;

        String DESC_ESTADO_MESA = "desc_estado_mesa";
        int DESC_ESTADO_MESA_COL = 7;

        String COD_RESERVA = "cod_reserva";
        int COD_RESERVA_COL = 8;
    }

    public interface Carta {
        String ID = "_id";
        int ID_COL = 0;

        String COD_FAMILIA = "cod_familia";
        int COD_FAMILIA_COL = 1;

        String COD_PRIORIDAD = "cod_prioridad";
        int COD_PRIORIDAD_COL = 2;

        String COD_ARTICULO = "cod_articulo";
        int COD_ARTICULO_COL = 3;

        String COD_ARTICULO_PRINC = "cod_articulo_princ";
        int COD_ARTICULO_PRINC_COL = 4;
    }

    public interface Articulo {
        String ID = "_id";
        int ID_COL = 0;

        String DESCRIPCION = "descripcion";
        int DESCRIPCION_COL = 1;

        String DESCRIPCION_NORM = "descripcion_norm";
        int DESCRIPCION_NORM_COL = 2;

        String UM = "um";
        int UM_COL = 3;

        String UM_DESC = "um_desc";
        int UM_DESC_COL = 4;

        String PRECIO = "um_precio";
        int PRECIO_COL = 5;

        String URL = "url";
        int URL_COL = 6;
    }

    public interface Tables {
        String PEDIDO = "pedido";
        String DETALLE_PEDIDO = "detalle_pedido";
        String FAMILIA = "familia";
        String PRIORIDAD = "prioridad";
        String CLIENTE = "cliente";
        String MESA_PISO = "mesa_piso";
        String CARTA = "carta";
        String ARTICULO = "articulo";

        String ARTICULOS_JOIN_CARTA = ARTICULO + " JOIN " + CARTA
                + " ON " + ARTICULO + "." + Articulo.ID + " = " + CARTA + "." + Carta.COD_ARTICULO;

    }

    private static class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE "
                            + Tables.PEDIDO + " ("
                            + Pedido.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                            + Pedido.FECHA + " TEXT NOT NULL,"
                            + Pedido.NRO_MESA + " INTEGER NOT NULL,"
                            + Pedido.AMBIENTE + " INTEGER NOT NULL,"
                            + Pedido.CODIGO_USUARIO + " TEXT NOT NULL,"
                            + Pedido.CODIGO_CLIENTE + " INTEGER,"
                            + Pedido.TIPO_VENTA + " TEXT,"
                            + Pedido.TIPO_PAGO + " TEXT,"
                            + Pedido.MONEDA + " TEXT,"
                            + Pedido.MONTO_TOTAL + " REAL NOT NULL,"
                            + Pedido.MONTO_RECIBIDO + " REAL,"
                            + Pedido.ESTADO + " TEXT NOT NULL,"
                            + Pedido.CODIGO_CIA + " TEXT NOT NULL,"
                            + Pedido.CONFIRMADO + " INTEGER DEFAULT 0,"
                            + Pedido.ENVIADO + " INTEGER DEFAULT 0"
                            + " )"
            );
            db.execSQL("CREATE TABLE "
                            + Tables.DETALLE_PEDIDO + " ("
                            + DetallePedido.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                            + DetallePedido.PEDIDO_ID + " INTEGER NOT NULL,"
                            + DetallePedido.COD_ART + " INTEGER NOT NULL,"
                            + DetallePedido.UM + " TEXT NOT NULL,"
                            + DetallePedido.CANTIDAD + " REAL NOT NULL,"
                            + DetallePedido.PRECIO + " REAL NOT NULL,"
                            + DetallePedido.TIPO_ART + " INTEGER NOT NULL,"
                            + DetallePedido.COD_ART_PRINCIPAL + " INTEGER,"
                            + DetallePedido.COMENTARIO + " TEXT,"
                            + DetallePedido.ESTADO_ART + " INTEGER NOT NULL,"
                            + DetallePedido.DESC_ART + " TEXT"
                            + " )"
            );
            db.execSQL("CREATE TABLE "
                            + Tables.FAMILIA + " ("
                            + Familia.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                            + Familia.CODIGO + " TEXT NOT NULL,"
                            + Familia.DESCRIPCION + " TEXT,"
                            + Familia.URL + " TEXT "
                            + " )"
            );
            db.execSQL("CREATE TABLE "
                            + Tables.PRIORIDAD + " ("
                            + Prioridad.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                            + Prioridad.CODIGO + " TEXT NOT NULL,"
                            + Prioridad.DESCRIPCION + " TEXT"
                            + " )"
            );
            db.execSQL("CREATE TABLE "
                            + Tables.CLIENTE + " ("
                            + Cliente.ID + " INTEGER PRIMARY KEY, "
                            + Cliente.RAZON_SOCIAL + " TEXT ,"
                            + Cliente.RAZON_SOCIAL_NORM + " TEXT,"
                            + Cliente.TIPO_PERSONA + " TEXT,"
                            + Cliente.NRO_DOCUMENTO + " TEXT,"
                            + Cliente.DIRECCION + " TEXT"
                            + " )"
            );
            db.execSQL("CREATE TABLE "
                            + Tables.MESA_PISO + " ("
                            + MesaPiso.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                            + MesaPiso.NRO_PISO + " INTEGER NOT NULL,"
                            + MesaPiso.COD_AMBIENTE + " INTEGER,"
                            + MesaPiso.DESC_AMBIENTE + " TEXT,"
                            + MesaPiso.NRO_MESA + " INTEGER,"
                            + MesaPiso.NRO_ASIENTOS + " INTEGER,"
                            + MesaPiso.COD_ESTADO_MESA + " TEXT,"
                            + MesaPiso.DESC_ESTADO_MESA + " TEXT,"
                            + MesaPiso.COD_RESERVA + " INTEGER"
                            + " )"
            );
            db.execSQL("CREATE TABLE "
                            + Tables.CARTA + " ("
                            + Carta.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                            + Carta.COD_FAMILIA + " TEXT NOT NULL,"
                            + Carta.COD_PRIORIDAD + " TEXT,"
                            + Carta.COD_ARTICULO + " INTEGER,"
                            + Carta.COD_ARTICULO_PRINC + " INTEGER"
                            + " )"
            );
            db.execSQL("CREATE TABLE "
                            + Tables.ARTICULO + " ("
                            + Articulo.ID + " INTEGER PRIMARY KEY , "
                            + Articulo.DESCRIPCION + " TEXT,"
                            + Articulo.DESCRIPCION_NORM + " TEXT,"
                            + Articulo.UM + " TEXT,"
                            + Articulo.UM_DESC + " TEXT,"
                            + Articulo.PRECIO + " REAL,"
                            + Articulo.URL + " TEXT"
                            + " )"
            );
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int i, int i1) {
            db.execSQL("DROP TABLE IF EXISTS " + Tables.PEDIDO);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.DETALLE_PEDIDO);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.FAMILIA);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.PRIORIDAD);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.CLIENTE);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.MESA_PISO);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.CARTA);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.ARTICULO);
        }
    }
}
