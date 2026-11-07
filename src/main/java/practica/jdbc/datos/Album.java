package practica.jdbc.datos;

public class Album {

    private int idAlbum;
    private String nombre;
    private int anioLanzamiento;
    private int idGenero;

    public Album(int idAlbum, String nombre, int anioLanzamiento, int idGenero) {
        this.idAlbum = idAlbum;
        this.nombre = nombre;
        this.anioLanzamiento = anioLanzamiento;
        this.idGenero = idGenero;
    }

    public int getIdAlbum() {
        return idAlbum;
    }

    public void setIdAlbum(int idAlbum) {
        this.idAlbum = idAlbum;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getAnioLanzamiento() {
        return anioLanzamiento;
    }

    public void setAnioLanzamiento(int anioLanzamiento) {
        this.anioLanzamiento = anioLanzamiento;
    }

    public int getIdGenero() {
        return idGenero;
    }

    public void setIdGenero(int idGenero) {
        this.idGenero = idGenero;
    }
}
