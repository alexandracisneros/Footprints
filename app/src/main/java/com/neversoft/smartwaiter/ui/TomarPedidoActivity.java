package com.neversoft.smartwaiter.ui;


import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.neversoft.smartwaiter.R;
import com.neversoft.smartwaiter.model.business.ArticuloDAO;
import com.neversoft.smartwaiter.model.business.CategoriaDAO;
import com.neversoft.smartwaiter.model.entity.ArticuloEE;
import com.neversoft.smartwaiter.model.entity.CategoriaEE;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class TomarPedidoActivity extends Activity
        implements OnItemClickListener {
    private ListView mMenuListView;
    private ListView mCategoriasListView;
    private ListView mArticulosListView;
    private ArrayList<CategoriaEE> mListaCategorias;
    private ArrayList<ArticuloEE> mListaArticulos;
    private CategoriaDAO mCategoriaDAO;
    private ArticuloDAO mArticuloDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        setContentView(R.layout.activity_tomar_pedido);
        mCategoriaDAO = new CategoriaDAO(getApplicationContext());
        mArticuloDAO = new ArticuloDAO(getApplicationContext());

        // get reference to the ListView and set its listener
        mMenuListView = (ListView) findViewById(R.id.menu_listview);
        mMenuListView.setOnItemClickListener(this);

        mCategoriasListView = (ListView) findViewById(R.id.categorias_listview);
        mCategoriasListView.setOnItemClickListener(this);

        mArticulosListView = (ListView) findViewById(R.id.articulos_listview);
        mArticulosListView.setOnItemClickListener(this);

        Resources res = getResources();
        String[] options = res.getStringArray(R.array.menu_items_array);
        ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, options);
        mMenuListView.setAdapter(itemsAdapter);

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
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

        if (adapterView.getId() == R.id.categorias_listview) {
            Toast.makeText(this, "Item Seleccionado : " + mListaCategorias.get(position).getDescripcion(), Toast.LENGTH_SHORT).show();
            int familiaId = Integer.parseInt(mListaCategorias.get(position).getCodigo().trim());
            loadArticulos(familiaId);
        } else if (adapterView.getId() == R.id.articulos_listview) {
            Toast.makeText(this, "Item Seleccionado : " + mListaArticulos.get(position).getDescripcionNorm(), Toast.LENGTH_SHORT).show();
        } else if (adapterView.getId() == R.id.menu_listview) {
            Toast.makeText(this, "Opcion de Menu : " + ((TextView) view).getText().toString(), Toast.LENGTH_SHORT).show();
        }

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
