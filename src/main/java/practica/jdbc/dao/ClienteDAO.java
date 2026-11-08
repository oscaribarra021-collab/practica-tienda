
package practica.jdbc.dao;

import practica.jdbc.datos.Cliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    private final Connection conexion;

    public ClienteDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public void insertar(Cliente cliente) throws SQLException {

        String sql = "INSERT INTO cliente (nombre, apellidos, email, telefono) VALUES (?, ?, ?, ?)";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, cliente.getNombre());
            sentencia.setString(2, cliente.getApellidos());
            sentencia.setString(3, cliente.getEmail());
            sentencia.setString(4, cliente.getTelefono());
            sentencia.executeUpdate();
        }
    }

    public Cliente buscarPorId(int idCliente) throws SQLException {

        String sql = "SELECT * FROM cliente WHERE id_cliente = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idCliente);

            try (ResultSet resultado = sentencia.executeQuery()) {

                if (resultado.next()) {

                    int id = resultado.getInt("id_cliente");
                    String nombre = resultado.getString("nombre");
                    String apellidos = resultado.getString("apellidos");
                    String email = resultado.getString("email");
                    String telefono = resultado.getString("telefono");
                    return new Cliente(id, nombre, apellidos, email, telefono);
                }
            }
        }
        return null;
    }

    public List<Cliente> listarTodos() throws SQLException {

        List<Cliente> clientes = new ArrayList<>();

        String sql = "SELECT * FROM cliente";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            try (ResultSet resultado = sentencia.executeQuery()) {

                while (resultado.next()) {
                    int id = resultado.getInt("id_cliente");
                    String nombre = resultado.getString("nombre");
                    String apellidos = resultado.getString("apellidos");
                    String email = resultado.getString("email");
                    String telefono = resultado.getString("telefono");

                    Cliente cliente = new Cliente(id, nombre, apellidos, email, telefono);

                    clientes.add(cliente);
                }
            }
        }
        return clientes;
    }

    public void actualizar(Cliente cliente) throws SQLException {

        String sql = "UPDATE cliente SET nombre = ?, apellidos = ?, email = ?, telefono = ? WHERE id_cliente = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, cliente.getNombre());
            sentencia.setString(2, cliente.getApellidos());
            sentencia.setString(3, cliente.getEmail());
            sentencia.setString(4, cliente.getTelefono());
            sentencia.setInt(5, cliente.getIdCliente());

            sentencia.executeUpdate();
        }
    }

    public void eliminar(int idCliente) throws SQLException {

        String sql = "DELETE FROM cliente WHERE id_cliente = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, idCliente);
            sentencia.executeUpdate();
        }
    }

    public int insertarConId(Cliente cliente) throws SQLException {

        String sql = "INSERT INTO cliente (nombre, apellidos, email, telefono) VALUES (?, ?, ?, ?)";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            sentencia.setString(1, cliente.getNombre());
            sentencia.setString(2, cliente.getApellidos());
            sentencia.setString(3, cliente.getEmail());
            sentencia.setString(4, cliente.getTelefono());
            sentencia.executeUpdate();

            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    return claves.getInt(1);
                }
            }
        }

        throw new SQLException("No se ha podido obtener el ID del cliente");
    }
}
