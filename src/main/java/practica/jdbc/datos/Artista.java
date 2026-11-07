package practica.jdbc.datos;

public class Artista {

    private int idArtista;
    private String nombre;
    private String paisOrigen;

    public Artista(int idArtista, String nombre, String paisOrigen) {
        this.idArtista = idArtista;
        this.nombre = nombre;
        this.paisOrigen = paisOrigen;
    }

    public int getIdArtista() {
        return idArtista;
    }

    public void setIdArtista(int idArtista) {
        this.idArtista = idArtista;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPaisOrigen() {
        return paisOrigen;
    }

    public void setPaisOrigen(String paisOrigen) {
        this.paisOrigen = paisOrigen;
    }
}
