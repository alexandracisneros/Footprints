package com.neversoft.smartwaiter.ui;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.neversoft.smartwaiter.R;
import com.neversoft.smartwaiter.database.DBHelper;
import com.neversoft.smartwaiter.io.RestConnector;
import com.neversoft.smartwaiter.io.RestUtil;
import com.neversoft.smartwaiter.preference.ConexionSharedPref;
import com.neversoft.smartwaiter.util.Funciones;

import java.net.URLEncoder;


public class ConfigurarConexionDialogFragment extends DialogFragment {
    // define SharedPreferences object
    private SharedPreferences mPrefConexion;
    private EditText mServidorEditText;
    private EditText mAplicacionEditText;
    private EditText mAmbienteEditText;
    private ProgressDialog mProgress;
    private View mForm = null;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mForm = getActivity().getLayoutInflater().inflate(
                R.layout.fragment_configurar_conexion_dialog, null);
        mServidorEditText = (EditText) mForm.findViewById(R.id.servidorEditText);
        mAplicacionEditText = (EditText) mForm.findViewById(R.id.aplicacionEditText);
        mAmbienteEditText = (EditText) mForm.findViewById(R.id.ambienteEditText);

        // get SharedPreferences object
        mPrefConexion = getActivity().getSharedPreferences(ConexionSharedPref.NAME, 0);

        mostrarDatosConexion();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_conf_conexion_title);
        builder.setIcon(R.drawable.ic_action_settings).setView(mForm);
        builder.setPositiveButton(R.string.aceptar,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Do nothing here because we override this button later to change the close behaviour.
                        //However, we still need this because on older versions of Android unless we
                        //pass a handler the button doesn't get instantiated

                    }
                });
        builder.setNegativeButton(R.string.cancelar, null);
        return builder.create();
    }

    private void mostrarDatosConexion() {
        mServidorEditText.setText(mPrefConexion.getString(ConexionSharedPref.SERVIDOR, ""));
        mAplicacionEditText.setText(mPrefConexion.getString(ConexionSharedPref.APLICACION, ""));
        mAmbienteEditText.setText(mPrefConexion.getString(ConexionSharedPref.NOMBRE_AMBIENTE, ""));
    }


    @Override
    public void onStart() {
        //super.onStart() is where dialog.show() is actually called on the underlying dialog, so we have to do it after this point
        super.onStart();
        AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Boolean wantToCloseDialog = false;
                    String msg = "";
                    //Do stuff, possibly set wantToCloseDialog to true then...
                    if (!Funciones.isEditTextEmpty(mServidorEditText)) {
                        if (!Funciones.isEditTextEmpty(mAplicacionEditText)) {
                            if (!Funciones.isEditTextEmpty(mAmbienteEditText)) {
                                ConexionSharedPref.save(mPrefConexion, mServidorEditText.getText().toString().trim(),
                                        mAplicacionEditText.getText().toString().trim(),
                                        mAmbienteEditText.getText().toString().trim(),
                                        true);
                                probarConexion(mServidorEditText.getText().toString().trim(),
                                        mAplicacionEditText.getText().toString().trim(),
                                        mAmbienteEditText.getText().toString().trim()
                                );
                                wantToCloseDialog = true;
                            } else {
                                msg = "Debe ingresar un ambiente.";
                            }
                        } else {
                            msg = "Debe ingresar una aplicación.";
                        }
                    } else {
                        msg = "Debe ingresar la URL del servidor.";

                    }
                    if (!wantToCloseDialog) {
                        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                    }
                    //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                }
            });
        }
    }

    public void conexionProbada(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
        getDialog().dismiss();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

    private void probarConexion(String server, String app, String ambiente) {
        //http://siempresoftqa.cloudapp.net/PruebaMovilAlex/api/restaurante/VerificarParamsConexion/?cadenaConexion=Initial%20Catalog=PRUEBAMOVILJHAV
        String url = "http://%s/%s/api/restaurante/VerificarParamsConexion/?cadenaConexion=%s";
        try {
            String encondedAmbiente = URLEncoder.encode("Initial Catalog=" + ambiente, "utf-8");
            String urlWithParams = String.format(url, server, app, encondedAmbiente);
            new ProbarConfigConexion().execute(urlWithParams);
        } catch (Exception e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    class ProbarConfigConexion extends AsyncTask<String, Void, Object> {
        Context ctxt = ConfigurarConexionDialogFragment.this.getActivity();

        @Override
        protected void onPreExecute() {
            mProgress = new ProgressDialog(ctxt);
            mProgress.setTitle("Procesando");
            mProgress.setMessage("Espere por favor...");
            mProgress.setCancelable(false);
            mProgress.show();
        }

        @Override
        protected Object doInBackground(String... params) {
            Object requestObject = null;
            String url = params[0];
            Log.d(DBHelper.TAG, url);
            RestConnector restConnector;
            try {
                if (Funciones.hasActiveInternetConnection(ctxt.getApplicationContext())) {
                    restConnector = RestUtil.obtainGetConnection(url);
                    requestObject = restConnector.doRequest(url);
                }

            } catch (Exception e) {
                requestObject = e;
            }
            return requestObject;
        }

        @Override
        protected void onPostExecute(Object result) {
            // Clear progress indicator
            String response;
            if (mProgress != null) {
                mProgress.dismiss();
            }
            if (result instanceof String) {
                ConfigurarConexionDialogFragment.this.conexionProbada("Configuración de conexión probada y guardada correctamente.");

            } else if (result instanceof Exception) {
                response = ((Exception) result).getMessage();
                Log.d(DBHelper.TAG, "Error al configurar la conexión: " + response);
                Toast.makeText(ctxt, response, Toast.LENGTH_LONG).show();
            }
        }

    }

}
