package com.neversoft.smartwaiter.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.neversoft.smartwaiter.R;
import com.neversoft.smartwaiter.database.DBHelper;
import com.neversoft.smartwaiter.model.business.DetallePedidoDAO;
import com.neversoft.smartwaiter.model.entity.DetallePedidoEE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Usuario on 17/12/2015.
 */
public class DetallePedidoFacturarDialogFragment extends DialogFragment {
    public static final String ARG_ID_PEDIDO = "idPedido";
    private ArrayList<DetallePedidoEE> mItems;
    private ListView mDetallePedidoListView;
    private View mForm = null;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mForm = getActivity().getLayoutInflater().inflate(
                R.layout.fragment_detalle_ped_facturar, null);
        mDetallePedidoListView = (ListView) mForm.findViewById(R.id.detallePedidoFacturarListView);
        String idPedido = getArguments().getString(ARG_ID_PEDIDO);
        new ConsultarItemsPedidoDespachado().execute(idPedido);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_detalle_pedido_title);
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
        return builder.create();

    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positiveButton = (Button) d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        dismiss();
                }
            });
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

    private void mostrarDetallePedido() {
        ArrayList<HashMap<String, String>> data =
                new ArrayList<HashMap<String, String>>();
        for (DetallePedidoEE item : mItems) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("item", String.valueOf(item.getItem()));
            map.put("articulo", item.getDescArticulo());
            map.put("estado", String.valueOf(item.getEstadoArticulo()));
            map.put("cantidad", String.valueOf(item.getCantidad()));
            data.add(map);
        }

        //create the resouces, from, and to variables
        int resource = R.layout.pedido_det_facturar_item;
        String[] from = {"item", "articulo", "estado", "cantidad"};
        int[] to = {R.id.itemDetalleTextView, R.id.articuloDetalleTextView,
                R.id.estadoDetalleTextView, R.id.cantidadDetalleTextView};

        //create and set the adapter
        SimpleAdapter adapter = new SimpleAdapter(getActivity(), data, resource, from, to);
        adapter.notifyDataSetChanged();

        mDetallePedidoListView.setAdapter(adapter);
    }

    private class ConsultarItemsPedidoDespachado extends AsyncTask<String, Void, Object> {

        @Override
        protected Object doInBackground(String... params) {
            Object requestObject;
            String idPedido = params[0];
            try {
                DetallePedidoDAO detallePedidoDAO = new DetallePedidoDAO(getActivity().getApplicationContext());
                requestObject = detallePedidoDAO.getDetallePorEstado(idPedido, -1); //-1 any status
            } catch (Exception e) {
                requestObject = e;
            }
            return requestObject;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (result instanceof List<?>) {
                mItems = (ArrayList<DetallePedidoEE>) result;
                mostrarDetallePedido();
            } else if (result instanceof Exception) {
                String response;
                response = ((Exception) result).getMessage();
                Log.d(DBHelper.TAG, "Se produjó la excepción: " + response);
                Toast.makeText(getActivity(), response, Toast.LENGTH_LONG)
                        .show();
            }
        }
    }
}
