package practica.jdbc.datos;

import java.math.BigDecimal;

public class LineaPedido {

    private int idLinea;
    private int idPedido;
    private int idVinilo;
    private int cantidad;
    private BigDecimal precioUnitario;

    public LineaPedido(int idLinea, int idPedido, int idVinilo, int cantidad, BigDecimal precioUnitario) {
        this.idLinea = idLinea;
        this.idPedido = idPedido;
        this.idVinilo = idVinilo;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public int getIdLinea() {
        return idLinea;
    }

    public void setIdLinea(int idLinea) {
        this.idLinea = idLinea;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public int getIdVinilo() {
        return idVinilo;
    }

    public void setIdVinilo(int idVinilo) {
        this.idVinilo = idVinilo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }
}
