package practica.jdbc.datos;

import java.time.LocalDateTime;

public class MovimientoStock {

    private int idMovimiento;
    private int idVinilo;
    private String tipo;
    private int cantidad;
    private LocalDateTime fechaMovimiento;
    private String motivo;

    public MovimientoStock(int idMovimiento, int idVinilo, String tipo, int cantidad, LocalDateTime fechaMovimiento, String motivo) {
        this.idMovimiento = idMovimiento;
        this.idVinilo = idVinilo;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.fechaMovimiento = fechaMovimiento;
        this.motivo = motivo;
    }

    public int getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(int idMovimiento) {
        this.idMovimiento = idMovimiento;
    }

    public int getIdVinilo() {
        return idVinilo;
    }

    public void setIdVinilo(int idVinilo) {
        this.idVinilo = idVinilo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public LocalDateTime getFechaMovimiento() {
        return fechaMovimiento;
    }

    public void setFechaMovimiento(LocalDateTime fechaMovimiento) {
        this.fechaMovimiento = fechaMovimiento;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
