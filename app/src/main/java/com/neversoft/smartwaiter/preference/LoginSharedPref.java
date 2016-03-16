package com.neversoft.smartwaiter.preference;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.neversoft.smartwaiter.database.DBHelper;

/**
 * Created by Usuario on 02/10/2015.
 */
public class LoginSharedPref {
    public static final String NAME="prefLogin";
    //public static final String AMBIENTE="ambiente";
    public static final String USUARIO="usuario";
    public static final String CONTRASENA="contrasena";
    public static final String COMPANIA="compania";
    public static final String INGRESO_APLICACION="ingresoAplicacion";

    public static void save(SharedPreferences prefLogin,String usuario,String contrasena,
                            String compania,Boolean ingresoApp,boolean limpiarValoresAnteriores ){
        Editor editor = prefLogin.edit();
        if(limpiarValoresAnteriores) editor.clear();
        //if(usuario!=null)editor.putString(AMBIENTE, ambiente);
        if(usuario!=null)editor.putString(USUARIO, usuario.trim());
        if(contrasena!=null)editor.putString(CONTRASENA, contrasena.trim());
        if(compania!=null)editor.putString(COMPANIA, compania.trim());
        if(ingresoApp!=null) editor.putBoolean(INGRESO_APLICACION, ingresoApp);
        editor.commit();
        Log.d(DBHelper.TAG, "Guarde SharedPreferece 'PREF_Login'");
    }

    public static void remove(SharedPreferences prefLogin) {
        Editor editor = prefLogin.edit();
        editor.clear();
        editor.commit();
        Log.d(DBHelper.TAG, "Elimine SharedPreferece 'PREF_Login'");
    }
}
