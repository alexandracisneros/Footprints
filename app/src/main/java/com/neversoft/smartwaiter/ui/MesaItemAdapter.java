package com.neversoft.smartwaiter.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.neversoft.smartwaiter.R;
import com.neversoft.smartwaiter.model.entity.MesaPisoEE;

import java.util.ArrayList;

/**
 * Created by Usuario on 23/09/2015.
 */
public class MesaItemAdapter  extends BaseAdapter {

    LayoutInflater mLayoutInflater;
    Context mContext;
    private ArrayList<MesaPisoEE> mItems;

    public MesaItemAdapter(Context context, ArrayList<MesaPisoEE> items) {
        this.mContext = context;
        this.mItems = items;
        this.mLayoutInflater = (LayoutInflater) this.mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder wrapper = null;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.mesa_item,
                    parent, false);
            wrapper = new ViewHolder(convertView);
            convertView.setTag(wrapper);
        } else {
            wrapper = (ViewHolder) convertView.getTag();
        }
        wrapper.nro.setText(Integer.toString(mItems.get(position).getNroMesa()));
        wrapper.estado.setText(mItems.get(position).getDescEstado());
        wrapper.reserva.setText(Integer.toString(mItems.get(position).getCodReserva()));
        return convertView;
    }
    public class ViewHolder {
        TextView nro = null;
        TextView estado = null;
        TextView reserva=null;

        ViewHolder(View row) {

            this.nro = (TextView) row.findViewById(R.id.nro_textview);
            this.estado = (TextView) row.findViewById(R.id.estado_textview);
            this.reserva = (TextView) row.findViewById(R.id.reserva_textview);
        }
    }
}
