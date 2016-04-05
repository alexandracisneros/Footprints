package com.neversoft.smartwaiter.ui;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.neversoft.smartwaiter.R;
import com.neversoft.smartwaiter.database.DBHelper;
import com.neversoft.smartwaiter.model.business.ArticuloDAO;
import com.neversoft.smartwaiter.model.business.CategoriaDAO;
import com.neversoft.smartwaiter.model.entity.ArticuloEE;
import com.neversoft.smartwaiter.model.entity.CategoriaEE;
import com.neversoft.smartwaiter.model.entity.DetallePedidoEE;
import com.neversoft.smartwaiter.model.entity.MesaPisoEE;
import com.neversoft.smartwaiter.model.entity.PedidoEE;
import com.neversoft.smartwaiter.preference.PedidoExtraSharedPref;
import com.neversoft.smartwaiter.preference.PedidoSharedPref;
import com.neversoft.smartwaiter.service.ActualizarEstadoMesaService;
import com.neversoft.smartwaiter.service.EnviarPedidoService;
import com.neversoft.smartwaiter.util.Funciones;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

;

public class TomarPedidoActivity extends AppCompatActivity
        implements OnItemClickListener, EditarCantidadItemFragment.Contract {

    private ListView mCategoriasListView;
    private ListView mArticulosListView;
    private ListView mPedidoListView;
    private ArrayList<CategoriaEE> mListaCategorias;
    private ArrayList<ArticuloEE> mListaArticulos;
    private CategoriaDAO mCategoriaDAO;
    private ArticuloDAO mArticuloDAO;
    private ArrayList<DetallePedidoEE> mItems;
    private ActionMode mActionMode;
    private int mSelectedItemsCount;
    private boolean mIsInActionMode = false;
    private MesaPisoEE mMesaPisoEE;

    private TextView mSubTotalPedidoTextView;
    private TextView mIGVPedidoTextView;
    private TextView mTotalPedidoTextView;

    private MaterialDialog mProgress;
    private float mTotal = 0;
    private String mPrevClassName;
    private SharedPreferences mPrefPedidoExtras;
    private BroadcastReceiver onEventActualizarEstadoMesa = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int resultadoOperacion = intent.getIntExtra(ActualizarEstadoMesaService.EXTRA_RESULTADO_ACTUALIZACION, 0);
            String mensajeOperacion = intent.getStringExtra(ActualizarEstadoMesaService.EXTRA_MENSAJE_ACTUALIZACION);

            if (resultadoOperacion > 0) {
                Log.d(DBHelper.TAG, "Resultado de Actualizar Estado de Mesa: " + resultadoOperacion);
                Class<?> clase = MesasActivity.class; //Clase por defecto para evitar asignar null
                try {
                    clase = Class.forName(mPrevClassName);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                PedidoSharedPref.clear(TomarPedidoActivity.this);
                PedidoExtraSharedPref.remove(mPrefPedidoExtras);

                Intent intentTo = new Intent(TomarPedidoActivity.this, clase);
                startActivity(intentTo);
                finish(); // finaliza actividad para que al volver necesariamente se tenga que volver a cargar la actividad
                showProgressIndicator(false);
            } else {
                Log.d(DBHelper.TAG, "Se produjó la excepción: " + mensajeOperacion);
                Toast.makeText(TomarPedidoActivity.this, mensajeOperacion, Toast.LENGTH_LONG).show();
                showProgressIndicator(false);
            }

        }
    };
    private BroadcastReceiver sendDataReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(DBHelper.TAG, "sendData broadcast received");
            //Retrieve extras
            boolean exito = intent.getBooleanExtra(EnviarPedidoService.EXTRA_RESULTADO_EXITO, false);
            String mensaje = intent.getStringExtra(EnviarPedidoService.EXTRA_RESULTADO_MENSAJE);

            abortBroadcast();

            if (exito) {
                mensaje = "Pedidos enviados correctamente.";
                Log.d(DBHelper.TAG, "Success from BroadcastReceiver within EnviarDatosActivity : "
                        + mensaje);
                Class<?> clase = MesasActivity.class; //Clase por defecto para evitar asignar null

                try {
                    clase = Class.forName(mPrevClassName);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                PedidoSharedPref.clear(TomarPedidoActivity.this);
                PedidoExtraSharedPref.remove(mPrefPedidoExtras);
                Intent toIntent = new Intent(TomarPedidoActivity.this, clase);
                startActivity(toIntent);
                finish();
                showProgressIndicator(false);
            } else {
                showProgressIndicator(false);
                Log.d(DBHelper.TAG, "Exception from BroadcastReceiver within EnviarDatosActivity :" + mensaje);
            }
            Toast.makeText(TomarPedidoActivity.this, mensaje, Toast.LENGTH_LONG).show();
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        setContentView(R.layout.activity_tomar_pedido);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //Retrieve Preferences
        mPrefPedidoExtras = getSharedPreferences(PedidoExtraSharedPref.NAME, MODE_PRIVATE);
        mPrevClassName = mPrefPedidoExtras.getString(PedidoExtraSharedPref.STARTING_ACTIVITY, MesasActivity.class.getClass().getName());
        String mesaString = mPrefPedidoExtras.getString(PedidoExtraSharedPref.SELECTED_TABLE_JSON, null);

        Gson gson = new Gson();
        mMesaPisoEE = gson.fromJson(mesaString, MesaPisoEE.class);

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
        mPedidoListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mPedidoListView.setMultiChoiceModeListener(new ActionModeCallbacks());

        loadCategorias();
        mItems = PedidoSharedPref.getItems(TomarPedidoActivity.this);
        showItems();
    }

    @Override
    public void onBackPressed() {
        confirmarCancelarPedido();
    }

    private void showProgressIndicator(boolean showValue) {
        if (showValue) {
            mProgress = new MaterialDialog.Builder(TomarPedidoActivity.this)
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
            case R.id.action_cancelar:
                //Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show();
                confirmarCancelarPedido();
                break;
        }
        return true;
    }

    private void confirmarCancelarPedido() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmación")
                .setMessage("¿Realmente desea cancelar el pedido actual?")
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        dialog.cancel();

                        Intent serviceIntent = new Intent(TomarPedidoActivity.this, ActualizarEstadoMesaService.class);
                        //Put Extras
                        serviceIntent.putExtra(ActualizarEstadoMesaService.EXTRA_NUEVO_ESTADO_MESA, "LIB"); //LIBRE
                        serviceIntent.putExtra(ActualizarEstadoMesaService.EXTRA_NUEVO_ESTADO_RESERVA, "CAN"); //CANCELADO

                        Log.d(DBHelper.TAG, "Antes de startService ActualizarEstadoMesaService (Cancelar Pedido)");
                        showProgressIndicator(true);
                        startService(serviceIntent);

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();

                    }
                }).show();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

        if (adapterView.getId() == R.id.categorias_listview) {
            //Toast.makeText(this, "Item Seleccionado : " + mListaCategorias.get(position).getDescripcion(), Toast.LENGTH_SHORT).show();
            int familiaId = Integer.parseInt(mListaCategorias.get(position).getCodigo().trim());
            loadArticulos(familiaId);
        } else if (adapterView.getId() == R.id.articulos_listview) {
            //Toast.makeText(this, "Item Seleccionado : " + mListaArticulos.get(position).getDescripcionNorm(), Toast.LENGTH_SHORT).show();
            DetallePedidoEE itemDetalle = new DetallePedidoEE(mListaArticulos.get(position));
            PedidoSharedPref.addItem(this, itemDetalle);
            mItems = PedidoSharedPref.getItems(this);
            showItems();
        }

    }

    private void showItems() {
        float subTotal = 0;
        float igv;
        //create a List of Map<String,?> objects
        ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
        if (mItems == null) {
            mItems = new ArrayList<>();
        }
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
                                PedidoEE pedido;
                                pedido = createPedido();

                                String pedidosString = gson.toJson(pedido);
                                Intent iniciarServiceIntent = new Intent(TomarPedidoActivity.this, EnviarPedidoService.class);
                                //Put extras
                                iniciarServiceIntent.putExtra(EnviarPedidoService.EXTRA_PEDIDO_JSON, pedidosString);

                                Log.d(DBHelper.TAG, "Antes de startService SendDataService");
                                showProgressIndicator(true);
                                startService(iniciarServiceIntent);

                            }
                        })
                .setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();

                            }
                        }).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(sendDataReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onEventActualizarEstadoMesa);
        Log.d(DBHelper.TAG, "Entre a onPause - TomarPedidoActivity");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(DBHelper.TAG, "Entre a onResume - TomarPedidoActivity");
        IntentFilter filter = new IntentFilter(EnviarPedidoService.ACTION_SEND_DATA);
        filter.setPriority(2);
        registerReceiver(sendDataReceiver, filter);
        IntentFilter filterNotificarActualizarEstadoMesa = new IntentFilter(ActualizarEstadoMesaService.ACTION_UPDATE_TABLE_STATUS);
        LocalBroadcastManager.getInstance(this).registerReceiver(onEventActualizarEstadoMesa, filterNotificarActualizarEstadoMesa);

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

    private PedidoEE createPedido() {
        // Retrieve  Shared Preferences
        SharedPreferences prefConfig = getApplicationContext().getSharedPreferences(
                LoginActivity.PREF_CONFIG, Context.MODE_PRIVATE);
        String codUsuario = prefConfig.getString("Usuario", "").toUpperCase(Locale.getDefault());
        String codCia = prefConfig.getString("CodCia", "");

        PedidoEE pedido = new PedidoEE();

        pedido.setFecha(Funciones.getCurrentDate("yyyy/MM/dd"));
        pedido.setNroMesa(mMesaPisoEE.getNroMesa());
        pedido.setNroPiso(mMesaPisoEE.getNroPiso());
        pedido.setCantRecogida("");
        pedido.setAmbiente(mMesaPisoEE.getCodAmbiente());
        pedido.setCodUsuario(codUsuario);
        pedido.setCodCliente(0);
        pedido.setTipoVenta("");
        pedido.setTipoPago("");
        pedido.setMoneda("SOL");
        pedido.setMontoTotal(mTotal);  // ESTE TOTAL NO ESTA COMPLETO ESTA VIAJANDO SOLO LA SUMATORIA Y NO EL TOTAL CON IGV
        pedido.setMontoRecibido(0);
        pedido.setEstado("020"); //020 = Se realizo el guardado del Pedido y se envío a cocina // 010 = Se realizo el guardado del pedido pero no se ha enviado a cocina
        pedido.setCodCia(codCia);
        pedido.setDetalle(mItems);
        return pedido;
    }

    @Override
    public void OnEditarCantidadItemClick(DetallePedidoEE item, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            PedidoSharedPref.updateItem(TomarPedidoActivity.this, item);
            mItems = PedidoSharedPref.getItems(TomarPedidoActivity.this);
            showItems();
        }
        mActionMode.finish();
    }


    public class ActionModeCallbacks implements AbsListView.MultiChoiceModeListener {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            TomarPedidoActivity.this.getMenuInflater().inflate(R.menu.menu_tomar_pedido_contextual, menu);
            mActionMode = mode;
            mIsInActionMode = true;
            mode.setTitle(String.format("%d Selected", mSelectedItemsCount));
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            if (mSelectedItemsCount == 1) {
                showOptionsOnSingleSelectedItem(true, menu);
                return true;
            } else {
                showOptionsOnSingleSelectedItem(false, menu);
                return true;
            }
        }

        private void showOptionsOnSingleSelectedItem(boolean showOnSingle, Menu menu) {
            MenuItem item = menu.findItem(R.id.menu_plus);
            item.setVisible(showOnSingle);
            item = menu.findItem(R.id.menu_minus);
            item.setVisible(showOnSingle);
            item = menu.findItem(R.id.menu_edit);
            item.setVisible(showOnSingle);
        }

        //"Don't you fucking play the hero again OK! Because I'll die if something happens to you". If only Fer would have remembered what David said to him :'(
        private void modifyProductQuantiy(int accion, SparseBooleanArray checkedItemPositions) {
            if (checkedItemPositions.valueAt(0)) {
                int position = checkedItemPositions.keyAt(0);
                DetallePedidoEE itemDetalle = mItems.get(position);
                if (accion == 0) {
                    itemDetalle.setCantidad(itemDetalle.getCantidad() + 1);
                } else {
                    if (itemDetalle.getCantidad() > 1) {
                        itemDetalle.setCantidad(itemDetalle.getCantidad() - 1);
                    } else {
                        mItems.remove(itemDetalle); //If quantity equals 1 then remove it!
                    }
                }
                PedidoSharedPref.saveItems(TomarPedidoActivity.this, mItems);
                showItems();
            }
        }

        private void removeSelectedItems(SparseBooleanArray checkedItemPositions) {
            for (int i = (checkedItemPositions.size() - 1); i >= 0; i--) {
                if (checkedItemPositions.valueAt(i)) {
                    int position = checkedItemPositions.keyAt(i);
                    DetallePedidoEE itemDetalle = mItems.get(position);
                    mItems.remove(itemDetalle);
                }
            }
            PedidoSharedPref.saveItems(TomarPedidoActivity.this, mItems);
            showItems();
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            //ArrayAdapter<String> adapter = (ArrayAdapter<String>) mItemsPedidoListView.getAdapter();
            SparseBooleanArray checkedItemPositions = mPedidoListView.getCheckedItemPositions();
            switch (item.getItemId()) {
                case R.id.menu_delete:
                    removeSelectedItems(checkedItemPositions);
                    mode.finish();
                    return true;
                case R.id.menu_edit:
                    int position = checkedItemPositions.keyAt(0);
                    DetallePedidoEE itemDetalle = mItems.get(position);
                    EditarCantidadItemFragment.newInstance(itemDetalle).show(TomarPedidoActivity.this.getFragmentManager(), "EditarCantidadFragment");
                    return true;
                case R.id.menu_plus:
                    modifyProductQuantiy(0, checkedItemPositions);
                    mode.finish();
                    return true;
                case R.id.menu_minus:
                    modifyProductQuantiy(1, checkedItemPositions);
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mIsInActionMode = false;
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            mSelectedItemsCount = mPedidoListView.getCheckedItemCount();
            mode.setTitle(String.format("%d Seleccionados", mSelectedItemsCount));
            mode.invalidate();
        }
    }

}
