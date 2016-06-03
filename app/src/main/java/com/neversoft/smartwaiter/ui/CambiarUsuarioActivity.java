package com.neversoft.smartwaiter.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.neversoft.smartwaiter.R;
import com.neversoft.smartwaiter.preference.LoginSharedPref;
import com.neversoft.smartwaiter.util.Funciones;

import java.lang.ref.WeakReference;

public class CambiarUsuarioActivity extends AppCompatActivity
        implements View.OnClickListener,
        NavigationView.OnNavigationItemSelectedListener {
    private NavigationView mNavigationView;
    private SharedPreferences mPrefLogin;
    private TextView mUsuarioTextView;
    private TextView mEmpresaTextView;
    private TextView mUltimoLoginTextView;
    private Button mCambiarUsuarioButton;
    private MaterialDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambiar_usuario);
        overridePendingTransition(0, 0);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mUsuarioTextView = (TextView) findViewById(R.id.usuarioTextView);
        mEmpresaTextView = (TextView) findViewById(R.id.empresaTextView);
        mUltimoLoginTextView = (TextView) findViewById(R.id.ultimoLoginTextView);
        mCambiarUsuarioButton = (Button) findViewById(R.id.cambiarUsuarioButton);

        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.getMenu().getItem(SmartWaiter.OPCION_USUARIO).setChecked(true);

        // get SharedPreferences
        mPrefLogin = getSharedPreferences(LoginSharedPref.NAME, MODE_PRIVATE);
        mUsuarioTextView.setText(mPrefLogin.getString(LoginSharedPref.USUARIO, "Sin establecer"));
        mEmpresaTextView.setText(mPrefLogin.getString(LoginSharedPref.COMPANIA, "Sin establecer"));
        mUltimoLoginTextView.setText(Funciones.getDateFromTimeStamp(
                        mPrefLogin.getLong(LoginSharedPref.FECHA_LOGIN, 0),
                        "dd/MM/yyyy hh:mm:ss"));
    }

    private void showProgressIndicator(boolean showValue) {
        if (showValue) {
            mProgress = new MaterialDialog.Builder(CambiarUsuarioActivity.this)
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cambiarUsuarioButton:
//                boolean isDayStarted = mPrefControl.getBoolean(ControlSharedPref.INICIO_DIA,
//                        false);
//                if (!isDayStarted) {
//                    confirmarIniciarDia();
//                } else {
//                    Toast.makeText(CambiarUsuarioActivity.this,
//                            "Ya se ha iniciado día en el dispositivo.",
//                            Toast.LENGTH_SHORT).show();
//                }
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        if (menuItem.getOrder() != SmartWaiter.OPCION_USUARIO) {
            WeakReference<AppCompatActivity> weakActivity = new WeakReference<AppCompatActivity>(CambiarUsuarioActivity.this);
            Funciones.selectMenuOption(weakActivity, menuItem.getOrder());
            return true;
        }
        return true;
    }
}
