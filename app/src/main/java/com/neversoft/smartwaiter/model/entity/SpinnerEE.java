package com.neversoft.smartwaiter.model.entity;

/**
 * Created by Usuario on 28/03/2015.
 */
public class SpinnerEE {
    private String codigo;
    private String descripcion;

    public SpinnerEE() {
        super();
    }

    public SpinnerEE(String codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return getDescripcion();
    }
}
