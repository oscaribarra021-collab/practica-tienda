
package practica.jdbc.dao;

import practica.jdbc.datos.Vinilo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ViniloDAO {

    private final Connection conexion;

    public ViniloDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public void insertar(Vinilo vinilo) throws SQLException {

        String sql = "INSERT INTO vinilo (id_album, referencia, color, precio) VALUES (?, ?, ?, ?)";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, vinilo.getIdAlbum());
            sentencia.setString(2, vinilo.getReferencia());
            sentencia.setString(3, vinilo.getColor());
            sentencia.setBigDecimal(4, vinilo.getPrecio());
            sentencia.executeUpdate();
        }
    }

    public Vinilo buscarPorId(int idVinilo) throws SQLException {

        String sql = "SELECT * FROM vinilo WHERE id_vinilo = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idVinilo);

            try (ResultSet resultado = sentencia.executeQuery()) {

                if (resultado.next()) {

                    int id = resultado.getInt("id_vinilo");
                    int idAlbum = resultado.getInt("id_album");
                    String referencia = resultado.getString("referencia");
                    String color = resultado.getString("color");
                    BigDecimal precio = resultado.getBigDecimal("precio");
                    return new Vinilo(id, idAlbum, referencia, color, precio);
                }
            }
        }
        return null;
    }

    public List<Vinilo> listarTodos() throws SQLException {

        List<Vinilo> vinilos = new ArrayList<>();

        String sql = "SELECT * FROM vinilo";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            try (ResultSet resultado = sentencia.executeQuery()) {

                while (resultado.next()) {
                    int id = resultado.getInt("id_vinilo");
                    int idAlbum = resultado.getInt("id_album");
                    String referencia = resultado.getString("referencia");
                    String color = resultado.getString("color");
                    BigDecimal precio = resultado.getBigDecimal("precio");

                    Vinilo vinilo = new Vinilo(id, idAlbum, referencia, color, precio);

                    vinilos.add(vinilo);
                }
            }
        }
        return vinilos;
    }

    public void actualizar(Vinilo vinilo) throws SQLException {

        String sql = "UPDATE vinilo SET id_album = ?, referencia = ?, color = ?, precio = ? WHERE id_vinilo = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, vinilo.getIdAlbum());
            sentencia.setString(2, vinilo.getReferencia());
            sentencia.setString(3, vinilo.getColor());
            sentencia.setBigDecimal(4, vinilo.getPrecio());
            sentencia.setInt(5, vinilo.getIdVinilo());

            sentencia.executeUpdate();
        }
    }

    public void eliminar(int idVinilo) throws SQLException {

        String sql = "DELETE FROM vinilo WHERE id_vinilo = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, idVinilo);
            sentencia.executeUpdate();
        }
    }
}
