package com.neversoft.smartwaiter.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.google.gson.Gson;
import com.neversoft.smartwaiter.model.entity.DetallePedidoEE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Usuario on 14/09/2015.
 */
public class PedidoSharedPreference {

    public static final String PREFS_NAME = "Pref_pedido";
    public static final String PREF_PEDIDO_ACTUAL = "pedido_actual";

    // This four methods are used for maintaining pedido.
    public static void saveItems(Context context, List<DetallePedidoEE> items) {
        SharedPreferences settings;
        Editor editor;

        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        editor = settings.edit();

        Gson gson = new Gson();
        String jsonItems = gson.toJson(items);

        editor.putString(PREF_PEDIDO_ACTUAL, jsonItems);
        //editor.clear();//BORRAR
        editor.commit();
    }

    public static void addItem(Context context, DetallePedidoEE item) {
        List<DetallePedidoEE> items = getItems(context);
        if (items == null)
            items = new ArrayList<DetallePedidoEE>();
        if (items.contains(item)) {  //This requires that the DetallePedidoEE overrides the equals method
            DetallePedidoEE detalle = items.get(items.indexOf(item));
            detalle.setCantidad(detalle.getCantidad() + 1);
        } else {
            items.add(item);
        }
        saveItems(context, items);
    }

    public static void updateItem(Context context, DetallePedidoEE item) {
        List<DetallePedidoEE> items = getItems(context);
        if (items == null)
            items = new ArrayList<DetallePedidoEE>();
        if (items.contains(item)) {  //This requires that the DetallePedidoEE overrides the equals method
            if (item.getCantidad() >= 1) {
                DetallePedidoEE detalle = items.get(items.indexOf(item));
                detalle.setCantidad(item.getCantidad());
            } else {
                items.remove(item);
            }
        }
        saveItems(context, items);
    }

    public static void removeItem(Context context, DetallePedidoEE item) {
        ArrayList<DetallePedidoEE> items = getItems(context);
        if (items != null) {
            items.remove(item);
            saveItems(context, items);
        }
    }

    public static ArrayList<DetallePedidoEE> getItems(Context context) {
        SharedPreferences preferences;
        List<DetallePedidoEE> items;

        preferences = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);

        if (preferences.contains(PREF_PEDIDO_ACTUAL)) {
            String jsonItems = preferences.getString(PREF_PEDIDO_ACTUAL, null);
            Gson gson = new Gson();
            DetallePedidoEE[] pedidoItems = gson.fromJson(jsonItems,
                    DetallePedidoEE[].class);

            items = Arrays.asList(pedidoItems);
            items = new ArrayList<DetallePedidoEE>(items);
        } else
            return null;

        return (ArrayList<DetallePedidoEE>) items;
    }
}
