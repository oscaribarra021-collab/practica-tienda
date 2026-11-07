package practica.jdbc.datos;

import java.math.BigDecimal;

public class Vinilo {

    private int idVinilo;
    private int idAlbum;
    private String referencia;
    private String color;
    private BigDecimal precio;

    public Vinilo(int idVinilo, int idAlbum, String referencia, String color, BigDecimal precio) {
        this.idVinilo = idVinilo;
        this.idAlbum = idAlbum;
        this.referencia = referencia;
        this.color = color;
        this.precio = precio;
    }

    public int getIdVinilo() {
        return idVinilo;
    }

    public void setIdVinilo(int idVinilo) {
        this.idVinilo = idVinilo;
    }

    public int getIdAlbum() {
        return idAlbum;
    }

    public void setIdAlbum(int idAlbum) {
        this.idAlbum = idAlbum;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }
}
