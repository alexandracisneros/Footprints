package com.neversoft.smartwaiter.model.entity;

/**
 * Created by Usuario on 13/12/2015.
 */
public class ClienteEE {
    private int mId;
    private String mRazonSocial;
    private String mTipoPersona;
    private String mNroDocumento;
    private String mDireccion;

    public ClienteEE() {
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getRazonSocial() {
        return mRazonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        mRazonSocial = razonSocial;
    }

    public String getTipoPersona() {
        return mTipoPersona;
    }

    public void setTipoPersona(String tipoPersona) {
        mTipoPersona = tipoPersona;
    }

    public String getNroDocumento() {
        return mNroDocumento;
    }

    public void setNroDocumento(String nroDocumento) {
        mNroDocumento = nroDocumento;
    }

    public String getDireccion() {
        return mDireccion;
    }

    public void setDireccion(String direccion) {
        mDireccion = direccion;
    }
}
