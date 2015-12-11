package com.neversoft.smartwaiter.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.neversoft.smartwaiter.R;
import com.neversoft.smartwaiter.model.entity.SpinnerEE;

import java.util.ArrayList;

public class PedidosFacturarActivity extends Activity
        implements AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener {

    private ListView mMenuListView;
    private ArrayList<SpinnerEE> mListaTipoVenta;
    private ArrayList<SpinnerEE> mListaTipoPago;
    private Spinner mTipoVentaSpinner;
    private Spinner mTipoPagoSpinner;

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
        initListaTipoVenta();
        initListaTipoPago();
    }

    private void initListaTipoVenta() {
        //CEF: Efectivo, CCC: Tarjeta de Crédito
        mListaTipoVenta=new ArrayList<>();
        mListaTipoVenta.add(new SpinnerEE("CEF", "Efectivo"));
        mListaTipoVenta.add(new SpinnerEE("CCC", "Tarjeta de Crédito"));
        mTipoVentaSpinner.setOnItemSelectedListener(this);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, mListaTipoVenta);
        mTipoVentaSpinner.setAdapter(adapter);

    }

    private void initListaTipoPago() {
        //01: Factura, 03: Boleta
        mListaTipoPago=new ArrayList<>();
        mListaTipoPago.add(new SpinnerEE("01", "Factura"));
        mListaTipoPago.add(new SpinnerEE("03", "Boleta"));
        mTipoPagoSpinner.setOnItemSelectedListener(this);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, mListaTipoPago);
        mTipoPagoSpinner.setAdapter(adapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view,
                               int position, long id) {
        switch (parent.getId()) {
            case R.id.tipoVentaSpinner:
//                Log.d(DBHelper.TAG,"Tipo Venta:" + mListaTipoVenta.get(position).getDescripcion());
                Toast.makeText(this, "Tipo Venta:" + mListaTipoVenta.get(position).getDescripcion(), Toast.LENGTH_SHORT).show();
                break;
            case R.id.tipoPagoSpinner:
//                Log.d(DBHelper.TAG,"Tipo Pago:" + mListaTipoPago.get(position).getDescripcion());
                Toast.makeText(this, "Tipo Pago:" + mListaTipoPago.get(position).getDescripcion(), Toast.LENGTH_SHORT).show();
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
}
