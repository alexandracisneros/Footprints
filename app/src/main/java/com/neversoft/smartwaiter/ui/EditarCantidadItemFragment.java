package com.neversoft.smartwaiter.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;


import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.neversoft.smartwaiter.R;
import com.neversoft.smartwaiter.model.entity.DetallePedidoEE;

/**
 * Created by Usuario on 14/03/2016.
 */
public class EditarCantidadItemFragment extends DialogFragment implements
        DialogInterface.OnClickListener {
    //http://android-developers.blogspot.com/2009/01/avoiding-memory-leaks.html
    public interface Contract{
        void OnEditarCantidadItemClick(DetallePedidoEE item, int which);
    }
    private View form = null;
    private DetallePedidoEE mItem;
    private String mNuevaCantidad;

    public static EditarCantidadItemFragment newInstance(DetallePedidoEE item) {
        EditarCantidadItemFragment frag = new EditarCantidadItemFragment();
        frag.setItemDetalle(item);
        return frag;
    }

    public void setItemDetalle(DetallePedidoEE item) {
        this.mItem = item;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        form = getActivity().getLayoutInflater()
                .inflate(R.layout.fragment_editar_cant_item, null);
        TextView descripcionTextView = (TextView) form.findViewById(R.id.articuloDescTextView);
        EditText cantidadEditText = (EditText) form.findViewById(R.id.articuloCantidadEditText);
        descripcionTextView.setText(mItem.getDescArticulo());
        if (mNuevaCantidad == null) {
            cantidadEditText.setText(String.valueOf(mItem.getCantidad()));
        } else {
            cantidadEditText.setText(mNuevaCantidad);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder.setTitle("Editar Cantidad").setView(form)
                .setPositiveButton("Aceptar", this)
                .setNegativeButton("Cancelar", this).create();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        EditText cantidadEditText = (EditText) form.findViewById(R.id.articuloCantidadEditText);
        mNuevaCantidad = cantidadEditText.getText().toString();
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            EditText cantidad = (EditText) form.findViewById(R.id.articuloCantidadEditText);
            mItem.setCantidad(Float.valueOf(cantidad.getText().toString()));
        }
        getContract().OnEditarCantidadItemClick(mItem,which);

    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }
    private Contract getContract() {
        return((Contract)getActivity());
    }
}
