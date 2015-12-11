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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.neversoft.smartwaiter.R;
import com.neversoft.smartwaiter.database.DBHelper;
import com.neversoft.smartwaiter.model.business.ArticuloDAO;
import com.neversoft.smartwaiter.model.business.CategoriaDAO;
import com.neversoft.smartwaiter.model.business.PedidoDAO;
import com.neversoft.smartwaiter.model.entity.ArticuloEE;
import com.neversoft.smartwaiter.model.entity.CategoriaEE;
import com.neversoft.smartwaiter.model.entity.DetallePedidoEE;
import com.neversoft.smartwaiter.model.entity.PedidoEE;
import com.neversoft.smartwaiter.preference.PedidoSharedPref;
import com.neversoft.smartwaiter.service.EnviarPedidoService;
import com.neversoft.smartwaiter.util.Funciones;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

public class TomarPedidoActivity extends Activity
        implements OnItemClickListener {
    private ListView mCategoriasListView;
    private ListView mArticulosListView;
    private ListView mPedidoListView;
    private ArrayList<CategoriaEE> mListaCategorias;
    private ArrayList<ArticuloEE> mListaArticulos;
    private CategoriaDAO mCategoriaDAO;
    private ArticuloDAO mArticuloDAO;
    private ArrayList<DetallePedidoEE> mItems;

    private TextView mSubTotalPedidoTextView;
    private TextView mIGVPedidoTextView;
    private TextView mTotalPedidoTextView;
    private float mTotal = 0;
    private BroadcastReceiver sendDataReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("QuickOrder", "sendData broadcast received");
            // if necessary get data from intent
            boolean exito = intent.getBooleanExtra("exito", false);
            abortBroadcast();
            String mensaje;
            if (exito) {
                PedidoSharedPref.clear(context);
                mItems = new ArrayList<>();
                showItems();
                mensaje = "Pedidos enviados correctamente.";
                Log.d(DBHelper.TAG,
                        "Success from BroadcastReceiver within EnviarDatosActivity : "
                                + mensaje);
            } else {
                mensaje = intent.getStringExtra("mensaje");
                Log.d(DBHelper.TAG, "Exception from BroadcastReceiver within EnviarDatosActivity :"
                        + mensaje);
            }

            // update the display
            Toast.makeText(TomarPedidoActivity.this,
                    mensaje, Toast.LENGTH_LONG).show();
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        setContentView(R.layout.activity_tomar_pedido);
        mCategoriaDAO = new CategoriaDAO(getApplicationContext());
        mArticuloDAO = new ArticuloDAO(getApplicationContext());

        mCategoriasListView = (ListView) findViewById(R.id.categorias_listview);
        mCategoriasListView.setOnItemClickListener(this);

        mArticulosListView = (ListView) findViewById(R.id.articulos_listview);
        mArticulosListView.setOnItemClickListener(this);

        mSubTotalPedidoTextView = (TextView) findViewById(R.id.subTotalPedidoTextView);
        mIGVPedidoTextView = (TextView) findViewById(R.id.igvPedidoTextView);
        mTotalPedidoTextView = (TextView) findViewById(R.id.totalPedidoTextView);
        mPedidoListView = (ListView) findViewById(R.id.detallePedidoListView);

        loadCategorias();
    }

    public ListView getCategoriasListView() {
        return mCategoriasListView;
    }

    public ListView getArticulosListView() {
        return mArticulosListView;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tomar_pedido, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_send:
                //Toast.makeText(this, "Save & Send", Toast.LENGTH_SHORT).show();
                sendData();
                break;
            case R.id.action_just_save:
                saveOrder();
                //Toast.makeText(getActivity(), "Just Save", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_cancelar:
                Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    private void saveOrder() {
        final PedidoDAO pedidoDAO = new PedidoDAO(getApplicationContext());
        final PedidoEE pedido = new PedidoEE();
        pedido.setFecha(Funciones.getCurrentDate("yyyy/MM/dd"));
        pedido.setNroMesa(2);
        pedido.setNroPiso(1);
        pedido.setCantRecogida("");
        pedido.setAmbiente(1);
        pedido.setCodUsuario("200");
        pedido.setCodCliente(100);
        pedido.setTipoVenta("020");
        pedido.setTipoPago("030");
        pedido.setMoneda("SOL");
        pedido.setMontoTotal(mTotal);
        pedido.setMontoRecibido(1500);
        pedido.setEstado("010"); //Se realizo el guardado del pedido pero no se ha enviado a cocina
        pedido.setCodCia("001");
        pedido.setDetalle(mItems);
        new AsyncTask<Void, Void, Object>() {

            @Override
            protected Object doInBackground(Void... voids) {
                try {
                    return pedidoDAO.savePedido(pedido,0); //0=NO ENVIADO A COCINA
                } catch (Exception e) {
                    return e;
                }
            }

            @Override
            protected void onPostExecute(Object result) {
                if (result instanceof Long) {
                    Toast.makeText(TomarPedidoActivity.this, "Operación completada con exito. Id =" + result, Toast.LENGTH_SHORT).show();
                } else if (result instanceof Exception) {
                    Toast.makeText(TomarPedidoActivity.this, "Se produjo la excepción: " + result, Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

        if (adapterView.getId() == R.id.categorias_listview) {
            Toast.makeText(this, "Item Seleccionado : " + mListaCategorias.get(position).getDescripcion(), Toast.LENGTH_SHORT).show();
            int familiaId = Integer.parseInt(mListaCategorias.get(position).getCodigo().trim());
            loadArticulos(familiaId);
        } else if (adapterView.getId() == R.id.articulos_listview) {
            Toast.makeText(this, "Item Seleccionado : " + mListaArticulos.get(position).getDescripcionNorm(), Toast.LENGTH_SHORT).show();
            DetallePedidoEE itemDetalle = new DetallePedidoEE(mListaArticulos.get(position));
            PedidoSharedPref.addItem(this, itemDetalle);
            mItems = PedidoSharedPref.getItems(this);
            showItems();
        } else if (adapterView.getId() == R.id.menu_listview) {
            Toast.makeText(this, "Opcion de Menu : " + ((TextView) view).getText().toString(), Toast.LENGTH_SHORT).show();
        }

    }

    private void showItems() {
        float subTotal = 0;
        float igv = 0;
        //create a List of Map<String,?> objects
        ArrayList<HashMap<String, String>> data =
                new ArrayList<HashMap<String, String>>();
        for (DetallePedidoEE item : mItems) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("articuloDescripcion", item.getDescArticulo());
            map.put("articuloCantidad", String.valueOf(item.getCantidad()));
            map.put("articuloPrecio", String.valueOf(item.getPrecio()));
            data.add(map);

            subTotal += item.getCantidad() * item.getPrecio();
        }

        //create the resouces, from, and to variables
        int resource = R.layout.order_item;
        String[] from = {"articuloDescripcion", "articuloCantidad", "articuloPrecio"};
        int[] to = {R.id.productoPedidoDescTextView, R.id.cantidadPedidoTextView,
                R.id.precioPedidoTextView};

        //create and set the adapter
        SimpleAdapter adapter = new SimpleAdapter(this, data, resource, from, to);
        mPedidoListView.setAdapter(adapter);

        //Calculate and display summary data
        igv = subTotal * 0.19f;
        mTotal = subTotal + igv;
        mSubTotalPedidoTextView.setText(String.valueOf(subTotal));
        mIGVPedidoTextView.setText(String.valueOf(igv));
        mTotalPedidoTextView.setText(String.valueOf(mTotal));
    }

    private void sendData() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmación")
                .setMessage("¿Realmente desea enviar los pedidos para su procesamiento?")
                .setPositiveButton("Si",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                Gson gson = new Gson();
                                //PONER EN FUCNION
                                PedidoEE pedido = new PedidoEE();
                                pedido.setFecha(Funciones.getCurrentDate("yyyy/MM/dd"));
                                pedido.setNroMesa(2);
                                pedido.setNroPiso(1);
                                pedido.setCantRecogida("");
                                pedido.setAmbiente(1);
                                pedido.setCodUsuario("200");
                                pedido.setCodCliente(100);
                                pedido.setTipoVenta("020"); // QUE ESTO?
                                pedido.setTipoPago("030"); // QUE ESTO??
                                pedido.setMoneda("SOL");
                                pedido.setMontoTotal(mTotal);  // ESTE TOTAL NO ESTA COMPLETO ESTA VIAJANDO SOLO LA SUMATORIA Y NO EL TOTAL CON IGV
                                pedido.setMontoRecibido(1500);
                                pedido.setEstado("020"); //Se realizo el guardado del Pedido y se envío a cocina //TODO Mejor pasalo el estado en "savePedido"
                                pedido.setCodCia("001");
                                pedido.setDetalle(mItems);
                                //PONER EN FUNCION


                                String pedidosString = gson.toJson(pedido);
                                Intent iniciarServiceIntent = new Intent(TomarPedidoActivity.this,
                                        EnviarPedidoService.class);
                                iniciarServiceIntent.putExtra("json", pedidosString);
                                Log.d(DBHelper.TAG, "Antes de startService SendDataService");
                                startService(iniciarServiceIntent);
                            }
                        })
                .setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();

                            }
                        }).setIcon(android.R.drawable.ic_dialog_alert).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(sendDataReceiver);
        Log.d(DBHelper.TAG, "Entre a onPause - EnviarDatosActivity");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(DBHelper.TAG, "Entre a onResume - EnviarDatosActivity");
        IntentFilter filter = new IntentFilter(
                EnviarPedidoService.ACTION_SEND_DATA);
        filter.setPriority(2);
        registerReceiver(sendDataReceiver, filter);

    }

    public ArrayList<CategoriaEE> getListaCategorias() {
        return mListaCategorias;
    }

    public ArrayList<ArticuloEE> getListaArticulos() {
        return mListaArticulos;
    }

    public void loadCategorias() {
        mListaCategorias = new ArrayList<CategoriaEE>();
        WeakReference<Activity> weakActivity = new WeakReference<Activity>(this);
        mCategoriaDAO.getCategoriasAsync(weakActivity);
    }

    public void loadArticulos(int familiaId) {
        mListaArticulos = new ArrayList<ArticuloEE>();
        WeakReference<Activity> weakActivity = new WeakReference<Activity>(this);
        mArticuloDAO.getArticuloPorFamiliaAsync(weakActivity, familiaId);
    }
}
