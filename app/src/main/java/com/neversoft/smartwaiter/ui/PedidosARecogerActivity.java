package com.neversoft.smartwaiter.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.neversoft.smartwaiter.R;
import com.neversoft.smartwaiter.database.SmartWaiterDB;
import com.neversoft.smartwaiter.model.business.PedidoDAO;
import com.neversoft.smartwaiter.model.entity.PedidoEE;
import com.neversoft.smartwaiter.service.ConsultarPedidosRecogerReceiver;
import com.neversoft.smartwaiter.service.ConsultarPedidosRecogerService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PedidosARecogerActivity extends Activity implements AdapterView.OnItemClickListener {
    //    public static final String EXTRA_RANDOM="r";
    public static final String EXTRA_CANTIDAD_ACTUALIZAR = "cantidad_actualizar";
    //    public static final String ACTION_EVENT="e";
    private ListView mMenuListView;
    private ListView mCabecerPedidoListView;
    private BroadcastReceiver onEvent = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //http://belencruz.com/2015/04/refresh-data-in-a-custom-android-adapter/
            int cantidadActualizar = intent.getIntExtra(EXTRA_CANTIDAD_ACTUALIZAR, 0);
            if (cantidadActualizar > 0) {
                new ConsultarPedidosDespachados().execute();
            }
//            Date date=new Date(intent.getLongExtra(EXTRA_TIME, 0));
//
//            mDateTextView.setText(String.format("%s = %x", fmt.format(date),
//                    intent.getIntExtra(EXTRA_RANDOM, -1)));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedidos_arecoger);
        overridePendingTransition(0, 0);

        // get reference to the ListView and set its listener
        mMenuListView = (ListView) findViewById(R.id.menu_listview);
        mMenuListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mMenuListView.setOnItemClickListener(this);

        // get reference header ListView
        mCabecerPedidoListView = (ListView) findViewById(R.id.detalleCabPedidoRecogerListView);
        mCabecerPedidoListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mCabecerPedidoListView.setOnItemClickListener(this);


        Resources res = getResources();
        String[] options = res.getStringArray(R.array.menu_items_array);
        ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, options);
        mMenuListView.setAdapter(itemsAdapter);
        mMenuListView.setItemChecked(3, true);  //TODO: Put this in some sort of Constant

        ConsultarPedidosRecogerReceiver.scheduleAlarms(this);

        Toast.makeText(this, R.string.alarms_scheduled, Toast.LENGTH_LONG)
                .show();
        ConsultarPedidosRecogerReceiver.scheduleAlarms(this);
        new ConsultarPedidosDespachados().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ConsultarPedidosRecogerService.ACTION_CHECK_READY_ORDERS);

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(onEvent, filter);
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(onEvent);
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        if (parent.getId() == R.id.menu_listview) {
            opcionesMenu(position);
        }
    }

    private void opcionesMenu(int position) {
        Intent intent;
        switch (position) {
            case 0:
                intent = new Intent(this, IniciarDiaActivity.class);
                startActivity(intent);
                finish();
                break;
            case 1:
                intent = new Intent(this, SincronizarActivity.class);
                startActivity(intent);
                finish();
                break;
            case 2:
                intent = new Intent(this, MesasActivity.class);
                startActivity(intent);
                finish();
                break;
            case 3:
                break;
            case 4:
                break;
            case 5:
                intent = new Intent(this, CerrarDiaActivity.class);
                startActivity(intent);
                finish();
                break;

        }
    }

    private void mostrarCabeceraPedido(List<PedidoEE> lista) {
        ArrayList<HashMap<String, String>> data =
                new ArrayList<HashMap<String, String>>();
        for (PedidoEE item : lista) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("pedidoNro", String.valueOf(item.getId()));
            map.put("pedidoPisoMesa", String.valueOf(item.getNroMesa()) + "-" + String.valueOf(item.getNroPiso())); //TODO: cambiar con lo que mande alex
            map.put("pedidoCantidad", item.getCantRecogida());//TODO: cambiar con lo que mande alex
            data.add(map);
        }

        //create the resouces, from, and to variables
        int resource = R.layout.pedido_cab_recoger_item;
        String[] from = {"pedidoNro", "pedidoPisoMesa", "pedidoCantidad"};
        int[] to = {R.id.nroPedidoDescTextView, R.id.mesaPisoPedidoTextView,
                R.id.itemsPedidoTextView};

        //create and set the adapter
        SimpleAdapter adapter = new SimpleAdapter(this, data, resource, from, to);
        adapter.notifyDataSetChanged();

        mCabecerPedidoListView.setAdapter(adapter);
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
            // Clear progress indicator

            if (result instanceof List<?>) {
                List<PedidoEE> listaPedidos = (List<PedidoEE>) result;
                mostrarCabeceraPedido(listaPedidos);

            } else if (result instanceof Exception) {
                String response;
                response = ((Exception) result).getMessage();
                Log.d(SmartWaiterDB.TAG, "Se produjó la excepción: " + response);
                Toast.makeText(PedidosARecogerActivity.this, response, Toast.LENGTH_LONG)
                        .show();
            }
        }

    }
}
