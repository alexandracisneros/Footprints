package com.neversoft.smartwaiter.preference;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.neversoft.smartwaiter.database.DBHelper;

/**
 * Created by Usuario on 30/09/2015.
 */
public class ConexionSharedPref {
    public static final String NAME = "prefConexion";
    public static final String SERVIDOR = "servidor";
    public static final String APLICACION = "aplicacion";
    public static final String NOMBRE_AMBIENTE = "nombre_ambiente";
    public static final String AMBIENTE = "ambiente";
    // To check whether all the fields were completed or not
    public static final String DATOS_COMPLETOS = "datosCcompletos";

    public static void save(SharedPreferences prefConexion, String servidor,
                            String aplicacion, String nombreAmbiente, Boolean datosCompletos) {
        Editor editor = prefConexion.edit();
        if (servidor != null)
            editor.putString(SERVIDOR, servidor);
        if (aplicacion != null)
            editor.putString(APLICACION, aplicacion);
        if (nombreAmbiente != null) {
            editor.putString(NOMBRE_AMBIENTE, nombreAmbiente);
            editor.putString(AMBIENTE, "Initial Catalog=" + nombreAmbiente);
        }
        if (datosCompletos != null)
            editor.putBoolean(DATOS_COMPLETOS, datosCompletos);
        editor.commit();
        Log.d(DBHelper.TAG, "Guarde SharedPreferece 'PREF_CONEXION'");
    }

    public static void remove(SharedPreferences prefControl) {
        Editor editor = prefControl.edit();
        editor.clear();
        editor.commit();
        Log.d("QuickOrder", "Elimine SharedPreferece 'PREF_CONEXION'");
    }
}
