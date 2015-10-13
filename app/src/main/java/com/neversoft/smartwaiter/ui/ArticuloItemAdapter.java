package com.neversoft.smartwaiter.ui;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.neversoft.smartwaiter.R;
import com.neversoft.smartwaiter.model.entity.ArticuloEE;
import com.neversoft.smartwaiter.model.entity.CategoriaEE;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Usuario on 10/09/2015.
 */
public class ArticuloItemAdapter extends BaseAdapter {
    LayoutInflater mLayoutInflater;
    Context mContext;
    int mSize;
    private ArrayList<ArticuloEE> mItems;

    public ArticuloItemAdapter(Context context, ArrayList<ArticuloEE> items) {
        this.mContext = context;
        this.mItems = items;
        this.mSize=context.getResources().getDimensionPixelSize(R.dimen.icon);
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
            convertView = mLayoutInflater.inflate(R.layout.imagelist_item_row,
                    parent, false);
            wrapper = new ViewHolder(convertView);
            convertView.setTag(wrapper);
        } else {
            wrapper = (ViewHolder) convertView.getTag();
        }
        Picasso.with(mContext).load(TextUtils.isEmpty(mItems.get(position).getUrl()) ? null : mItems.get(position).getUrl())
                .placeholder(R.drawable.owner_placeholder)
                .resize(mSize,mSize)
                .centerCrop()
                .error(R.drawable.owner_error)
                .into(wrapper.icon);
        wrapper.text.setText(mItems.get(position).getDescripcionNorm());
        return convertView;
    }

    public class ViewHolder {
        ImageView icon = null;
        TextView text = null;

        ViewHolder(View row) {
            this.icon = (ImageView) row.findViewById(R.id.picture_imageview);
            this.text = (TextView) row.findViewById(R.id.desc_textview);
        }
    }
}
