package practica.jdbc.datos;

public class Inventario {

    private int idInventario;
    private int idVinilo;
    private int stockActual;

    public Inventario(int idInventario, int idVinilo, int stockActual) {
        this.idInventario = idInventario;
        this.idVinilo = idVinilo;
        this.stockActual = stockActual;
    }

    public int getIdInventario() {
        return idInventario;
    }

    public void setIdInventario(int idInventario) {
        this.idInventario = idInventario;
    }

    public int getIdVinilo() {
        return idVinilo;
    }

    public void setIdVinilo(int idVinilo) {
        this.idVinilo = idVinilo;
    }

    public int getStockActual() {
        return stockActual;
    }

    public void setStockActual(int stockActual) {
        this.stockActual = stockActual;
    }
}
