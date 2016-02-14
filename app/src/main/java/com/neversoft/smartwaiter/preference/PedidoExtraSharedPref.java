package com.neversoft.smartwaiter.preference;

import android.content.SharedPreferences;
import android.util.Log;

import com.neversoft.smartwaiter.database.DBHelper;

/**
 * Created by Usuario on 13/02/2016.
 * Extra info about an order.
 * Basically, the selected table and the activity it was selected from.
 * This is stored in a shared preferences to avoid having to constantly pass
 * the same info through the several activities
 */

public class PedidoExtraSharedPref {
    public static final String NAME = "prefPedidoExtra";
    public static final String SELECTED_TABLE_JSON = "selected_table_json";
    public static final String STARTING_ACTIVITY = "starting_activity";

    public static void save(SharedPreferences prefPedidoExtra, String jsonTable, String startingActivity) {
        SharedPreferences.Editor editor = prefPedidoExtra.edit();
        editor.putString(SELECTED_TABLE_JSON, jsonTable);
        editor.putString(STARTING_ACTIVITY, startingActivity.trim());
        editor.commit();
        Log.d(DBHelper.TAG, "Guarde SharedPreferece 'prefPedidoExtra'");
    }

    public static void remove(SharedPreferences prefPedidoExtra) {
        SharedPreferences.Editor editor = prefPedidoExtra.edit();
        editor.clear();
        editor.commit();
        Log.d(DBHelper.TAG, "Elimine SharedPreferece 'prefPedidoExtra'");
    }
}
