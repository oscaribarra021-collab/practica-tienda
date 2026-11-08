
package practica.jdbc.dao;

import practica.jdbc.datos.Direccion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DireccionDAO {

    private final Connection conexion;

    public DireccionDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public void insertar(Direccion direccion) throws SQLException {

        String sql = "INSERT INTO direccion (id_cliente, calle, numero, codigo_postal, ciudad, provincia, pais) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, direccion.getIdCliente());
            sentencia.setString(2, direccion.getCalle());
            sentencia.setString(3, direccion.getNumero());
            sentencia.setString(4, direccion.getCodigoPostal());
            sentencia.setString(5, direccion.getCiudad());
            sentencia.setString(6, direccion.getProvincia());
            sentencia.setString(7, direccion.getPais());
            sentencia.executeUpdate();
        }
    }

    public Direccion buscarPorId(int idDireccion) throws SQLException {

        String sql = "SELECT * FROM direccion WHERE id_direccion = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idDireccion);

            try (ResultSet resultado = sentencia.executeQuery()) {

                if (resultado.next()) {

                    int id = resultado.getInt("id_direccion");
                    int idCliente = resultado.getInt("id_cliente");
                    String calle = resultado.getString("calle");
                    String numero = resultado.getString("numero");
                    String codigoPostal = resultado.getString("codigo_postal");
                    String ciudad = resultado.getString("ciudad");
                    String provincia = resultado.getString("provincia");
                    String pais = resultado.getString("pais");
                    return new Direccion(id, idCliente, calle, numero,
                            codigoPostal, ciudad, provincia, pais);
                }
            }
        }
        return null;
    }

    public List<Direccion> listarTodos() throws SQLException {

        List<Direccion> direcciones = new ArrayList<>();

        String sql = "SELECT * FROM direccion";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            try (ResultSet resultado = sentencia.executeQuery()) {

                while (resultado.next()) {
                    int id = resultado.getInt("id_direccion");
                    int idCliente = resultado.getInt("id_cliente");
                    String calle = resultado.getString("calle");
                    String numero = resultado.getString("numero");
                    String codigoPostal = resultado.getString("codigo_postal");
                    String ciudad = resultado.getString("ciudad");
                    String provincia = resultado.getString("provincia");
                    String pais = resultado.getString("pais");

                    Direccion direccion = new Direccion(id, idCliente, calle, numero,
                            codigoPostal, ciudad, provincia, pais);

                    direcciones.add(direccion);
                }
            }
        }
        return direcciones;
    }

    public void actualizar(Direccion direccion) throws SQLException {

        String sql = "UPDATE direccion SET id_cliente = ?, calle = ?, numero = ?, codigo_postal = ?, ciudad = ?, provincia = ?, pais = ? WHERE id_direccion = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, direccion.getIdCliente());
            sentencia.setString(2, direccion.getCalle());
            sentencia.setString(3, direccion.getNumero());
            sentencia.setString(4, direccion.getCodigoPostal());
            sentencia.setString(5, direccion.getCiudad());
            sentencia.setString(6, direccion.getProvincia());
            sentencia.setString(7, direccion.getPais());
            sentencia.setInt(8, direccion.getIdDireccion());

            sentencia.executeUpdate();
        }
    }

    public void eliminar(int idDireccion) throws SQLException {

        String sql = "DELETE FROM direccion WHERE id_direccion = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, idDireccion);
            sentencia.executeUpdate();
        }
    }
}

