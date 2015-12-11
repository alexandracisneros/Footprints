package com.neversoft.smartwaiter.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.neversoft.smartwaiter.R;
import com.neversoft.smartwaiter.database.DBHelper;
import com.neversoft.smartwaiter.service.SincronizarService;

public class SincronizarActivity extends Activity implements AdapterView.OnItemClickListener{
    private ListView mMenuListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sincronizar);
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
        mMenuListView.setItemChecked(1, true);  //TODO: Put this in some sort of Constant
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
    public void onClick(View v) {
        // here get SharedPreferences and send them with the Intent
        Intent inputIntent = new Intent(SincronizarActivity.this,
                SincronizarService.class);
        Log.d(DBHelper.TAG, "Antes de startService");
        // Display progress to the user
        startService(inputIntent);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v,
                            int position, long id) {
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
                intent = new Intent(this, PedidosFacturarActivity.class);
                startActivity(intent);
                finish();
                break;
            case 5:
                intent = new Intent(this, CerrarDiaActivity.class);
                startActivity(intent);
                finish();
                break;

        }
    }
}
