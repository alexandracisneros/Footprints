package com.neversoft.smartwaiter.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.neversoft.smartwaiter.model.entity.DetallePedidoEE;

import java.util.ArrayList;

/**
 * Created by Usuario on 21/11/2015.
 */
public class ItemsRecogidosAdapter  extends BaseAdapter{
    Context mContext;
    LayoutInflater mLayoutInflater;
    ArrayList<DetallePedidoEE> mDetallePedido;

    @Override
    public int getCount() {
        return mDetallePedido.size();
    }

    @Override
    public Object getItem(int position) {
        return mDetallePedido.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
