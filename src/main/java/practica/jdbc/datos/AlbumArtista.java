package practica.jdbc.datos;

public class AlbumArtista {

    private int idAlbum;
    private int idArtista;

    public AlbumArtista(int idAlbum, int idArtista) {
        this.idAlbum = idAlbum;
        this.idArtista = idArtista;
    }

    public int getIdAlbum() {
        return idAlbum;
    }

    public void setIdAlbum(int idAlbum) {
        this.idAlbum = idAlbum;
    }

    public int getIdArtista() {
        return idArtista;
    }

    public void setIdArtista(int idArtista) {
        this.idArtista = idArtista;
    }
}
