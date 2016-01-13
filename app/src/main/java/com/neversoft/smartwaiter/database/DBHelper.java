package com.neversoft.smartwaiter.database;

import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Usuario on 02/09/2015.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String TAG = "SmartWaiter";
    // database constants
    private static final String DB_NAME = "SmartWaiter.db";
    private static final int DB_VERSION = 1;
    private static DBHelper mSingleton = null;

    private DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public synchronized static DBHelper getInstance(Context ctxt) {
        if (mSingleton == null) {
            mSingleton = new DBHelper(ctxt.getApplicationContext());
        }
        return mSingleton;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "
                        + Tables.PEDIDO + " ("
                        + Pedido.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + Pedido.FECHA + " TEXT NOT NULL,"
                        + Pedido.NRO_MESA + " INTEGER NOT NULL,"
                        + Pedido.NRO_PISO + " INTEGER NOT NULL,"
                        + Pedido.CANT_RECOGIDA + " TEXT NOT NULL,"
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
                        + Pedido.ENVIADO + " INTEGER DEFAULT 0,"
                        + Pedido.NRO_PED_SERVIDOR + " INTEGER DEFAULT 0"
                        + " )"
        );
        db.execSQL("CREATE TABLE "
                        + Tables.DETALLE_PEDIDO + " ("
                        + DetallePedido.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + DetallePedido.PEDIDO_ID + " INTEGER NOT NULL,"
                        + DetallePedido.ITEM + " INTEGER NOT NULL,"
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
                        + MesaPiso.ID + " INTEGER PRIMARY KEY, "
                        + MesaPiso.NRO_PISO + " INTEGER NOT NULL,"
                        + MesaPiso.COD_AMBIENTE + " INTEGER,"
                        + MesaPiso.DESC_AMBIENTE + " TEXT,"
                        + MesaPiso.NRO_MESA + " INTEGER,"
                        + MesaPiso.NRO_ASIENTOS + " INTEGER,"
                        + MesaPiso.COD_ESTADO_MESA + " TEXT,"
                        + MesaPiso.DESC_ESTADO_MESA + " TEXT,"
                        + MesaPiso.COD_RESERVA + " INTEGER,"
                        + MesaPiso.ID_CLIE_RESERVA + " TEXT"
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
        db.execSQL("CREATE TABLE "
                        + Tables.CONCEPTO + " ("
                        + Concepto.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + Concepto.COD_ITEM + " TEXT NOT NULL,"
                        + Concepto.DESC_ITEM + " TEXT NOT NULL,"
                        + Concepto.TIPO_ITEM + " INTEGER NOT NULL"
                        + " )"
        );
        db.execSQL("CREATE TABLE "
                        + Tables.MESA_INFO + " ("
                        + MesaInfo.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + MesaInfo.COD_ESTADO + " TEXT NOT NULL,"
                        + MesaInfo.DESC_ESTADO + " TEXT NOT NULL,"
                        + MesaInfo.COD_COLOR + " TEXT NOT NULL,"
                        + MesaInfo.DESC_COLOR + " TEXT NOT NULL"
                        + " )"
        );
        db.execSQL("CREATE TABLE "
                        + Tables.RESERVA + " ("
                        + Reserva.ID + " INTEGER PRIMARY KEY , "
                        + Reserva.ID_CLIENTE + " TEXT NOT NULL,"
                        + Reserva.COD_MESA + " INTEGER NOT NULL,"
                        + Reserva.EST_MESA + " TEXT NOT NULL,"
                        + Reserva.EST_RESERVA + " TEXT NOT NULL"
                        + " )"
        );

        /***************************  INDEXES  ***********************/
        db.execSQL("CREATE UNIQUE INDEX "
                        + MesaPiso.INDEX_PISO_AMB_MESA + " ON " + Tables.MESA_PISO + "("
                        + MesaPiso.NRO_PISO + ","
                        + MesaPiso.COD_AMBIENTE + ","
                        + MesaPiso.NRO_MESA + ")"
        );
        db.execSQL("CREATE INDEX "
                        + Concepto.INDEX_COD_TIPO + " ON " + Tables.CONCEPTO + "("
                        + Concepto.COD_ITEM + "," + Concepto.TIPO_ITEM + ")"
        );
        db.execSQL("CREATE INDEX "
                        + MesaInfo.INDEX_CODMESA + " ON " + Tables.MESA_INFO + "("
                        + MesaInfo.COD_ESTADO + ")"
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
        db.execSQL("DROP TABLE IF EXISTS " + Tables.CONCEPTO);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.MESA_INFO);
    }

    /*******************************************
     * Public Methods
     *******************************************/

    public long count(String tableName, String where, String[] whereArgs) throws Exception {
        long nroCols = 0;
        nroCols = DatabaseUtils.queryNumEntries(getReadableDatabase(), tableName, where,
                whereArgs);
        return nroCols;
    }

    public void deleteTable(String tableName, SQLiteDatabase db) throws Exception {
        String[] whereArgs = {tableName};
        db.delete(tableName, null, null);
        db.delete("sqlite_sequence", " name=?", whereArgs);
    }

    //TABLAS
    public interface Pedido {
        String ID = "_id";
        int ID_COL = 0;

        String FECHA = "fecha";
        int FECHA_COL = 1;

        String NRO_MESA = "nro_mesa";
        int NRO_MESA_COL = 2;

        String NRO_PISO = "nro_piso";
        int NRO_PISO_COL = 3;

        String CANT_RECOGIDA = "cant_recogida";
        int CANT_RECOGIDA_COL = 4;

        String AMBIENTE = "ambiente";
        int AMBIENTE_COL = 5;

        String CODIGO_USUARIO = "codigo_usuario";
        int CODIGO_USUARIO_COL = 6;

        String CODIGO_CLIENTE = "codigo_cliente";
        int CODIGO_COL = 7;

        String TIPO_VENTA = "tipo_venta";
        int TIPO_VENTA_COL = 8;

        String TIPO_PAGO = "tipo_pago";
        int TIPO_PAGO_COL = 9;

        String MONEDA = "moneda";
        int MONEDA_COL = 10;

        String MONTO_TOTAL = "monto_total";
        int MONTO_TOTAL_COL = 11;

        String MONTO_RECIBIDO = "moneda_recibido";
        int MONTO_RECIBIDO_COL = 12;

        String ESTADO = "estado";
        int ESTADO_COL = 13;

        String CODIGO_CIA = "cod_cia";
        int CODIGO_CIA_COL = 14;

        String CONFIRMADO = "confirmado";
        int CONFIRMADO_COL = 15;

        String ENVIADO = "enviado";
        int ENVIADO_COL = 16;

        String NRO_PED_SERVIDOR = "nro_pedido_servidor";
        int NRO_PED_SERVIDOR_COL = 17;
    }

    public interface DetallePedido {
        String ID = "_id";
        int ID_COL = 0;

        String PEDIDO_ID = "pedido_id";
        int PEDIDO_ID_COL = 1;

        String ITEM = "item";
        int ITEM_COL = 2;

        String COD_ART = "cod_articulo";
        int COD_ART_COL = 3;

        String UM = "um";
        int UM_COL = 4;

        String CANTIDAD = "cantidad";
        int CANTIDAD_COL = 5;

        String PRECIO = "precio";
        int PRECIO_COL = 6;

        String TIPO_ART = "tipo_articulo";
        int TIPO_ART_COL = 7;

        String COD_ART_PRINCIPAL = "cod_art_principal";
        int COD_ART_PRINCIPAL_COL = 8;

        String COMENTARIO = "comentario";
        int COMENTARIO_COL = 9;

        String ESTADO_ART = "estado_articulo";
        int ESTADO_ART_COL = 10;

        String DESC_ART = "desc_articulo";
        int DESC_ART_COL = 11;
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

        String ID_CLIE_RESERVA = "id_clie_reserva";
        int ID_CLIE_RESERVA_COL = 9;

        String INDEX_PISO_AMB_MESA = "i_mesapiso_pisoambmesa";
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

    public interface Concepto {
        String ID = "_id";
        int ID_COL = 0;

        String COD_ITEM = "cod";
        int COD_ITEM_COL = 1;

        String DESC_ITEM = "desc";
        int DESC_ITEM_COL = 2;

        String TIPO_ITEM = "tipo";
        int TIPO_ITEM_COL = 3;

        String INDEX_COD_TIPO = "i_concepto_codtipo";
    }

    public interface MesaInfo {
        String ID = "_id";
        int ID_COL = 0;

        String COD_ESTADO = "cod_estado";
        int COD_ESTADO_COL = 1;

        String DESC_ESTADO = "desc_estado";
        int DESC_ESTADO_COL = 2;

        String COD_COLOR = "cod_color";
        int COD_COLOR_COL = 3;

        String DESC_COLOR = "desc_color";
        int DESC_COLOR_COL = 4;

        String INDEX_CODMESA = "i_mesainfo_codmesa";
    }

    public interface Reserva {
        String ID = "_id";
        int ID_COL = 0;

        String ID_CLIENTE = "id_cliente";
        int ID_CLIENTE_COL = 1;

        String COD_MESA = "cod_mesa";
        int COD_MESA_COL = 2;

        String EST_MESA = "est_mesa";
        int EST_MESA_COL = 3;

        String EST_RESERVA = "est_reserva";
        int EST_RESERVA_COL = 4;
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
        String CONCEPTO = "concepto";
        String MESA_INFO = "mesa_info";
        String RESERVA = "reserva";

        String ARTICULOS_JOIN_CARTA = ARTICULO + " JOIN " + CARTA
                + " ON " + ARTICULO + "." + Articulo.ID + " = " + CARTA + "." + Carta.COD_ARTICULO;

        String MESAPISO_JOIN_MESAINFO = MESA_PISO + " JOIN " + MESA_INFO
                + " ON " + MESA_PISO + "." + MesaPiso.COD_ESTADO_MESA + " = " + MESA_INFO + "." + MesaInfo.COD_ESTADO;
        String RESERVA_JOIN_MESAPISO_JOIN_MESAINFO =
                RESERVA + " JOIN " + MESA_PISO + " ON " + Reserva.COD_MESA + "=" + MESA_PISO + "." + MesaPiso.ID +
                        " JOIN " + MESA_INFO + " ON " + MESA_PISO + "." + MesaPiso.COD_ESTADO_MESA + " = " + MESA_INFO + "." + MesaInfo.COD_ESTADO;
    }

}

