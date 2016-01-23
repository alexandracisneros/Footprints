package com.neversoft.smartwaiter.model.business;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.neversoft.smartwaiter.database.DBHelper;
import com.neversoft.smartwaiter.database.DBHelper.MesaInfo;
import com.neversoft.smartwaiter.database.DBHelper.MesaPiso;
import com.neversoft.smartwaiter.database.DBHelper.Reserva;
import com.neversoft.smartwaiter.database.DBHelper.Tables;
import com.neversoft.smartwaiter.model.entity.MesaPisoEE;
import com.neversoft.smartwaiter.model.entity.SpinnerEE;
import com.neversoft.smartwaiter.ui.MesaItemAdapter;
import com.neversoft.smartwaiter.ui.MesasActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

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
        String insertQuery = "INSERT INTO " + Tables.MESA_PISO + "( " +
                MesaPiso.ID + "," +
                MesaPiso.NRO_PISO + "," + MesaPiso.COD_AMBIENTE + "," +
                MesaPiso.DESC_AMBIENTE + "," + MesaPiso.NRO_MESA + "," +
                MesaPiso.NRO_ASIENTOS + "," + MesaPiso.COD_ESTADO_MESA + "," +
                MesaPiso.DESC_ESTADO_MESA + "," + MesaPiso.COD_RESERVA + "," +
                MesaPiso.ID_CLIE_RESERVA + " ) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?)";
        DBHelper dbHelper;
        SQLiteDatabase db = null;
        try {
            dbHelper = DBHelper.getInstance(MesaPisoDAO.this.mContext);
            db = dbHelper.getWritableDatabase();
            SQLiteStatement statement = db.compileStatement(insertQuery);
            db.beginTransaction();
            if (jsonArrayMesa.size() > 0) {
                for (int i = 0; i < jsonArrayMesa.size(); i++) {
                    JsonObject jsonObjItem = jsonArrayMesa.get(i).getAsJsonObject();
                    statement.clearBindings();
                    statement.bindLong(1, jsonObjItem.get("CODMESA").getAsInt());
                    statement.bindLong(2, jsonObjItem.get("NROPISO").getAsInt());
                    statement.bindLong(3, jsonObjItem.get("CAMBIENTE").getAsInt());
                    statement.bindString(4, jsonObjItem.get("DAMBIENTE").getAsString());
                    statement.bindLong(5, jsonObjItem.get("NROMESA").getAsLong());
                    statement.bindLong(6, jsonObjItem.get("NROASIENTOS").getAsLong());
                    statement.bindString(7, jsonObjItem.get("CEMESA").getAsString());
                    statement.bindString(8, jsonObjItem.get("DEMESA").getAsString());
                    statement.bindLong(9, jsonObjItem.get("CODRESERVA").getAsLong());
                    if (jsonObjItem.get("NROID").isJsonNull())
                        statement.bindNull(10);
                    else
                        statement.bindString(10, jsonObjItem.get("NROID").getAsString());
                    statement.execute();
                }
                db.setTransactionSuccessful();
                numInserted = jsonArrayMesa.size();
            } else {
                throw new Exception("No hay 'Mesas'.");
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

    public int updateEstadoMesa(JsonArray jsonArrayMesa) throws Exception {
        int numToUpdate = 0;
        int numAffectedRows;
        String updateQuery = "UPDATE " + Tables.MESA_PISO + " SET  " +
                MesaPiso.COD_ESTADO_MESA + "=? WHERE " + MesaPiso.ID + "=? ";
        DBHelper dbHelper;
        SQLiteDatabase db = null;
        try {
            dbHelper = DBHelper.getInstance(MesaPisoDAO.this.mContext);
            db = dbHelper.getWritableDatabase();
            SQLiteStatement statement = db.compileStatement(updateQuery);
            db.beginTransaction();
            
            for (int i = 0; i < jsonArrayMesa.size(); i++) {
                JsonObject jsonObjItem = jsonArrayMesa.get(i).getAsJsonObject();
                statement.clearBindings();
                statement.bindString(1, jsonObjItem.get("CEMESA").getAsString());
                statement.bindLong(2, jsonObjItem.get("CODMESA").getAsInt());
                numAffectedRows = statement.executeUpdateDelete();
                if (numAffectedRows > 0) {
                    numToUpdate++;
                }
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
        return numToUpdate;
    }

    public int updateEstadoMesaYReserva(int idMesa, int idReserva, String nuevoEstadoMesa, String nuevoEstadoReserva) throws Exception {
        DBHelper dbHelper;
        SQLiteDatabase db = null;
        int filas_mesa = 0;
        int filas_reserva = 0;
        try {
            dbHelper = DBHelper.getInstance(MesaPisoDAO.this.mContext);
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();

            //TODO: Solo si idRserva Mayor a Cero
            if (idReserva > 0) {
                ContentValues cvReserva = new ContentValues();
                cvReserva.put(Reserva.EST_MESA, nuevoEstadoMesa);
                cvReserva.put(Reserva.EST_RESERVA, nuevoEstadoReserva);
                String updateWhereReserva = Reserva.ID + " =? AND " + Reserva.COD_MESA + " =?";
                String[] updateWhereArgsReserva = new String[]{String.valueOf(idReserva), String.valueOf(idMesa)};

                filas_reserva = db.update(Tables.RESERVA, cvReserva, updateWhereReserva,
                        updateWhereArgsReserva);
            }
            if (idReserva > 0 && filas_reserva > 0) {
                ContentValues cvMesa = new ContentValues();
                cvMesa.put(MesaPiso.COD_ESTADO_MESA, nuevoEstadoMesa);
                String updateWhereMesa = MesaPiso.ID + " =? ";
                String[] updateWhereArgsMesa = new String[]{String.valueOf(idMesa)};

                filas_mesa = db.update(Tables.MESA_PISO, cvMesa, updateWhereMesa,
                        updateWhereArgsMesa);
                if (filas_mesa > 0) {
                    db.setTransactionSuccessful();
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
        return filas_mesa;
    }

    public void getPisosAsync(final WeakReference<Activity> mReference) {
        final Activity activity = mReference.get();
        final DBHelper dbHelper = DBHelper.getInstance(MesaPisoDAO.this.mContext);
        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        new AsyncTask<Void, Void, Cursor>() {

            @Override
            protected Cursor doInBackground(Void... params) {
                Cursor cursor = db.query(true, Tables.MESA_PISO, PisosQuery.PROJECTION, null, null, null, null, null, null);
                return cursor;
            }

            @Override
            protected void onPostExecute(Cursor cursor) {
                while (cursor.moveToNext()) {
                    SpinnerEE item = new SpinnerEE();
                    item.setCodigo(cursor.getString(PisosQuery.PISO_NRO_PISO));
                    item.setDescripcion("PISO " + cursor.getInt(PisosQuery.PISO_NRO_PISO));
                    ((MesasActivity) activity).getListaPisos().add(item);
                }
                cursor.close();
                db.close();  //ITV
                ArrayAdapter adapter = new ArrayAdapter(mContext, android.R.layout.simple_spinner_item, ((MesasActivity) activity).getListaPisos());
                ((MesasActivity) activity).getPisosSpinner().setAdapter(adapter);

                AdapterView.OnItemSelectedListener pisosSelectedListener = new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> spinner, View container,
                                               int position, long id) {
                        int nroPiso = Integer.parseInt(((MesasActivity) activity).getListaPisos().get(position).getCodigo());
                        //Toast.makeText(CombosActivity.this,"Piso: " + nroPiso,Toast.LENGTH_SHORT).show();
                        ((MesasActivity) activity).loadAmbienteSpinner(nroPiso);

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                        // TODO Auto-generated method stub
                    }
                };
                //http://stackoverflow.com/questions/2562248/how-to-keep-onitemselected-from-firing-off-on-a-newly-instantiated-spinner
                //mPisosSpinner.setSelection(0,false);
                // Setting ItemClick Handler for Spinner Widget
                ((MesasActivity) activity).getPisosSpinner().setOnItemSelectedListener(pisosSelectedListener);
            }
        }.execute();
    }

    public void getAmbientesAsync(final WeakReference<Activity> mReference, final int nroPiso) {
        final Activity activity = mReference.get();
        final DBHelper dbHelper = DBHelper.getInstance(MesaPisoDAO.this.mContext);
        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        new AsyncTask<Void, Void, Cursor>() {

            @Override
            protected Cursor doInBackground(Void... params) {
                Cursor cursor = db.query(true, Tables.MESA_PISO, AmbientesQuery.PROJECTION, MesaPiso.NRO_PISO + "= ? ", new String[]{String.valueOf(nroPiso)}, null, null, null, null);
                return cursor;
            }

            @Override
            protected void onPostExecute(Cursor cursor) {
                while (cursor.moveToNext()) {
                    SpinnerEE item = new SpinnerEE();
                    item.setCodigo(cursor.getString(AmbientesQuery.AMBIENTE_COD_AMBIENTE));
                    item.setDescripcion(cursor.getString(AmbientesQuery.AMBIENTE_DESC_AMBIENTE));
                    ((MesasActivity) activity).getListaAmbientes().add(item);
                }
                cursor.close();
                db.close();//ITV
                ArrayAdapter adapter = new ArrayAdapter(mContext, android.R.layout.simple_spinner_item, ((MesasActivity) activity).getListaAmbientes());
                ((MesasActivity) activity).getAmbienteSpinner().setAdapter(adapter);

                AdapterView.OnItemSelectedListener ambientesSelectedListener = new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> spinner, View container,
                                               int position, long id) {
                        int codAmbiente = Integer.parseInt(((MesasActivity) activity).getListaAmbientes().get(position).getCodigo());

                        ((MesasActivity) activity).startActualizarEstadoMesas();

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                        // TODO Auto-generated method stub
                    }
                };
                // Setting ItemClick Handler for Spinner Widget
                ((MesasActivity) activity).getAmbienteSpinner().setOnItemSelectedListener(ambientesSelectedListener);
            }
        }.execute();
    }

    public void getMesasAsync(final WeakReference<Activity> mReference, final int nroPiso, final int codAmbiente, final String clickableState) {
        final Activity activity = mReference.get();
        final DBHelper dbHelper = DBHelper.getInstance(MesaPisoDAO.this.mContext);
        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        new AsyncTask<Void, Void, Cursor>() {
            @Override
            protected Cursor doInBackground(Void... params) {
                Cursor cursor = db.query(true, Tables.MESAPISO_JOIN_MESAINFO, MesasInfoQuery.PROJECTION, MesaPiso.NRO_PISO + "=? AND " + MesaPiso.COD_AMBIENTE + "=? ",
                        new String[]{String.valueOf(nroPiso), String.valueOf(codAmbiente)}, null, null, null, null);
                return cursor;
            }

            @Override
            protected void onPostExecute(Cursor cursor) {
                while (cursor.moveToNext()) {
                    MesaPisoEE item = getMesasPisoFromCursor(cursor);
                    ((MesasActivity) activity).getListaMesas().add(item);
                }
                cursor.close();
                db.close();//ITV
                ((MesasActivity) activity).getMesasGridView().setAdapter(new MesaItemAdapter(mContext, ((MesasActivity) activity).getListaMesas(), clickableState));
                //((MesasActivity) activity).getMesasGridView().setOnItemClickListener(mContext);
//                ((MesasActivity) activity).getRecylerView().setAdapter(((MesasActivity) activity).getMesasAdapter());

            }
        }.execute();
    }

    public List<MesaPisoEE> getListaMesasReservadas(String idReserva, String idCliente) {
        List<MesaPisoEE> listaMesas = new ArrayList<>();
        DBHelper dbHelper;
        SQLiteDatabase db = null;
        try {
            dbHelper = DBHelper.getInstance(MesaPisoDAO.this.mContext);
            db = dbHelper.getReadableDatabase();
            idReserva = (idReserva == null ? Tables.RESERVA + "." + Reserva.ID : idReserva);
            idCliente = (idCliente == null ? Reserva.ID_CLIENTE : idCliente);
            String query = " SELECT " + TextUtils.join(",", MesasInfoReservaQuery.PROJECTION) +
                    " FROM " + Tables.RESERVA_JOIN_MESAPISO_JOIN_MESAINFO +
                    " WHERE " +
                    Tables.RESERVA + "." + Reserva.ID + "=" + idReserva + " AND " +
                    Reserva.ID_CLIENTE + "=" + idCliente + " AND " +
                    Reserva.EST_RESERVA + "='PDT'";

            Cursor cursor = db.rawQuery(query, null);
            while (cursor.moveToNext()) {
                MesaPisoEE item = getMesasPisoReservaFromCursor(cursor);
                listaMesas.add(item);
            }
            cursor.close();

        } finally {
            if (db != null) {
                db.close();
            }
        }
        return listaMesas;
    }

    private MesaPisoEE getMesasPisoFromCursor(Cursor cursor) {
        if (cursor == null || cursor.getCount() == 0) {
            return null;
        } else {
            try {
                MesaPisoEE mesaPisoEE = new MesaPisoEE();
                mesaPisoEE.setId(cursor.getInt(MesasInfoQuery.MESA_ID));
                mesaPisoEE.setNroPiso(cursor.getInt(MesasInfoQuery.MESA_NRO_PISO));
                mesaPisoEE.setCodAmbiente(cursor.getInt(MesasInfoQuery.MESA_COD_AMBIENTE));
                mesaPisoEE.setNroMesa(cursor.getInt(MesasInfoQuery.MESA_NRO_MESA));
                mesaPisoEE.setNroAsientos(cursor.getInt(MesasInfoQuery.MESA_NRO_ASIENTOS));
                mesaPisoEE.setCodEstado(cursor.getString(MesasInfoQuery.MESA_COD_ESTADO));
                mesaPisoEE.setDescEstado(cursor.getString(MesasInfoQuery.MESA_DESC_ESTADO));
                mesaPisoEE.setHTMLColor(cursor.getString(MesasInfoQuery.MESA_COD_COLOR));

                return mesaPisoEE;

            } catch (Exception e) {
                return null;
            }
        }
    }

    private MesaPisoEE getMesasPisoReservaFromCursor(Cursor cursor) {
        if (cursor == null || cursor.getCount() == 0) {
            return null;
        } else {
            try {
                MesaPisoEE mesaPisoEE = new MesaPisoEE();
                mesaPisoEE.setId(cursor.getInt(MesasInfoReservaQuery.MESA_ID));
                mesaPisoEE.setNroPiso(cursor.getInt(MesasInfoReservaQuery.MESA_NRO_PISO));
                mesaPisoEE.setCodAmbiente(cursor.getInt(MesasInfoReservaQuery.MESA_COD_AMBIENTE));
                mesaPisoEE.setNroMesa(cursor.getInt(MesasInfoReservaQuery.MESA_NRO_MESA));
                mesaPisoEE.setNroAsientos(cursor.getInt(MesasInfoReservaQuery.MESA_NRO_ASIENTOS));
                mesaPisoEE.setCodEstado(cursor.getString(MesasInfoReservaQuery.MESA_COD_ESTADO));
                mesaPisoEE.setDescEstado(cursor.getString(MesasInfoReservaQuery.MESA_DESC_ESTADO));
                mesaPisoEE.setHTMLColor(cursor.getString(MesasInfoReservaQuery.MESA_COD_COLOR));
                mesaPisoEE.setCodReserva(cursor.getInt(MesasInfoReservaQuery.RESERVA_COD_RESERVA));
                mesaPisoEE.setIDCliente(cursor.getString(MesasInfoReservaQuery.RESERVA_COD_CLIE));

                return mesaPisoEE;

            } catch (Exception e) {
                return null;
            }
        }
    }

    private interface PisosQuery {
        String[] PROJECTION = {
                MesaPiso.NRO_PISO
        };
        int PISO_NRO_PISO = 0;
    }

    private interface AmbientesQuery {
        String[] PROJECTION = {
                MesaPiso.COD_AMBIENTE,
                MesaPiso.DESC_AMBIENTE
        };

        int AMBIENTE_COD_AMBIENTE = 0;
        int AMBIENTE_DESC_AMBIENTE = 1;
    }

    private interface MesasInfoQuery {
        String[] PROJECTION = {
                Tables.MESA_PISO + "." + MesaPiso.ID,
                MesaPiso.NRO_PISO,
                MesaPiso.COD_AMBIENTE,
                MesaPiso.NRO_MESA,
                MesaPiso.NRO_ASIENTOS,
                MesaPiso.COD_ESTADO_MESA,
                MesaInfo.DESC_ESTADO,
                MesaInfo.COD_COLOR
        };
        int MESA_ID = 0;
        int MESA_NRO_PISO = 1;
        int MESA_COD_AMBIENTE = 2;
        int MESA_NRO_MESA = 3;
        int MESA_NRO_ASIENTOS = 4;
        int MESA_COD_ESTADO = 5;
        int MESA_DESC_ESTADO = 6;
        int MESA_COD_COLOR = 7;
    }

    private interface MesasInfoReservaQuery {
        String[] PROJECTION = {
                Tables.MESA_PISO + "." + MesaPiso.ID,
                MesaPiso.NRO_PISO,
                MesaPiso.COD_AMBIENTE,
                MesaPiso.NRO_MESA,
                MesaPiso.NRO_ASIENTOS,
                Reserva.EST_MESA,
                MesaInfo.DESC_ESTADO,
                MesaInfo.COD_COLOR,
                Tables.RESERVA + "." + Reserva.ID,
                Reserva.ID_CLIENTE
        };
        int MESA_ID = 0;
        int MESA_NRO_PISO = 1;
        int MESA_COD_AMBIENTE = 2;
        int MESA_NRO_MESA = 3;
        int MESA_NRO_ASIENTOS = 4;
        int MESA_COD_ESTADO = 5;
        int MESA_DESC_ESTADO = 6;
        int MESA_COD_COLOR = 7;
        int RESERVA_COD_RESERVA = 8;
        int RESERVA_COD_CLIE = 9;

    }
}
