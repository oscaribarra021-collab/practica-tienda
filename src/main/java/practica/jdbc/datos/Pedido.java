package practica.jdbc.datos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Pedido {

    private int idPedido;
    private int idCliente;
    private int idDireccion;
    private LocalDateTime fechaPedido;
    private String estado;
    private BigDecimal total;

    public Pedido(int idPedido, int idCliente, int idDireccion, LocalDateTime fechaPedido, String estado, BigDecimal total) {
        this.idPedido = idPedido;
        this.idCliente = idCliente;
        this.idDireccion = idDireccion;
        this.fechaPedido = fechaPedido;
        this.estado = estado;
        this.total = total;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public int getIdDireccion() {
        return idDireccion;
    }

    public void setIdDireccion(int idDireccion) {
        this.idDireccion = idDireccion;
    }

    public LocalDateTime getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(LocalDateTime fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}
