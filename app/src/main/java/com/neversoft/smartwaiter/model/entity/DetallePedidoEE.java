package com.neversoft.smartwaiter.model.entity;

/**
 * Created by Usuario on 02/09/2015.
 */
public class DetallePedidoEE {
    private int mId;
    private int mPedidoId;
    private int mItem;
    private int mCodArticulo;
    private String mUm;
    private float mCantidad;
    private float mPrecio;
    private int mTipoArticulo;
    private int mCodArticuloPrincipal;
    private String mComentario;
    private int mEstadoArticulo;
    private String mDescEstadoArticulo;
    private String mDescArticulo; //FALTA AGREGARLO A LA BD PORQUE SINO AL MOSTRAR HABRIA QUE VOLVER A CONSULTAR LA BD SOLO X LOS NOMBRES

    public DetallePedidoEE() {
    }

    public DetallePedidoEE(ArticuloEE articulo) {
        this.setCodArticulo(articulo.getId());
        this.setUm(articulo.getUm());
        this.setDescArticulo(articulo.getDescripcionNorm());
        this.setCantidad(1); //1 item por defecto.
        this.setPrecio(articulo.getPrecio());
        this.setEstadoArticulo(0); //No enviado a cocina
        this.setDescEstadoArticulo("");
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public int getPedidoId() {
        return mPedidoId;
    }

    public void setPedidoId(int pedidoId) {
        mPedidoId = pedidoId;
    }

    public int getItem() {
        return mItem;
    }

    public void setItem(int item) {
        mItem = item;
    }

    public int getCodArticulo() {
        return mCodArticulo;
    }

    public void setCodArticulo(int codArticulo) {
        mCodArticulo = codArticulo;
    }

    public String getUm() {
        return mUm;
    }

    public void setUm(String um) {
        mUm = um;
    }

    public float getCantidad() {
        return mCantidad;
    }

    public void setCantidad(float cantidad) {
        mCantidad = cantidad;
    }

    public float getPrecio() {
        return mPrecio;
    }

    public void setPrecio(float precio) {
        mPrecio = precio;
    }

    public int getTipoArticulo() {
        return mTipoArticulo;
    }

    public void setTipoArticulo(int tipoArticulo) {
        mTipoArticulo = tipoArticulo;
    }

    public int getCodArticuloPrincipal() {
        return mCodArticuloPrincipal;
    }

    public void setCodArticuloPrincipal(int codArticuloPrincipal) {
        mCodArticuloPrincipal = codArticuloPrincipal;
    }

    public String getComentario() {
        return mComentario;
    }

    public void setComentario(String comentario) {
        mComentario = comentario;
    }

    public int getEstadoArticulo() {
        return mEstadoArticulo;
    }

    public void setEstadoArticulo(int estadoArticulo) {
        mEstadoArticulo = estadoArticulo;
    }

    public String getDescEstadoArticulo() {
        return mDescEstadoArticulo;
    }

    public void setDescEstadoArticulo(String descEstadoArticulo) {
        mDescEstadoArticulo = descEstadoArticulo;
    }

    public String getDescArticulo() {
        return mDescArticulo;
    }

    public void setDescArticulo(String descArticulo) {
        mDescArticulo = descArticulo;
    }

    //This functions has been overriden so that I can use CONTAINS in the ArrayList of Details. See PedidoSharedPreference.addItem
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DetallePedidoEE)) {
            return false;
        }
        DetallePedidoEE item = (DetallePedidoEE) o;
        return this.getCodArticulo() == item.getCodArticulo();
    }
}
