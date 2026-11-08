package practica.jdbc.dao;

import practica.jdbc.datos.Genero;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GeneroDAO {

    private final Connection conexion;

    public GeneroDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public void insertar(Genero genero) throws SQLException {

        String sql = "INSERT INTO genero (nombre, descripcion) VALUES (?, ?)";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, genero.getNombre());
            sentencia.setString(2, genero.getDescripcion());
            sentencia.executeUpdate();
        }
    }

    public Genero buscarPorId(int idGenero) throws SQLException {

        String sql = "SELECT * FROM genero WHERE id_genero = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idGenero);

            try (ResultSet resultado = sentencia.executeQuery()) {

                if (resultado.next()) {

                    int id = resultado.getInt("id_genero");
                    String nombre = resultado.getString("nombre");
                    String descripcion = resultado.getString("descripcion");
                    return new Genero(id, nombre, descripcion);
                }
            }
        }
        return null;
    }

    public List<Genero> listarTodos() throws SQLException {

        List<Genero> generos = new ArrayList<>();

        String sql = "SELECT * FROM genero";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            try (ResultSet resultado = sentencia.executeQuery()) {

                while (resultado.next()) {
                    int id = resultado.getInt("id_genero");
                    String nombre = resultado.getString("nombre");
                    String descripcion = resultado.getString("descripcion");

                    Genero genero = new Genero(id, nombre, descripcion);

                    generos.add(genero);
                }
            }
        }
        return generos;
    }

    public void actualizar(Genero genero) throws SQLException {

        String sql = "UPDATE genero SET nombre = ?, descripcion = ? WHERE id_genero = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, genero.getNombre());
            sentencia.setString(2, genero.getDescripcion());
            sentencia.setInt(3, genero.getIdGenero());

            sentencia.executeUpdate();
        }
    }

    public void eliminar(int idGenero) throws SQLException {

        String sql = "DELETE FROM genero WHERE id_genero = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, idGenero);
            sentencia.executeUpdate();
        }
    }


}
