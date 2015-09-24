package com.neversoft.smartwaiter.model.entity;

/**
 * Created by Usuario on 28/03/2015.
 */
public class SpinnerEE {
    private int codigo;
    private String descripcion;

    public SpinnerEE() {
        super();
    }

    public SpinnerEE(int codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
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
