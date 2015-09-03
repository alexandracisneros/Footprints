package com.neversoft.smartwaiter.model.entity;

/**
 * Created by Usuario on 02/09/2015.
 */
public class ArticuloEE {

    private int mId;
    private String mDescripcionNorm;
    private String mUm;
    private String mUmDescripcion;
    private float mPrecio;
    private String mUrl;

    public ArticuloEE() {
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getDescripcionNorm() {
        return mDescripcionNorm;
    }

    public void setDescripcionNorm(String descripcionNorm) {
        mDescripcionNorm = descripcionNorm;
    }

    public String getUm() {
        return mUm;
    }

    public void setUm(String um) {
        mUm = um;
    }

    public String getUmDescripcion() {
        return mUmDescripcion;
    }

    public void setUmDescripcion(String umDescripcion) {
        mUmDescripcion = umDescripcion;
    }

    public float getPrecio() {
        return mPrecio;
    }

    public void setPrecio(float precio) {
        mPrecio = precio;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }
}
