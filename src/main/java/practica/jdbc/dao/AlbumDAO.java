
package practica.jdbc.dao;

import practica.jdbc.datos.Album;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AlbumDAO {

    private final Connection conexion;

    public AlbumDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public void insertar(Album album) throws SQLException {

        String sql = "INSERT INTO album (nombre, anio_lanzamiento, id_genero) VALUES (?, ?, ?)";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, album.getNombre());
            sentencia.setInt(2, album.getAnioLanzamiento());
            sentencia.setInt(3, album.getIdGenero());
            sentencia.executeUpdate();
        }
    }

    public Album buscarPorId(int idAlbum) throws SQLException {

        String sql = "SELECT * FROM album WHERE id_album = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idAlbum);

            try (ResultSet resultado = sentencia.executeQuery()) {

                if (resultado.next()) {

                    int id = resultado.getInt("id_album");
                    String nombre = resultado.getString("nombre");
                    int anioLanzamiento = resultado.getInt("anio_lanzamiento");
                    int idGenero = resultado.getInt("id_genero");
                    return new Album(id, nombre, anioLanzamiento, idGenero);
                }
            }
        }
        return null;
    }

    public List<Album> listarTodos() throws SQLException {

        List<Album> albumes = new ArrayList<>();

        String sql = "SELECT * FROM album";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

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

    public void actualizar(Album album) throws SQLException {

        String sql = "UPDATE album SET nombre = ?, anio_lanzamiento = ?, id_genero = ? WHERE id_album = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, album.getNombre());
            sentencia.setInt(2, album.getAnioLanzamiento());
            sentencia.setInt(3, album.getIdGenero());
            sentencia.setInt(4, album.getIdAlbum());

            sentencia.executeUpdate();
        }
    }

    public void eliminar(int idAlbum) throws SQLException {

        String sql = "DELETE FROM album WHERE id_album = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, idAlbum);
            sentencia.executeUpdate();
        }
    }
}
