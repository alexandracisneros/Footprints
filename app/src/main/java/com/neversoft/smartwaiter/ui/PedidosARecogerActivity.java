package com.neversoft.smartwaiter.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.neversoft.smartwaiter.R;
import com.neversoft.smartwaiter.database.DBHelper;
import com.neversoft.smartwaiter.model.business.DetallePedidoDAO;
import com.neversoft.smartwaiter.model.business.PedidoDAO;
import com.neversoft.smartwaiter.model.entity.DetallePedidoEE;
import com.neversoft.smartwaiter.model.entity.PedidoEE;
import com.neversoft.smartwaiter.service.ConsultarPedidosRecogerService;
import com.neversoft.smartwaiter.service.NotificarPedidosRecogidosService;
import com.neversoft.smartwaiter.util.Funciones;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class PedidosARecogerActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener,
        AbsListView.MultiChoiceModeListener, NavigationView.OnNavigationItemSelectedListener {

    public static final String EXTRA_CANTIDAD_ACTUALIZAR = "cantidad_actualizar";
    public static final String EXTRA_SELECTED_ITEMS_ARRAY = "selected_items_array";
    public static final String EXTRA_ID_PEDIDO = "id_pedido";
    public static final String EXTRA_ID_PEDIDO_SERV = "id_pedido_servidor";
    public static final String EXTRA_TOTAL_ITEMS_RECOGER = "total_items_recoger";
    public static final String EXTRA_ID_PEDIDO_REFRESCAR = "id_pedido_refrescar";

    private NavigationView mNavigationView;
    private ListView mCabecerPedidoListView;
    private ListView mDetallePedidoListView;
    private ArrayList<DetallePedidoEE> mItems;
    private ActionMode mActionMode;
    private String mIdPedido;
    private String mIdPedidoServidor;
    private MaterialDialog mProgress;

    private BroadcastReceiver onEventConsultarPedidosARecoger = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            int cantidadActualizar = intent.getIntExtra(EXTRA_CANTIDAD_ACTUALIZAR, 0);

            new ConsultarPedidosDespachados().execute();
            new ConsultarItemsPedidoDespachado().execute("0");
            showProgressIndicator(false);


        }
    };
    private BroadcastReceiver onEventNotificarPedidosRecojidos = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String idPedidoRefrescar = intent.getStringExtra(EXTRA_ID_PEDIDO_REFRESCAR);
            //ANTES DE ESTO DEBERIA HABERSE UTILIZANDO UN LOADING QUE NO DEJE SELECCIONAR NADA MAS PARA PODER REFRESCAR LOS ITEMS PREVIAMENTE SELECCIONADOS
            if (idPedidoRefrescar == "0") {
                new ConsultarPedidosDespachados().execute();
            }
            new ConsultarItemsPedidoDespachado().execute(idPedidoRefrescar);
            showProgressIndicator(false);
            //TODO : Verificar si los AsyncTask "ConsultarPedidoDespachados" y "ConsultarItemsPedidoDespachado" no se pueden fucionar en uno solo
            //Para hacer lo anterior factible crea una inner class que tenga dos retornos,uno para cada AsyncTask,

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedidos_arecoger);
        overridePendingTransition(0, 0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // get reference to the ListView and set its listener
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.getMenu().getItem(SmartWaiter.OPCION_PEDIDOS_RECOGER).setChecked(true);

        // get reference header ListView
        mCabecerPedidoListView = (ListView) findViewById(R.id.cabeceraPedidoRecogerListView);
        mCabecerPedidoListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mCabecerPedidoListView.setOnItemClickListener(this);

        mDetallePedidoListView = (ListView) findViewById(R.id.detallePedidoRecogerListView);
        mDetallePedidoListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mDetallePedidoListView.setMultiChoiceModeListener(this);

        consultarPedidosARecoger();

    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ConsultarPedidosRecogerService.ACTION_CHECK_READY_ORDERS);
        LocalBroadcastManager.getInstance(this).registerReceiver(onEventConsultarPedidosARecoger, filter);

        IntentFilter filterNotificarRecojo = new IntentFilter(NotificarPedidosRecogidosService.ACTION_NOTIFICAR_RECOJO_PEDIDO);
        LocalBroadcastManager.getInstance(this).registerReceiver(onEventNotificarPedidosRecojidos, filterNotificarRecojo);

    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onEventConsultarPedidosARecoger);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onEventNotificarPedidosRecojidos);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pedidos_arecoger, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_actualizar) { //TODO Esta accion si es que se deja deberia ser para configurar el intervalo de notificacion entre alarma y alarma
            consultarPedidosARecoger();
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    private void consultarPedidosARecoger() {
        showProgressIndicator(true);
        Intent i = new Intent(this, ConsultarPedidosRecogerService.class);
        WakefulIntentService.sendWakefulWork(this, i);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        if (parent.getId() == R.id.cabeceraPedidoRecogerListView) {
            if (mActionMode != null) {
                mActionMode.finish();
            }
            TextView nroPedidoTextView = (TextView) v.findViewById(R.id.nroPedidoDescTextView);
            TextView nroPedServTextView = (TextView) v.findViewById(R.id.nroPedidoServTextView);
            mIdPedido = nroPedidoTextView.getText().toString();
            mIdPedidoServidor = nroPedServTextView.getText().toString();
            new ConsultarItemsPedidoDespachado().execute(mIdPedido);
        }
    }

    private void mostrarCabeceraPedido(List<PedidoEE> lista) {
        ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
        for (PedidoEE item : lista) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("pedidoNro", String.valueOf(item.getId()));
            map.put("pedidoPisoMesa", String.valueOf(item.getNroMesa()) + "-" + String.valueOf(item.getNroPiso())); //TODO: cambiar con lo que mande alex
            map.put("pedidoCantidad", item.getCantRecogida());//TODO: cambiar con lo que mande alex
            map.put("nroPedidoServ", String.valueOf(item.getNroPedidoServidor()));
            data.add(map);
        }

        //create the resouces, from, and to variables
        int resource = R.layout.pedido_cab_recoger_item;
        String[] from = {"pedidoNro", "pedidoPisoMesa", "pedidoCantidad", "nroPedidoServ"};
        int[] to = {R.id.nroPedidoDescTextView, R.id.mesaPisoPedidoTextView,
                R.id.itemsPedidoTextView, R.id.nroPedidoServTextView};

        //create and set the adapter
        SimpleAdapter adapter = new SimpleAdapter(this, data, resource, from, to);
        adapter.notifyDataSetChanged();

        mCabecerPedidoListView.setAdapter(adapter);
    }

    private void mostrarDetallePedido() {
        ArrayList<HashMap<String, String>> data =
                new ArrayList<HashMap<String, String>>();
        for (DetallePedidoEE item : mItems) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("item", String.valueOf(item.getItem()));
            map.put("articulo", item.getDescArticulo());
            map.put("cantidad", String.valueOf(item.getCantidad()));
            map.put("itemId", String.valueOf(item.getId()));
            data.add(map);
        }

        //create the resouces, from, and to variables
        int resource = R.layout.pedido_det_recoger_item;
        String[] from = {"item", "articulo", "cantidad", "itemId"};
        int[] to = {R.id.itemDetalleTextView, R.id.articuloDetalleTextView,
                R.id.cantidadDetalleTextView, R.id.itemIdDetalleTextView};

        //create and set the adapter
        SimpleAdapter adapter = new SimpleAdapter(this, data, resource, from, to);
        adapter.notifyDataSetChanged();

        mDetallePedidoListView.setAdapter(adapter);
    }

    //Action Mode -Start
    @Override
    public void onItemCheckedStateChanged(ActionMode actionMode, int position, long id, boolean checked) {
        int count = mDetallePedidoListView.getCheckedItemCount();
        actionMode.setTitle(String.format("%d Selected", count));
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        mActionMode = actionMode;
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.menu_pedidos_arecoger_contextual, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem item) {
        SparseBooleanArray items = mDetallePedidoListView.getCheckedItemPositions();

        switch (item.getItemId()) {
            case R.id.action_send_recogidos:
                confirmarRecojoItemsPedido(items, actionMode);
                return true;
        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {

    }

    //Action Mode -End
    private void confirmarRecojoItemsPedido(final SparseBooleanArray items, final ActionMode actionMode) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmación")
                .setMessage("¿Realmente desea confirmar el recojo de los items seleccionados?")
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();

                        enviarItemsPedidoRecogidos(items);
                        actionMode.finish();

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        actionMode.finish();

                    }
                }).show();
    }

    private void enviarItemsPedidoRecogidos(SparseBooleanArray items) {
        ArrayList<String> selectedItems = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            if (items.valueAt(i)) {
                int position = items.keyAt(i);
                DetallePedidoEE itemDetalle = mItems.get(position);
                selectedItems.add(String.valueOf(itemDetalle.getItem()));
            }
        }
        //
        Intent i = new Intent(PedidosARecogerActivity.this, NotificarPedidosRecogidosService.class);
        i.putStringArrayListExtra(EXTRA_SELECTED_ITEMS_ARRAY, selectedItems);
        i.putExtra(EXTRA_ID_PEDIDO, mIdPedido);
        i.putExtra(EXTRA_ID_PEDIDO_SERV, mIdPedidoServidor);
        i.putExtra(EXTRA_TOTAL_ITEMS_RECOGER, mItems.size());
        showProgressIndicator(true);
        startService(i);
    }

    private void showProgressIndicator(boolean showValue) {
        if (showValue) {
            mProgress = new MaterialDialog.Builder(PedidosARecogerActivity.this)
                    .content("Espere por favor...")
                    .cancelable(false)
                    .progress(true, 0)
                    .show();
        } else {
            if (mProgress != null) {
                mProgress.dismiss();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        if (menuItem.getOrder() != SmartWaiter.OPCION_PEDIDOS_RECOGER) {
            WeakReference<AppCompatActivity> weakActivity = new WeakReference<AppCompatActivity>(PedidosARecogerActivity.this);
            Funciones.selectMenuOption(weakActivity, menuItem.getOrder());
            return true;
        }
        return true;
    }

    private class ConsultarPedidosDespachados extends AsyncTask<Void, Void, Object> {

        @Override
        protected Object doInBackground(Void... voids) {
            Object requestObject;
            try {
                PedidoDAO pedidoDAO = new PedidoDAO(getApplicationContext());
                requestObject = pedidoDAO.getPedidosDespachados();
            } catch (Exception e) {
                requestObject = e;
            }
            return requestObject;
        }

        @Override
        protected void onPostExecute(Object result) {

            if (result instanceof List<?>) {
                List<PedidoEE> listaPedidos = (List<PedidoEE>) result;
                mostrarCabeceraPedido(listaPedidos);

            } else if (result instanceof Exception) {
                String response;
                response = ((Exception) result).getMessage();
                Log.d(DBHelper.TAG, "Se produjó la excepción: " + response);
                Toast.makeText(PedidosARecogerActivity.this, response, Toast.LENGTH_LONG).show();
            }
        }

    }

    private class ConsultarItemsPedidoDespachado extends AsyncTask<String, Void, Object> {
        @Override
        protected Object doInBackground(String... params) {
            Object requestObject;
            String idPedido = params[0];
            try {
                DetallePedidoDAO detallePedidoDAO = new DetallePedidoDAO(getApplicationContext());
                requestObject = detallePedidoDAO.getDetallePorEstado(idPedido, 2);
            } catch (Exception e) {
                requestObject = e;
            }
            return requestObject;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (result instanceof List<?>) {
                mItems = (ArrayList<DetallePedidoEE>) result;
                mostrarDetallePedido();
            } else if (result instanceof Exception) {
                String response;
                response = ((Exception) result).getMessage();
                Log.d(DBHelper.TAG, "Se produjó la excepción: " + response);
                Toast.makeText(PedidosARecogerActivity.this, response, Toast.LENGTH_LONG).show();
            }
        }
    }

}
