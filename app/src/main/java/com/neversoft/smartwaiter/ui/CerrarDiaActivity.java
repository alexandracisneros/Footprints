package com.neversoft.smartwaiter.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.neversoft.smartwaiter.R;
import com.neversoft.smartwaiter.database.DBHelper;
import com.neversoft.smartwaiter.io.RestConnector;
import com.neversoft.smartwaiter.io.RestUtil;
import com.neversoft.smartwaiter.model.business.PedidoDAO;
import com.neversoft.smartwaiter.model.business.SincroDAO;
import com.neversoft.smartwaiter.preference.ConexionSharedPref;
import com.neversoft.smartwaiter.preference.ControlSharedPref;
import com.neversoft.smartwaiter.util.Funciones;

import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.Locale;

;

public class CerrarDiaActivity extends AppCompatActivity
        implements View.OnClickListener,
        NavigationView.OnNavigationItemSelectedListener {
    private String mUrlServer;
    private NavigationView mNavigationView;
    private Button mCerrarDiaButton;
    // define SharedPreferences object
    private SharedPreferences mPrefConfig;
    private SharedPreferences mPrefControl;
    private SharedPreferences mPrefConexion;
    private FrameLayout mIndicatorFrameLayout;
    private LinearLayout mMainRelativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cerrar_dia);
        overridePendingTransition(0, 0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // get SharedPreferences
        mPrefConfig = getSharedPreferences(LoginActivity.PREF_CONFIG, MODE_PRIVATE);
        mPrefControl = getSharedPreferences(ControlSharedPref.NAME, MODE_PRIVATE);
        mPrefConexion = getSharedPreferences(ConexionSharedPref.NAME, MODE_PRIVATE);

        mCerrarDiaButton = (Button) findViewById(R.id.cerrarDiaButton);
        mCerrarDiaButton.setOnClickListener(this);

        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.getMenu().getItem(SmartWaiter.OPCION_CERRAR_DIA).setChecked(true);

        mUrlServer = RestUtil.obtainURLServer(getApplicationContext());

        mIndicatorFrameLayout = (FrameLayout) findViewById(R.id.loadingIndicatorLayout);
        mMainRelativeLayout = (LinearLayout) findViewById(R.id.mainRelativeLayout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cerrar_dia, menu);

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

    private void showProgressIndicator(boolean showValue) {
        if (showValue) {
            mMainRelativeLayout.setVisibility(View.GONE);
            mIndicatorFrameLayout.setVisibility(View.VISIBLE);
        } else {
            mMainRelativeLayout.setVisibility(View.VISIBLE);
            mIndicatorFrameLayout.setVisibility(View.GONE);
        }
    }

    public void confirmarCerrarDiaSinPedidos() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmación")
                .setMessage(
                        "No ha realizado ningún pedido.\n¿Desea cerrar el día de todas maneras?")
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Start daily operations
                        // AQUI HACER LO MISMO QUE CON ENVIAR DATA
                        dialog.cancel();
                        cerrarDia();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();

                    }
                }).setIcon(android.R.drawable.ic_dialog_alert).show();

    }

    private void cerrarDia() {
        String url = mUrlServer + "ventas/CerrarDiaVendedorMV/?"
                + "fecha=%s&codVen=%s&" + "codCia=%s&usuario=%s&"
                + "cadenaConexion=%s";

//         String fecha = Funciones.getCurrentDate("yyyy/MM/dd");
        String fecha = mPrefControl
                .getString(ControlSharedPref.FECHA_INICIO_DIA, Funciones.getCurrentDate("yyyy/MM/dd"));
        String codMozo = mPrefConfig.getString("CodMozo", "");
        String codCia = mPrefConfig.getString("CodCia", "");
        String usuario = mPrefConfig.getString("Usuario", "").toUpperCase(
                Locale.getDefault());
        String ambiente = mPrefConexion.getString(ConexionSharedPref.AMBIENTE, "");
        Log.d(DBHelper.TAG, ambiente);
        try {
            String encondedAmbiente = URLEncoder.encode(ambiente, "utf-8");
            String urlWithParams = String.format(url, fecha, codMozo, codCia,
                    usuario, encondedAmbiente);
            new DoCerrarDia().execute(urlWithParams);
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View view) {
        boolean isDayStarted;
        boolean isDataSynchronized;
        isDayStarted = mPrefControl.getBoolean(ControlSharedPref.INICIO_DIA,
                false);
//        if (isDayStarted) {
//            isDataSynchronized = mPrefControl.getBoolean(
//                    ControlSharedPref.DATA_SINCRONIZADA, false);
//            if (isDataSynchronized) {
        confirmarRealizacionDePedidos();
//            } else {
//                Toast.makeText(CerrarDiaActivity.this,
//                        "Aún no ha sincronizado los datos.",
//                        Toast.LENGTH_SHORT).show();
//            }
//        } else {
//            Toast.makeText(CerrarDiaActivity.this,
//                    "Debe iniciar el día antes de intentar cerrarlo.",
//                    Toast.LENGTH_SHORT).show();
//        }

    }

    public void confirmarRealizacionDePedidos() {
        try {
            boolean isDayClosed = mPrefControl.getBoolean(ControlSharedPref.CIERRE_DIA,
                    false);
            PedidoDAO pedidoDAO = new PedidoDAO(getApplicationContext());
            if (isDayClosed) {
                Toast.makeText(CerrarDiaActivity.this,
                        "Día ya ha sido cerrado.", Toast.LENGTH_SHORT).show();
            } else {
                long nroPedidos = pedidoDAO.getNumeroPedidos(-1);
                if (nroPedidos > 0) {
                    confirmarCerrarDiaConPedidos(pedidoDAO);
                } else {
                    confirmarCerrarDiaSinPedidos();
                }
            }
        } catch (Exception ex) {
            Toast.makeText(CerrarDiaActivity.this,
                    "Se produjo el error: " + ex.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }

    }

    public void confirmarCerrarDiaConPedidos(PedidoDAO pedidoDAO) throws Exception {
        long nroPedidoNoEnviados = pedidoDAO.getNumeroPedidos(0); // Pendientes de Envio
        String mensaje;
        if (nroPedidoNoEnviados > 0) {
            mensaje = "Hay pedidos que aún no han sido enviados. De proceder no podrá enviarlos.\n¿Confirma que desea cerrar el día?";
            ;
        } else {
            mensaje = "De proceder no podrá agregar nuevos pedidos.\n¿Confirma que desea cerrar el día?";
        }
        new AlertDialog.Builder(this).setTitle("Confirmación")
                .setMessage(mensaje)
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Start daily operations
                        dialog.cancel();
                        cerrarDia();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();

                    }
                }).setIcon(android.R.drawable.ic_dialog_alert).show();

    }


    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        if (menuItem.getOrder() != SmartWaiter.OPCION_CERRAR_DIA) {
            WeakReference<Activity> weakActivity = new WeakReference<Activity>(CerrarDiaActivity.this);
            Funciones.selectMenuOption(weakActivity, menuItem.getOrder());
            return true;
        }
        return true;
    }

    class DoCerrarDia extends AsyncTask<String, Void, Object> {

        @Override
        protected void onPreExecute() {
            showProgressIndicator(true);
        }

        @Override
        protected Object doInBackground(String... params) {
            Object requestObject = null;
            String url = params[0];
            Log.d(DBHelper.TAG, url);
            RestConnector restConnector;
            try {
                if (Funciones
                        .hasActiveInternetConnection(getApplicationContext())) {
                    restConnector = RestUtil.obtainGetConnection(url);
                    requestObject = restConnector.doRequest(url);
                }
            } catch (Exception e) {
                requestObject = e;
            }
            SystemClock.sleep(5000);
            return requestObject;

        }

        @Override
        protected void onPostExecute(Object result) {
            // Clear progress indicator
            String mensaje = "";
            showProgressIndicator(false);
            if (result instanceof String) {
                mensaje = (String) result;
                if (mensaje.equals("1")) {
                    try {
                        // Since the user is closing the day we need to
                        // reset all values from PREF_Control,except cerrarDia,
                        // will do that when we sync data
                        // PREF_Login, and the Downloaded Data
                        ControlSharedPref.save(mPrefControl, false, "", true, false,
                                false, "", false);
                        // Clear out mPrefLoginValues
                        // PREF_Login.remove(mPrefLogin); //MANTENER XQ LO NECESITO
                        // PARA DATOS ADICIONALES SINO SALE EXECPCION XQ NECESITO
                        // USUARIO
                        // Clear out mPrefTrasc
                        //PREF_Transac.remove(mPrefTransac); //TODO : REVISAR PORQUE ES NECESARIO
                        // Clear out mPrefPoliticas
                        //PREF_Politicas.remove(mPrefPoliticas); //TODO : REVISAR PORQUE ES NECESARIO
                        // ACA CORREGIR
                        //TODO: realizar esto que tiene reversion primero y luego recien lo de las sharedpreferences que no tienen reversion
                        SincroDAO sincroDAO = new SincroDAO(getApplicationContext());
                        sincroDAO.dropDataDownloaded(); //TODO : REVISAR PORQUE ES NECESARIO

                        mensaje = "Día cerrado correctamente.";
                    } catch (Exception ex) {
                        Toast.makeText(CerrarDiaActivity.this, "Se produjo el error:" + ex.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }

            } else if (result instanceof Exception) {
                mensaje = ((Exception) result).getMessage();
                Log.d(DBHelper.TAG, "Error en Cerrar dia: " + mensaje);
            }
            Toast.makeText(CerrarDiaActivity.this, mensaje,
                    Toast.LENGTH_LONG).show();
        }

    }
}
