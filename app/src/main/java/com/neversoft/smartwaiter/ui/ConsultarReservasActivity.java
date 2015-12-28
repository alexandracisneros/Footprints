package com.neversoft.smartwaiter.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;

import com.neversoft.smartwaiter.R;
import com.neversoft.smartwaiter.util.Funciones;

import java.lang.ref.WeakReference;

public class ConsultarReservasActivity extends Activity implements AdapterView.OnItemClickListener {
    private EditText mIdClienteEditText;
    private EditText mCodigoReservaEditText;
    private ImageButton mBuscarReservaImageButton;
    private GridView mMesasGridView;
    private ListView mMenuListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultar_reservas);

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
        mMenuListView.setItemChecked(SmartWaiter.OPCION_RESERVAS, true);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v,
                            int position, long id) {
        if (parent.getId() == R.id.mesasGridView) {
            Intent intent = new Intent(this, TomarPedidoActivity.class);
            startActivity(intent);
        } else if (parent.getId() == R.id.menu_listview) {
            if (position != SmartWaiter.OPCION_RESERVAS) {
                WeakReference<Activity> weakActivity = new WeakReference<Activity>(this);
                Funciones.selectMenuOption(weakActivity, position);
            }
        }
    }
}
