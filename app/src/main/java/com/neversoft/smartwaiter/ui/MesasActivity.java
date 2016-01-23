package com.neversoft.smartwaiter.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.neversoft.smartwaiter.R;
import com.neversoft.smartwaiter.database.DBHelper;
import com.neversoft.smartwaiter.model.business.MesaPisoDAO;
import com.neversoft.smartwaiter.model.entity.MesaPisoEE;
import com.neversoft.smartwaiter.model.entity.SpinnerEE;
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
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(onEventRefrescarListadoMesas);
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
            Toast.makeText(
                    getApplicationContext(),
                    "Mesa Nro  #" + getListaMesas().get(position).getNroMesa(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, TomarPedidoActivity.class);
            startActivity(intent);
            //finish(); //TODO: Revisar si Tomar pedido deberia mostrar el menu lateral o no segun lineamientos de android
        } else if (parent.getId() == R.id.menu_listview) {
            if (position != SmartWaiter.OPCION_TOMAR_PEDIDO) {
                WeakReference<Activity> weakActivity = new WeakReference<Activity>(this);
                Funciones.selectMenuOption(weakActivity, position);
            }
        }
    }
}
