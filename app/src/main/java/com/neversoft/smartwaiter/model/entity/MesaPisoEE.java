package com.neversoft.smartwaiter.model.entity;

/**
 * Created by Usuario on 02/09/2015.
 */
public class MesaPisoEE {
    private int mId;
    private int mNroPiso;
    private int mCodAmbiente;
    private String mDescAmbiente;
    private int mNroMesa;
    private int mNroAsientos;
    private String mCodEstado;
    private String mDescEstado;
    private int mCodReserva;
    private String mHTMLColor;

    public MesaPisoEE() {
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public int getNroPiso() {
        return mNroPiso;
    }

    public void setNroPiso(int nroPiso) {
        mNroPiso = nroPiso;
    }

    public int getCodAmbiente() {
        return mCodAmbiente;
    }

    public void setCodAmbiente(int codAmbiente) {
        mCodAmbiente = codAmbiente;
    }

    public String getDescAmbiente() {
        return mDescAmbiente;
    }

    public void setDescAmbiente(String descAmbiente) {
        mDescAmbiente = descAmbiente;
    }

    public int getNroMesa() {
        return mNroMesa;
    }

    public void setNroMesa(int nroMesa) {
        mNroMesa = nroMesa;
    }

    public int getNroAsientos() {
        return mNroAsientos;
    }

    public void setNroAsientos(int nroAsientos) {
        mNroAsientos = nroAsientos;
    }

    public String getCodEstado() {
        return mCodEstado;
    }

    public void setCodEstado(String codEstado) {
        mCodEstado = codEstado;
    }

    public String getDescEstado() {
        return mDescEstado;
    }

    public void setDescEstado(String descEstado) {
        mDescEstado = descEstado;
    }

    public int getCodReserva() {
        return mCodReserva;
    }

    public void setCodReserva(int codReserva) {
        mCodReserva = codReserva;
    }

    public String getHTMLColor() {
        return mHTMLColor;
    }

    public void setHTMLColor(String HTMLColor) {
        mHTMLColor = HTMLColor;
    }
}
