package com.neversoft.smartwaiter.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
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
    private FrameLayout mIndicatorFrameLayout;
    private LinearLayout mMainLinearLayout;
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
            //TODO: If it takes too long you could put a delay here to play for time for the tables to be loaded
            showProgressIndicator(false);
        }
    };
    private BroadcastReceiver onEventActualizarEstadoMesa = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int resultadoOperacion = intent.getIntExtra(ActualizarEstadoMesaService.EXTRA_RESULTADO_ACTUALIZACION, 0);
            String mensajeOperacion = intent.getStringExtra(ActualizarEstadoMesaService.EXTRA_MENSAJE_ACTUALIZACION);
            if (mensajeOperacion.equals("")) {
                Intent toIntent = new Intent(MesasActivity.this, TomarPedidoActivity.class);
                toIntent.putExtra(TomarPedidoActivity.EXTRA_PREVIOUS_ACTIVITY_CLASS, MesasActivity.this.getClass().getName());
                startActivity(toIntent);
                finish(); // finaliza actividad para que al volver necesariamente se tenga que volver a cargar la actividad
            } else {
                Log.d(DBHelper.TAG, "Se produjó la excepción: " + mensajeOperacion);
                Toast.makeText(MesasActivity.this, resultadoOperacion, Toast.LENGTH_LONG)
                        .show();
                showProgressIndicator(false);
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

        mIndicatorFrameLayout = (FrameLayout) findViewById(R.id.loadingIndicatorLayout);
        mMainLinearLayout = (LinearLayout) findViewById(R.id.mainLinearLayout);
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
        showProgressIndicator(true);
        startService(serviceIntent);
    }

    private void showProgressIndicator(boolean showValue) {
        if (showValue) {
            mMainLinearLayout.setVisibility(View.GONE);
            mIndicatorFrameLayout.setVisibility(View.VISIBLE);
        } else {
            mMainLinearLayout.setVisibility(View.VISIBLE);
            mIndicatorFrameLayout.setVisibility(View.GONE);
        }
    }

    private void confirmarActualizarEstadoMesa() {

        new AlertDialog.Builder(this)
                .setTitle("Confirmación")
                .setMessage("¿Realmente desea proceder a efectuar un pedido sobre la mesa :" + mMesaPisoSeleccionado.getNroMesa() + " ?")
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();
                        Gson gson = new Gson();
                        String mesaString = gson.toJson(mMesaPisoSeleccionado);
                        Intent serviceIntent = new Intent(MesasActivity.this, ActualizarEstadoMesaService.class);
                        //Extras
                        serviceIntent.putExtra(ActualizarEstadoMesaService.EXTRA_TABLE, mesaString);
                        serviceIntent.putExtra(ActualizarEstadoMesaService.EXTRA_CLASS_NAME, MesasActivity.this.getClass().getName());
                        Log.d(DBHelper.TAG, "Antes de startService ActualizarEstadoMesaService");
                        showProgressIndicator(true);
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
            confirmarActualizarEstadoMesa();
        } else if (parent.getId() == R.id.menu_listview) {
            if (position != SmartWaiter.OPCION_TOMAR_PEDIDO) {
                WeakReference<Activity> weakActivity = new WeakReference<Activity>(this);
                Funciones.selectMenuOption(weakActivity, position);
            }
        }
    }
}
