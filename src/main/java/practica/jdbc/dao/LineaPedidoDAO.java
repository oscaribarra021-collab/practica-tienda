package practica.jdbc.dao;

import practica.jdbc.datos.LineaPedido;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LineaPedidoDAO {

    private final Connection conexion;

    public LineaPedidoDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public void insertar(LineaPedido lineaPedido) throws SQLException {

        String sql = "INSERT INTO linea_pedido (id_pedido, id_vinilo, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, lineaPedido.getIdPedido());
            sentencia.setInt(2, lineaPedido.getIdVinilo());
            sentencia.setInt(3, lineaPedido.getCantidad());
            sentencia.setBigDecimal(4, lineaPedido.getPrecioUnitario());
            sentencia.executeUpdate();
        }
    }

    public List<LineaPedido> obtenerPorPedido(int idPedido) throws SQLException {

        List<LineaPedido> lineas = new ArrayList<>();

        String sql = "SELECT * FROM linea_pedido WHERE id_pedido = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idPedido);

            try (ResultSet resultado = sentencia.executeQuery()) {

                while (resultado.next()) {

                    int id = resultado.getInt("id_linea");
                    int idPedidoResultado = resultado.getInt("id_pedido");
                    int idVinilo = resultado.getInt("id_vinilo");
                    int cantidad = resultado.getInt("cantidad");
                    java.math.BigDecimal precioUnitario = resultado.getBigDecimal("precio_unitario");

                    LineaPedido lineaPedido = new LineaPedido(id, idPedidoResultado, idVinilo, cantidad, precioUnitario);

                    lineas.add(lineaPedido);
                }
            }
        }
        return lineas;
    }

    public List<LineaPedido> listarTodos() throws SQLException {

        List<LineaPedido> lineas = new ArrayList<>();

        String sql = "SELECT * FROM linea_pedido";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            try (ResultSet resultado = sentencia.executeQuery()) {

                while (resultado.next()) {

                    int id = resultado.getInt("id_linea");
                    int idPedido = resultado.getInt("id_pedido");
                    int idVinilo = resultado.getInt("id_vinilo");
                    int cantidad = resultado.getInt("cantidad");
                    java.math.BigDecimal precioUnitario = resultado.getBigDecimal("precio_unitario");

                    LineaPedido lineaPedido = new LineaPedido(id, idPedido, idVinilo, cantidad, precioUnitario);

                    lineas.add(lineaPedido);
                }
            }
        }
        return lineas;
    }
}

