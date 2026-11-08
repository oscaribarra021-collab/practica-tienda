package practica.jdbc.dao;

import practica.jdbc.datos.Album;
import practica.jdbc.datos.Artista;
import practica.jdbc.datos.AlbumArtista;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AlbumArtistaDAO {

    private final Connection conexion;

    public AlbumArtistaDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public void asociarArtistaAlbum(AlbumArtista albumArtista) throws SQLException {

        String sql = "INSERT INTO album_artista (id_album, id_artista) VALUES (?, ?)";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, albumArtista.getIdAlbum());
            sentencia.setInt(2, albumArtista.getIdArtista());
            sentencia.executeUpdate();
        }
    }

    public List<Artista> obtenerArtistasPorAlbum(int idAlbum) throws SQLException {

        List<Artista> artistas = new ArrayList<>();

        String sql = "SELECT a.id_artista, a.nombre, a.pais_origen " + "FROM artista a "
                + "INNER JOIN album_artista aa ON a.id_artista = aa.id_artista " + "WHERE aa.id_album = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idAlbum);

            try (ResultSet resultado = sentencia.executeQuery()) {

                while (resultado.next()) {

                    int id = resultado.getInt("id_artista");
                    String nombre = resultado.getString("nombre");
                    String paisOrigen = resultado.getString("pais_origen");

                    Artista artista = new Artista(id, nombre, paisOrigen);

                    artistas.add(artista);
                }
            }
        }
        return artistas;
    }

    public List<Album> obtenerAlbumesPorArtista(int idArtista) throws SQLException {

        List<Album> albumes = new ArrayList<>();

        String sql = "SELECT a.id_album, a.nombre, a.anio_lanzamiento, a.id_genero " + "FROM album a "
                + "INNER JOIN album_artista aa ON a.id_album = aa.id_album " + "WHERE aa.id_artista = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idArtista);

            try (ResultSet resultado = sentencia.executeQuery()) {

                while (resultado.next()) {

                    int id = resultado.getInt("id_album");
                    String nombre = resultado.getString("nombre");
                    int anioLanzamiento = resultado.getInt("anio_lanzamiento");
                    int idGenero = resultado.getInt("id_genero");

                    Album album = new Album(id, nombre, anioLanzamiento, idGenero);

                    albumes.add(album);
                }
            }
        }
        return albumes;
    }

    public void eliminarAsociacion(int idAlbum, int idArtista) throws SQLException {

        String sql = "DELETE FROM album_artista WHERE id_album = ? AND id_artista = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, idAlbum);
            sentencia.setInt(2, idArtista);
            sentencia.executeUpdate();
        }
    }
}