package practica.jdbc.dao;

import practica.jdbc.datos.Pedido;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO {

    private final Connection conexion;

    public PedidoDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public int insertar(Pedido pedido) throws SQLException {

        String sql = "INSERT INTO pedido (id_cliente, id_direccion, fecha_pedido, estado, total) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            sentencia.setInt(1, pedido.getIdCliente());
            sentencia.setInt(2, pedido.getIdDireccion());
            sentencia.setTimestamp(3, java.sql.Timestamp.valueOf(pedido.getFechaPedido()));
            sentencia.setString(4, pedido.getEstado());
            sentencia.setBigDecimal(5, pedido.getTotal());

            sentencia.executeUpdate();

            try (ResultSet resultado = sentencia.getGeneratedKeys()) {

                if (resultado.next()) {
                    return resultado.getInt(1);
                }
            }
        }
        throw new SQLException("No se ha podido obtener el ID del pedido");
    }

    public Pedido buscarPorId(int idPedido) throws SQLException {

        String sql = "SELECT * FROM pedido WHERE id_pedido = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idPedido);

            try (ResultSet resultado = sentencia.executeQuery()) {

                if (resultado.next()) {

                    int id = resultado.getInt("id_pedido");
                    int idCliente = resultado.getInt("id_cliente");
                    int idDireccion = resultado.getInt("id_direccion");

                    java.time.LocalDateTime fechaPedido = resultado.getTimestamp("fecha_pedido").toLocalDateTime();

                    String estado = resultado.getString("estado");
                    java.math.BigDecimal total = resultado.getBigDecimal("total");

                    return new Pedido(id, idCliente, idDireccion, fechaPedido, estado, total);
                }
            }
        }
        return null;
    }

    public List<Pedido> listarTodos() throws SQLException {

        List<Pedido> pedidos = new ArrayList<>();

        String sql = "SELECT * FROM pedido";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            try (ResultSet resultado = sentencia.executeQuery()) {

                while (resultado.next()) {

                    int id = resultado.getInt("id_pedido");
                    int idCliente = resultado.getInt("id_cliente");
                    int idDireccion = resultado.getInt("id_direccion");

                    java.time.LocalDateTime fechaPedido = resultado.getTimestamp("fecha_pedido").toLocalDateTime();

                    String estado = resultado.getString("estado");
                    java.math.BigDecimal total = resultado.getBigDecimal("total");

                    Pedido pedido = new Pedido(id, idCliente, idDireccion, fechaPedido, estado, total);

                    pedidos.add(pedido);
                }
            }
        }
        return pedidos;
    }

    public void actualizarEstado(int idPedido, String estado) throws SQLException {

        String sql = "UPDATE pedido SET estado = ? WHERE id_pedido = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, estado);
            sentencia.setInt(2, idPedido);
            sentencia.executeUpdate();
        }
    }
}