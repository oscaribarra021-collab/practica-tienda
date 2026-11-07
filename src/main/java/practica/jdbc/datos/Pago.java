package practica.jdbc.datos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Pago {

    private int idPago;
    private int idPedido;
    private String metodo;
    private LocalDateTime fechaPago;
    private BigDecimal importe;
    private String estado;

    public Pago(int idPago, int idPedido, String metodo, LocalDateTime fechaPago, BigDecimal importe, String estado) {
        this.idPago = idPago;
        this.idPedido = idPedido;
        this.metodo = metodo;
        this.fechaPago = fechaPago;
        this.importe = importe;
        this.estado = estado;
    }

    public int getIdPago() {
        return idPago;
    }

    public void setIdPago(int idPago) {
        this.idPago = idPago;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public String getMetodo() {
        return metodo;
    }

    public void setMetodo(String metodo) {
        this.metodo = metodo;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
