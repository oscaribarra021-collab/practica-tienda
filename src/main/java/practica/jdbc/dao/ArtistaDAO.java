
package practica.jdbc.dao;

import practica.jdbc.datos.Artista;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ArtistaDAO {

    private final Connection conexion;

    public ArtistaDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public void insertar(Artista artista) throws SQLException {

        String sql = "INSERT INTO artista (nombre, pais_origen) VALUES (?, ?)";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, artista.getNombre());
            sentencia.setString(2, artista.getPaisOrigen());
            sentencia.executeUpdate();
        }
    }

    public Artista buscarPorId(int idArtista) throws SQLException {

        String sql = "SELECT * FROM artista WHERE id_artista = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idArtista);

            try (ResultSet resultado = sentencia.executeQuery()) {

                if (resultado.next()) {

                    int id = resultado.getInt("id_artista");
                    String nombre = resultado.getString("nombre");
                    String paisOrigen = resultado.getString("pais_origen");
                    return new Artista(id, nombre, paisOrigen);
                }
            }
        }
        return null;
    }

    public List<Artista> listarTodos() throws SQLException {

        List<Artista> artistas = new ArrayList<>();

        String sql = "SELECT * FROM artista";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

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

    public void actualizar(Artista artista) throws SQLException {

        String sql = "UPDATE artista SET nombre = ?, pais_origen = ? WHERE id_artista = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, artista.getNombre());
            sentencia.setString(2, artista.getPaisOrigen());
            sentencia.setInt(3, artista.getIdArtista());

            sentencia.executeUpdate();
        }
    }

    public void eliminar(int idArtista) throws SQLException {

        String sql = "DELETE FROM artista WHERE id_artista = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, idArtista);
            sentencia.executeUpdate();
        }
    }
}
