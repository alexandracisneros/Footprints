package com.neversoft.smartwaiter.model.entity;

/**
 * Created by Usuario on 02/09/2015.
 */
public class CategoriaEE {
    private int mId;
    private String mCodigo;
    private String mDescripcion;
    private String mUrl;

    public CategoriaEE() {
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getCodigo() {
        return mCodigo;
    }

    public void setCodigo(String codigo) {
        mCodigo = codigo;
    }

    public String getDescripcion() {
        return mDescripcion;
    }

    public void setDescripcion(String descripcion) {
        mDescripcion = descripcion;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }
}
