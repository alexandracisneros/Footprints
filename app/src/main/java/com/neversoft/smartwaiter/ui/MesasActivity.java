package com.neversoft.smartwaiter.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.neversoft.smartwaiter.R;
import com.neversoft.smartwaiter.database.DBHelper;
import com.neversoft.smartwaiter.model.business.MesaPisoDAO;
import com.neversoft.smartwaiter.model.entity.MesaPisoEE;
import com.neversoft.smartwaiter.model.entity.SpinnerEE;
import com.neversoft.smartwaiter.service.ActualizarEstadoMesaService;
import com.neversoft.smartwaiter.service.ObtenerListaMesasService;
import com.neversoft.smartwaiter.util.Funciones;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class MesasActivity extends Activity
        implements AdapterView.OnItemClickListener {
    public static final String EXTRA_CANTIDAD_MESAS_ACTUALIZADOS = "cantidad_mesas_actualizadas";
    private Spinner mPisosSpinner;
    private Spinner mAmbienteSpinner;
    private GridView mMesasGridView;
    private ListView mMenuListView;
    private MesaPisoEE mMesaPisoSeleccionado;

    private ArrayList<SpinnerEE> mListaAmbientes;
    private ArrayList<SpinnerEE> mListaPisos;
    private ArrayList<MesaPisoEE> mListaMesas;
    // The ScheduleHelper is responsible for feeding data in a format suitable to the Adapter.
    private MesaPisoDAO mDataHelper;

    private BroadcastReceiver onEventRefrescarListadoMesas = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int cantMesasActualizadas = intent.getIntExtra(EXTRA_CANTIDAD_MESAS_ACTUALIZADOS, 0);
            Toast.makeText(MesasActivity.this, "Nro de mesas actualizadas : " + cantMesasActualizadas, Toast.LENGTH_SHORT).show();
            int nroPiso = Integer.parseInt((mListaPisos.get(mPisosSpinner.getSelectedItemPosition()).getCodigo()));
            int codAmbiente = Integer.parseInt((mListaAmbientes.get(mAmbienteSpinner.getSelectedItemPosition()).getCodigo()));
            loadMesas(nroPiso, codAmbiente);
        }
    };
    private BroadcastReceiver onEventActualizarEstadoMesa = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean resultadoOperacion = intent.getBooleanExtra(ActualizarEstadoMesaService.EXTRA_RESULTADO_ACTUALIZACION, false);
            if (resultadoOperacion) {
                Toast.makeText(MesasActivity.this, "Resultado de Actualizar Estado: " + resultadoOperacion, Toast.LENGTH_SHORT).show();
                String[] params = {String.valueOf(mMesaPisoSeleccionado.getId()),
                        String.valueOf(mMesaPisoSeleccionado.getCodReserva())};
                new ActualizarMesa_Reserva().execute(params);

            } else {
                String response = "Error";
                //response = ((Exception) result).getMessage();
                Log.d(DBHelper.TAG, "Se produjó la excepción: " + response);
                Toast.makeText(MesasActivity.this, response, Toast.LENGTH_LONG)
                        .show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mesas);
        overridePendingTransition(0, 0);
        mDataHelper = new MesaPisoDAO(this);
        // get reference to the ListView and set its listener
        mMenuListView = (ListView) findViewById(R.id.menu_listview);
        mMenuListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mMenuListView.setOnItemClickListener(this);


        Resources res = getResources();
        String[] options = res.getStringArray(R.array.menu_items_array);
        ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, options);
        mMenuListView.setAdapter(itemsAdapter);
        mMenuListView.setItemChecked(SmartWaiter.OPCION_TOMAR_PEDIDO, true);


        mPisosSpinner = (Spinner) findViewById(R.id.pisos_spinner);
        mAmbienteSpinner = (Spinner) findViewById(R.id.ambientes_spinner);

        mMesasGridView = (GridView) findViewById(R.id.mesasGridView);
        mMesasGridView.setOnItemClickListener(this);
        loadPisosSpinner();


    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filterNotificarRefrescarListado = new IntentFilter(ObtenerListaMesasService.ACTION_GET_TABLES_STATUS);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(onEventRefrescarListadoMesas, filterNotificarRefrescarListado);
        IntentFilter filterNotificarActualizarEstadoMesa = new IntentFilter(ActualizarEstadoMesaService.ACTION_UPDATE_TABLE_STATUS);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(onEventActualizarEstadoMesa, filterNotificarActualizarEstadoMesa);
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(onEventRefrescarListadoMesas);
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(onEventActualizarEstadoMesa);
        super.onPause();
    }

    public Spinner getPisosSpinner() {
        return mPisosSpinner;
    }

    public Spinner getAmbienteSpinner() {
        return mAmbienteSpinner;
    }

    public ArrayList<SpinnerEE> getListaAmbientes() {
        return mListaAmbientes;
    }

    public ArrayList<SpinnerEE> getListaPisos() {
        return mListaPisos;
    }

    public GridView getMesasGridView() {
        return mMesasGridView;
    }

    public ArrayList<MesaPisoEE> getListaMesas() {
        return mListaMesas;
    }

    private void loadPisosSpinner() {
        mListaPisos = new ArrayList<SpinnerEE>();
        WeakReference<Activity> weakActivity = new WeakReference<Activity>(this);
        mDataHelper.getPisosAsync(weakActivity);
    }

    public void loadAmbienteSpinner(final int nroPiso) {
        mListaAmbientes = new ArrayList<SpinnerEE>();
        WeakReference<Activity> weakActivity = new WeakReference<Activity>(this);
        //TODO: Se debera tener en cuenta lo siguiente:
        /*
            1. Cada vez que se ingrese a esta pagina se debera consultar el webservice
            2. Con la data recuperada actualizar los estados de las mesas
            3. Refrescar la patalla con la data actual (revisa si se puede actualizar directamente con los retornado sin ncesidad de leer bd
        * */
        mDataHelper.getAmbientesAsync(weakActivity, nroPiso);
    }

    public void loadMesas(int nroPiso, int codAmbiente) {
        mListaMesas = new ArrayList<MesaPisoEE>();
        WeakReference<Activity> weakActivity = new WeakReference<Activity>(this);
        mDataHelper.getMesasAsync(weakActivity, nroPiso, codAmbiente, "LIB");
    }

    public void startActualizarEstadoMesas() {
        //Desde aca mostrar la pantalla de Loading y terminarla cuando regrese el servicio
        Intent serviceIntent = new Intent(MesasActivity.this,
                ObtenerListaMesasService.class);
        Log.d(DBHelper.TAG, "Antes de startService ObtenerListaMesasServiceService");
        startService(serviceIntent);
    }

    private void confirmarActualizarEstadoMesa(final MesaPisoEE mesaPisoEE) {

        new AlertDialog.Builder(this)
                .setTitle("Confirmación")
                .setMessage("¿Realmente desea proceder a efectuar un pedido sobre la mesa :" + mesaPisoEE.getNroMesa() + " ?")
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Start daily operations
                        dialog.cancel();
                        Gson gson = new Gson();
                        String mesaString = gson.toJson(mesaPisoEE);
                        Intent serviceIntent = new Intent(MesasActivity.this,
                                ActualizarEstadoMesaService.class);
                        serviceIntent.putExtra(ActualizarEstadoMesaService.EXTRA_TABLE, mesaString);
                        serviceIntent.putExtra(ActualizarEstadoMesaService.EXTRA_CLASS_NAME, this.getClass().getName());
                        Log.d(DBHelper.TAG, "Antes de startService ActualizarEstadoMesaService");
                        startService(serviceIntent);

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();

                    }
                }).setIcon(android.R.drawable.ic_dialog_alert).show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mesas, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v,
                            int position, long id) {
        if (parent.getId() == R.id.mesasGridView) {
            mMesaPisoSeleccionado = getListaMesas().get(position);
            confirmarActualizarEstadoMesa(mMesaPisoSeleccionado);
        } else if (parent.getId() == R.id.menu_listview) {
            if (position != SmartWaiter.OPCION_TOMAR_PEDIDO) {
                WeakReference<Activity> weakActivity = new WeakReference<Activity>(this);
                Funciones.selectMenuOption(weakActivity, position);
            }
        }
    }

    private class ActualizarMesa_Reserva extends AsyncTask<String, Void, Object> {
        @Override
        protected Object doInBackground(String... params) {


            Object requestObject;
            String idMesa = params[0];
            String idReservaLocal = params[1];
            try {

                MesaPisoDAO mesaPisoDAO = new MesaPisoDAO(getApplicationContext());
                requestObject = mesaPisoDAO.updateEstadoMesaYReserva(Integer.parseInt(idMesa), Integer.parseInt(idReservaLocal),
                        "OCU", "EFE");
            } catch (Exception e) {
                requestObject = e;
            }
            return requestObject;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (result instanceof Integer) {
                Intent intent = new Intent(MesasActivity.this, TomarPedidoActivity.class);
                startActivity(intent);
                //finish(); //TODO: Revisar si Tomar pedido deberia mostrar el menu lateral o no segun lineamientos de android
            } else if (result instanceof Exception) {
                String response;
                response = ((Exception) result).getMessage();
                Log.d(DBHelper.TAG, "Se produjó la excepción: " + response);
                Toast.makeText(MesasActivity.this, response, Toast.LENGTH_LONG)
                        .show();
            }
        }

    }
}
