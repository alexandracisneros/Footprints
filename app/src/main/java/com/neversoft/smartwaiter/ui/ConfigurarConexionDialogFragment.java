package com.neversoft.smartwaiter.ui;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.neversoft.smartwaiter.R;
import com.neversoft.smartwaiter.preference.ConexionSharedPref;
import com.neversoft.smartwaiter.util.Funciones;


public class ConfigurarConexionDialogFragment extends DialogFragment {
    // define SharedPreferences object
    private SharedPreferences mPrefConexion;
    private EditText mServidorEditText;
    private EditText mAplicacionEditText;
    private EditText mAmbienteEditText;
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
        builder.setIcon(R.drawable.ic_settings).setView(mForm);
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
            Button positiveButton = (Button) d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Boolean wantToCloseDialog = false;
                    String msg = "";
                    //Do stuff, possibly set wantToCloseDialog to true then...
                    if (!Funciones.isEditTextEmpty(mServidorEditText)) {
                        if (!Funciones.isEditTextEmpty(mAplicacionEditText)) {
                            if (!Funciones.isEditTextEmpty(mAmbienteEditText)) {
                                ConexionSharedPref.save(mPrefConexion, mServidorEditText
                                                .getText().toString().trim(),
                                        mAplicacionEditText.getText().toString().trim(),
                                        mAmbienteEditText.getText().toString().trim(),
                                        true);
                                msg = "Configuración de conexión guardada correctamente.";
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

                    Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
                    if (wantToCloseDialog)
                        dismiss();
                    //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
                }
            });
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

}
