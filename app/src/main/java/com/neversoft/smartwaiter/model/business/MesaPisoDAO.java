package com.neversoft.smartwaiter.model.business;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.neversoft.smartwaiter.database.SmartWaiterDB;
import com.neversoft.smartwaiter.database.SmartWaiterDB.MesaPiso;
import com.neversoft.smartwaiter.database.SmartWaiterDB.Tables;
import com.neversoft.smartwaiter.model.entity.MesaPisoEE;
import com.neversoft.smartwaiter.model.entity.SpinnerEE;
import com.neversoft.smartwaiter.ui.MesaItemAdapter;
import com.neversoft.smartwaiter.ui.MesasActivity;

import java.lang.ref.WeakReference;

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
        SmartWaiterDB db = new SmartWaiterDB(this.mContext);
        String insertQuery = "INSERT INTO " + Tables.MESA_PISO + "( " +
                MesaPiso.NRO_PISO + "," + MesaPiso.COD_AMBIENTE + "," +
                MesaPiso.DESC_AMBIENTE + "," + MesaPiso.NRO_MESA + "," +
                MesaPiso.NRO_ASIENTOS + "," + MesaPiso.COD_ESTADO_MESA + "," +
                MesaPiso.DESC_ESTADO_MESA + "," + MesaPiso.COD_RESERVA + " ) " +
                "VALUES (?,?,?,?,?,?,?,?)";
        try {
            db.openWriteableDB();
            SQLiteStatement statement = db.compileStatement(insertQuery);
            db.getDb().beginTransaction();
            if (jsonArrayMesa.size() > 0) {
                for (int i = 0; i < jsonArrayMesa.size(); i++) {
                    JsonObject jsonObjItem = jsonArrayMesa.get(i).getAsJsonObject();
                    statement.clearBindings();
                    statement.bindLong(1, jsonObjItem.get("NROPISO").getAsInt());
                    statement.bindLong(2, jsonObjItem.get("CAMBIENTE").getAsInt());
                    statement.bindString(3, jsonObjItem.get("DAMBIENTE").getAsString());
                    statement.bindLong(4, jsonObjItem.get("NROMESA").getAsLong());
                    statement.bindLong(5, jsonObjItem.get("NROASIENTOS").getAsLong());
                    statement.bindString(6, jsonObjItem.get("CEMESA").getAsString());
                    statement.bindString(7, jsonObjItem.get("DEMESA").getAsString());
                    statement.bindLong(8, jsonObjItem.get("CODRESERVA").getAsLong());
                    statement.execute();
                }
                db.getDb().setTransactionSuccessful();
                numInserted = jsonArrayMesa.size();
            } else {
                throw new Exception("No hay 'Mesas'.");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            db.getDb().endTransaction();
            db.getDb().close();
        }
        return numInserted;

    }

    public void getPisosAsync(final WeakReference<Activity> mReference) {
        final Activity activity = mReference.get();
        final SmartWaiterDB db = new SmartWaiterDB(MesaPisoDAO.this.mContext);
        new AsyncTask<Void, Void, Cursor>() {

            @Override
            protected Cursor doInBackground(Void... params) {
                db.openReadableDB();
                Cursor cursor = db.query(true, Tables.MESA_PISO, PisosQuery.PROJECTION, null, null, null, null, null, null);
                return cursor;
            }

            @Override
            protected void onPostExecute(Cursor cursor) {
                while (cursor.moveToNext()) {
                    SpinnerEE item = new SpinnerEE();
                    item.setCodigo(cursor.getInt(PisosQuery.PISO_NRO_PISO));
                    item.setDescripcion("PISO " + cursor.getInt(PisosQuery.PISO_NRO_PISO));
                    ((MesasActivity) activity).getListaPisos().add(item);
                }
                cursor.close();
                ArrayAdapter adapter = new ArrayAdapter(mContext, android.R.layout.simple_spinner_item, ((MesasActivity) activity).getListaPisos());
                ((MesasActivity) activity).getPisosSpinner().setAdapter(adapter);

                AdapterView.OnItemSelectedListener pisosSelectedListener = new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> spinner, View container,
                                               int position, long id) {
                        int nroPiso = ((MesasActivity) activity).getListaPisos().get(position).getCodigo();
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
        final SmartWaiterDB db = new SmartWaiterDB(MesaPisoDAO.this.mContext);
        new AsyncTask<Void, Void, Cursor>() {

            @Override
            protected Cursor doInBackground(Void... params) {
                db.openReadableDB();
                Cursor cursor = db.query(true, Tables.MESA_PISO, AmbientesQuery.PROJECTION, MesaPiso.NRO_PISO + "= ? ", new String[]{String.valueOf(nroPiso)}, null, null, null, null);
                return cursor;
            }

            @Override
            protected void onPostExecute(Cursor cursor) {
                while (cursor.moveToNext()) {
                    SpinnerEE item = new SpinnerEE();
                    item.setCodigo(cursor.getInt(AmbientesQuery.AMBIENTE_COD_AMBIENTE));
                    item.setDescripcion(cursor.getString(AmbientesQuery.AMBIENTE_DESC_AMBIENTE));
                    ((MesasActivity) activity).getListaAmbientes().add(item);
                }
                cursor.close();
                ArrayAdapter adapter = new ArrayAdapter(mContext, android.R.layout.simple_spinner_item, ((MesasActivity) activity).getListaAmbientes());
                ((MesasActivity) activity).getAmbienteSpinner().setAdapter(adapter);

                AdapterView.OnItemSelectedListener ambientesSelectedListener = new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> spinner, View container,
                                               int position, long id) {
                        int codAmbiente = ((MesasActivity) activity).getListaAmbientes().get(position).getCodigo();

                        ((MesasActivity) activity).loadMesas(nroPiso, codAmbiente); // TODO: ACA ME QUEDE

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

    public void getMesasAsync(final WeakReference<Activity> mReference,final int nroPiso, final int codAmbiente) {
        final Activity activity = mReference.get();
        final SmartWaiterDB db = new SmartWaiterDB(MesaPisoDAO.this.mContext);
        new AsyncTask<Void, Void, Cursor>() {
            @Override
            protected Cursor doInBackground(Void... params) {
                db.openReadableDB();
                Cursor cursor = db.query(true, Tables.MESA_PISO,  MesasQuery.PROJECTION,  MesaPiso.NRO_PISO + "=? and " + MesaPiso.COD_AMBIENTE + "=? ", new String[]{String.valueOf(nroPiso), String.valueOf(codAmbiente)}, null, null, null, null);
                return cursor;
            }

            @Override
            protected void onPostExecute(Cursor cursor) {
                while (cursor.moveToNext()) {
                    MesaPisoEE item = new MesaPisoEE();
                    item.setId(cursor.getInt(MesasQuery.MESA_ID));
                    item.setNroPiso(cursor.getInt(MesasQuery.MESA_NRO_PISO));
                    item.setCodAmbiente(cursor.getInt(MesasQuery.MESA_COD_AMBIENTE));
                    item.setNroMesa(cursor.getInt(MesasQuery.MESA_NRO_MESA));
                    item.setNroAsientos(cursor.getInt(MesasQuery.MESA_NRO_ASIENTOS));
                    item.setCodEstado(cursor.getString(MesasQuery.MESA_COD_ESTADO));
                    item.setDescEstado(cursor.getString(MesasQuery.MESA_DESC_ESTADO));
                    item.setCodReserva(cursor.getInt(MesasQuery.MESA_COD_RESERVA));
                    ((MesasActivity) activity).getListaMesas().add(item);
                }
                cursor.close();
                ((MesasActivity) activity).getMesasGridView().setAdapter(new MesaItemAdapter(mContext, ((MesasActivity) activity).getListaMesas()));
                //((MesasActivity) activity).getMesasGridView().setOnItemClickListener(mContext);
//                ((MesasActivity) activity).getRecylerView().setAdapter(((MesasActivity) activity).getMesasAdapter());

            }
        }.execute();
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

    private interface MesasQuery {
        String[] PROJECTION = {
                MesaPiso.ID,
                MesaPiso.NRO_PISO,
                MesaPiso.COD_AMBIENTE,
                MesaPiso.NRO_MESA,
                MesaPiso.NRO_ASIENTOS,
                MesaPiso.COD_ESTADO_MESA,
                MesaPiso.DESC_ESTADO_MESA,
                MesaPiso.COD_RESERVA
        };
        int MESA_ID = 0;
        int MESA_NRO_PISO = 1;
        int MESA_COD_AMBIENTE = 2;
        int MESA_NRO_MESA = 3;
        int MESA_NRO_ASIENTOS = 4;
        int MESA_COD_ESTADO = 5;
        int MESA_DESC_ESTADO = 6;
        int MESA_COD_RESERVA = 7;
    }
}
