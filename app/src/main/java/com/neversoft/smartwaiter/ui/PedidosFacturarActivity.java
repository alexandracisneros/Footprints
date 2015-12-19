package com.neversoft.smartwaiter.ui;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.neversoft.smartwaiter.R;
import com.neversoft.smartwaiter.database.DBHelper;
import com.neversoft.smartwaiter.model.business.ClienteDAO;
import com.neversoft.smartwaiter.model.business.PedidoDAO;
import com.neversoft.smartwaiter.model.entity.ClienteEE;
import com.neversoft.smartwaiter.model.entity.PedidoEE;
import com.neversoft.smartwaiter.model.entity.SpinnerEE;
import com.neversoft.smartwaiter.service.EnviarPedidoFacturadoService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PedidosFacturarActivity extends Activity
        implements AdapterView.OnItemSelectedListener,
        AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener,
        View.OnClickListener {

    public static final String EXTRA_ID_PEDIDO = "id_pedido";
    public static final String EXTRA_ID_PEDIDO_SERV = "id_pedido_servidor";
    public static final String EXTRA_TIPO_VENTA = "pedido_tipo_venta";
    public static final String EXTRA_TIPO_PAGO = "pedido_tipo_pago";
    public static final String EXTRA_RUC = "pedido_ruc";
    public static final String EXTRA_REFRESCAR_LIST_VIEW = "refrescar_list_view";

    private ListView mMenuListView;
    private ListView mCabPedidosFacturarListView;
    private ArrayList<SpinnerEE> mListaTipoVenta;
    private Spinner mTipoVentaSpinner;
    private ArrayList<SpinnerEE> mListaTipoPago;
    private Spinner mTipoPagoSpinner;
    private TextView mTotalTextView;
    private TextView mClienteTextView;
    private EditText mRucEditText;
    private ImageButton mBuscarClieButton;
    private Button mAceptarButton;
    private EditText mMontoRecibidoEditText;
    private TextView mMontoRestanteTextView;
    private TableRow mClienteRow;
    private TableRow mRucRow;
    private TableRow mRecibidoRow;
    private TableRow mRestanteRow;

    private BroadcastReceiver onEvent = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean refrescarListView = intent.getBooleanExtra(EXTRA_REFRESCAR_LIST_VIEW, false);
            if (refrescarListView) {
                new ConsultarPedidosPorFacturar().execute();
                //TODO : NO SOLO ES RECARGAR LA LISTA DE PEDIDOS, tambien limpiar el formulario y actualizar los otros campos del pedido (recibido y restante)
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedidos_facturar);
        overridePendingTransition(0, 0);

        // get reference to the ListView and set its listener
        mMenuListView = (ListView) findViewById(R.id.menu_listview);
        mMenuListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mMenuListView.setOnItemClickListener(this);

        Resources res = getResources();
        String[] options = res.getStringArray(R.array.menu_items_array);
        ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, options);
        mMenuListView.setAdapter(itemsAdapter);
        mMenuListView.setItemChecked(4, true);  //TODO: Put this in some sort of Constant

        mTipoVentaSpinner = (Spinner) findViewById(R.id.tipoVentaSpinner);
        mTipoPagoSpinner = (Spinner) findViewById(R.id.tipoPagoSpinner);
        mTotalTextView = (TextView) findViewById(R.id.totalTextView);
        mClienteTextView = (TextView) findViewById(R.id.clienteTextView);
        mRucEditText = (EditText) findViewById(R.id.rucEditText);
        mMontoRecibidoEditText = (EditText) findViewById(R.id.montoRecibidoEditText);
        mMontoRestanteTextView = (TextView) findViewById(R.id.restanteTextView);
        mBuscarClieButton = (ImageButton) findViewById(R.id.buscarClieImageButton);
        mBuscarClieButton.setOnClickListener(this);
        mAceptarButton = (Button) findViewById(R.id.aceptarButton);
        mAceptarButton.setOnClickListener(this);
        mClienteRow = (TableRow) findViewById(R.id.clienteRow);
        mRucRow = (TableRow) findViewById(R.id.rucRow);
        mRecibidoRow = (TableRow) findViewById(R.id.recibidoRow);
        mRestanteRow = (TableRow) findViewById(R.id.restanteRow);


        mCabPedidosFacturarListView = (ListView) findViewById(R.id.cabeceraPedidoFacturarListView);
        mCabPedidosFacturarListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mCabPedidosFacturarListView.setOnItemClickListener(this);
        mCabPedidosFacturarListView.setOnItemLongClickListener(this);
        new ConsultarPedidosPorFacturar().execute();

        initListaTipoVenta();
        initListaTipoPago();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(EnviarPedidoFacturadoService.ACTION_SEND_ORDERS_TO_INVOICE);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(onEvent, filter);
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(onEvent);
        super.onPause();
    }

    private void initListaTipoVenta() {
        //CEF: Efectivo, CCC: Tarjeta de Crédito
        mListaTipoVenta = new ArrayList<>();
        mListaTipoVenta.add(new SpinnerEE("01", "Factura"));
        mListaTipoVenta.add(new SpinnerEE("03", "Boleta"));
        mTipoVentaSpinner.setOnItemSelectedListener(this);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, mListaTipoVenta);
        mTipoVentaSpinner.setAdapter(adapter);

    }

    private void initListaTipoPago() {
        //01: Factura, 03: Boleta
        mListaTipoPago = new ArrayList<>();
        mListaTipoPago.add(new SpinnerEE("CEF", "Efectivo"));
        mListaTipoPago.add(new SpinnerEE("CCC", "Tarjeta de Crédito"));
        mTipoPagoSpinner.setOnItemSelectedListener(this);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, mListaTipoPago);
        mTipoPagoSpinner.setAdapter(adapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view,
                               int position, long id) {
        switch (parent.getId()) {
            case R.id.tipoVentaSpinner:
                Toast.makeText(this, "Tipo Venta:" + mListaTipoVenta.get(position).getDescripcion(), Toast.LENGTH_SHORT).show();
                if (mListaTipoVenta.get(position).getCodigo().equals("01")) { //FACTURA
                    mRucRow.setVisibility(View.VISIBLE);
                    mClienteRow.setVisibility(View.VISIBLE);
                } else {
                    mRucRow.setVisibility(View.INVISIBLE);
                    mClienteRow.setVisibility(View.INVISIBLE);
                }
                break;
            case R.id.tipoPagoSpinner:
                Toast.makeText(this, "Tipo Pago:" + mListaTipoPago.get(position).getDescripcion(), Toast.LENGTH_SHORT).show();
                if (mListaTipoPago.get(position).getCodigo().equals("CEF")) { //EFECTIVO
                    mRecibidoRow.setVisibility(View.VISIBLE);
                    mRestanteRow.setVisibility(View.VISIBLE);
                } else {
                    mRecibidoRow.setVisibility(View.INVISIBLE);
                    mRestanteRow.setVisibility(View.INVISIBLE);
                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.menu_listview) {
            opcionesMenu(position);
        } else if (parent.getId() == R.id.cabeceraPedidoFacturarListView) {
            String montoTotal = ((TextView) view.findViewById(R.id.importePedidoTextView)).getText().toString();
            mTotalTextView.setText(montoTotal);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        String idPedido = ((TextView) view.findViewById(R.id.nroPedidoTextView)).getText().toString();
        Bundle args = new Bundle();
        args.putString(DetallePedidoFacturarDialogFragment.ARG_ID_PEDIDO, idPedido);
        DialogFragment newFragment = new DetallePedidoFacturarDialogFragment();
        newFragment.setArguments(args);
        newFragment.show(getFragmentManager(), "dlgDetalleFacturar");

        //TODO: ACA ME QUDE 7:51
        //http://stackoverflow.com/questions/15459209/passing-argument-to-dialogfragment
        //http://stackoverflow.com/questions/17622622/how-to-pass-data-from-a-fragment-to-a-dialogfragment

        return true;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.buscarClieImageButton) {
            new ConsultarClientePorRUC().execute(mRucEditText.getText().toString().trim());
        } else if (view.getId() == R.id.aceptarButton) {
            int position = mCabPedidosFacturarListView.getCheckedItemPosition();
            View childView = mCabPedidosFacturarListView.getChildAt(position);
            String idPedido = ((TextView) childView.findViewById(R.id.nroPedidoTextView)).getText().toString();
            String idPedidoServ = ((TextView) childView.findViewById(R.id.nroPedidoServTextView)).getText().toString();
            String tipoVenta = mListaTipoVenta.get(mTipoVentaSpinner.getSelectedItemPosition()).getCodigo();
            String tipoPago = mListaTipoPago.get(mTipoPagoSpinner.getSelectedItemPosition()).getCodigo();
            String ruc = (tipoVenta.equals("01") ? mRucEditText.getText().toString().trim() : "");
            Intent i = new Intent(PedidosFacturarActivity.this, EnviarPedidoFacturadoService.class);
            i.putExtra(EXTRA_ID_PEDIDO, idPedido);
            i.putExtra(EXTRA_ID_PEDIDO_SERV, idPedidoServ);
            i.putExtra(EXTRA_TIPO_VENTA, tipoVenta);
            i.putExtra(EXTRA_TIPO_PAGO, tipoPago);
            i.putExtra(EXTRA_RUC, ruc);
            startService(i);
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
                intent = new Intent(this, PedidosARecogerActivity.class);
                startActivity(intent);
                finish();
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

    private void mostrarPedidosPorFacturar(List<PedidoEE> lista) {
        ArrayList<HashMap<String, String>> data =
                new ArrayList<HashMap<String, String>>();
        for (PedidoEE item : lista) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("pedidoNro", String.valueOf(item.getId()));
            map.put("pedidoMesa", String.valueOf(item.getNroMesa()));
            map.put("pedidoPiso", String.valueOf(item.getNroPiso()));
            map.put("pedidoImporte", String.valueOf(item.getMontoTotal()));
            map.put("nroPedidoServ", String.valueOf(item.getNroPedidoServidor()));
            data.add(map);
        }

        //create the resouces, from, and to variables
        int resource = R.layout.pedido_cab_facturar_item;
        String[] from = {"pedidoNro", "pedidoMesa", "pedidoPiso", "pedidoImporte", "nroPedidoServ"};
        int[] to = {R.id.nroPedidoTextView, R.id.nroMesaPedidoTextView, R.id.nroPisoPedidoTextView,
                R.id.importePedidoTextView, R.id.nroPedidoServTextView};

        //create and set the adapter
        SimpleAdapter adapter = new SimpleAdapter(this, data, resource, from, to);
        adapter.notifyDataSetChanged();

        mCabPedidosFacturarListView.setAdapter(adapter);
    }


    private class ConsultarPedidosPorFacturar extends AsyncTask<Void, Void, Object> {
        @Override
        protected Object doInBackground(Void... voids) {
            Object requestObject;
            try {
                PedidoDAO pedidoDAO = new PedidoDAO(getApplicationContext());
                requestObject = pedidoDAO.getPedidosPorFacturar();
            } catch (Exception e) {
                requestObject = e;
            }
            return requestObject;
        }

        @Override
        protected void onPostExecute(Object result) {

            if (result instanceof List<?>) {
                List<PedidoEE> listaPedidos = (List<PedidoEE>) result;
                mostrarPedidosPorFacturar(listaPedidos);

            } else if (result instanceof Exception) {
                String response;
                response = ((Exception) result).getMessage();
                Log.d(DBHelper.TAG, "Se produjó la excepción: " + response);
                Toast.makeText(PedidosFacturarActivity.this, response, Toast.LENGTH_LONG)
                        .show();
            }
        }

    }

    private class ConsultarClientePorRUC extends AsyncTask<String, Void, Object> {
        @Override
        protected Object doInBackground(String... params) {
            Object requestObject;
            try {
                String ruc = params[0];
                ClienteDAO clienteDAO = new ClienteDAO(getApplicationContext());
                requestObject = clienteDAO.getClienteByRuc(ruc);
            } catch (Exception e) {
                requestObject = e;
            }
            return requestObject;
        }

        @Override
        protected void onPostExecute(Object result) {

            if (result instanceof ClienteEE) {
                ClienteEE cliente = (ClienteEE) result;
                String razonSocial = (cliente.getRazonSocial() == null ? "xxx" : cliente.getRazonSocial().trim());
                mClienteTextView.setText(razonSocial);
            } else if (result instanceof Exception) {
                String response;
                response = ((Exception) result).getMessage();
                Log.d(DBHelper.TAG, "Se produjó la excepción: " + response);
                Toast.makeText(PedidosFacturarActivity.this, response, Toast.LENGTH_LONG)
                        .show();
            }
        }

    }
}
