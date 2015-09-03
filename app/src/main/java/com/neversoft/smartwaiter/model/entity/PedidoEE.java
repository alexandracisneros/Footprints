package com.neversoft.smartwaiter.model.entity;

import java.util.ArrayList;

/**
 * Created by Usuario on 02/09/2015.
 */
public class PedidoEE {
    private int mId;
    private String mFecha;
    private int mNroMesa;
    private int mAmbiente;
    private String mCodUsuario;
    private int mCodCliente;
    private String mTipoVenta;
    private String mTipoPago;
    private String mMoneda;
    private float mMontoTotal;
    private float mMontoRecibido;
    private int mEstado;
    private ArrayList<DetallePedidoEE> mDetalle;

    public PedidoEE() {
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getFecha() {
        return mFecha;
    }

    public void setFecha(String fecha) {
        mFecha = fecha;
    }

    public int getNroMesa() {
        return mNroMesa;
    }

    public void setNroMesa(int nroMesa) {
        mNroMesa = nroMesa;
    }

    public int getAmbiente() {
        return mAmbiente;
    }

    public void setAmbiente(int ambiente) {
        mAmbiente = ambiente;
    }

    public String getCodUsuario() {
        return mCodUsuario;
    }

    public void setCodUsuario(String codUsuario) {
        mCodUsuario = codUsuario;
    }

    public int getCodCliente() {
        return mCodCliente;
    }

    public void setCodCliente(int codCliente) {
        mCodCliente = codCliente;
    }

    public String getTipoVenta() {
        return mTipoVenta;
    }

    public void setTipoVenta(String tipoVenta) {
        mTipoVenta = tipoVenta;
    }

    public String getTipoPago() {
        return mTipoPago;
    }

    public void setTipoPago(String tipoPago) {
        mTipoPago = tipoPago;
    }

    public String getMoneda() {
        return mMoneda;
    }

    public void setMoneda(String moneda) {
        mMoneda = moneda;
    }

    public float getMontoTotal() {
        return mMontoTotal;
    }

    public void setMontoTotal(float montoTotal) {
        mMontoTotal = montoTotal;
    }

    public float getMontoRecibido() {
        return mMontoRecibido;
    }

    public void setMontoRecibido(float montoRecibido) {
        mMontoRecibido = montoRecibido;
    }

    public int getEstado() {
        return mEstado;
    }

    public void setEstado(int estado) {
        mEstado = estado;
    }

    public ArrayList<DetallePedidoEE> getDetalle() {
        return mDetalle;
    }

    public void setDetalle(ArrayList<DetallePedidoEE> detalle) {
        mDetalle = detalle;
    }
}
