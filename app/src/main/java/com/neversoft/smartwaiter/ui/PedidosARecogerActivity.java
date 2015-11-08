package com.neversoft.smartwaiter.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.neversoft.smartwaiter.R;
import com.neversoft.smartwaiter.service.ConsultarPedidosRecogerReceiver;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PedidosARecogerActivity extends Activity implements AdapterView.OnItemClickListener {
    public static final String EXTRA_RANDOM="r";
    public  static final String EXTRA_TIME="t";
    public static final String ACTION_EVENT="e";
    private ListView mMenuListView;
    private TextView mDateTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedidos_arecoger);
        overridePendingTransition(0, 0);

        // get reference to the ListView and set its listener
        mMenuListView = (ListView) findViewById(R.id.menu_listview);
        mMenuListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mMenuListView.setOnItemClickListener(this);

        mDateTextView= (TextView) findViewById(R.id.fechaTextView);

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter=new IntentFilter(ACTION_EVENT);

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(onEvent, filter);
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(onEvent);
        super.onPause();
    }
    private BroadcastReceiver onEvent=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DateFormat fmt=new SimpleDateFormat("HH:mm:ss", Locale.US);


            Date date=new Date(intent.getLongExtra(EXTRA_TIME, 0));

            mDateTextView.setText(String.format("%s = %x", fmt.format(date),
                    intent.getIntExtra(EXTRA_RANDOM, -1)));
        }
    };
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
    public void onItemClick(AdapterView<?> parent, View v,int position, long id) {
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
}
