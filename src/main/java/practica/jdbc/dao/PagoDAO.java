package practica.jdbc.dao;

import practica.jdbc.datos.Pago;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PagoDAO {

    private final Connection conexion;

    public PagoDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public void insertar(Pago pago) throws SQLException {

        String sql = "INSERT INTO pago (id_pedido, metodo, fecha_pago, importe, estado) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, pago.getIdPedido());
            sentencia.setString(2, pago.getMetodo());

            if (pago.getFechaPago() != null) {
                sentencia.setTimestamp(3, java.sql.Timestamp.valueOf(pago.getFechaPago()));
            } else {
                sentencia.setNull(3, java.sql.Types.TIMESTAMP);
            }

            sentencia.setBigDecimal(4, pago.getImporte());
            sentencia.setString(5, pago.getEstado());
            sentencia.executeUpdate();
        }
    }

    public Pago buscarPorPedido(int idPedido) throws SQLException {

        String sql = "SELECT * FROM pago WHERE id_pedido = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idPedido);

            try (ResultSet resultado = sentencia.executeQuery()) {

                if (resultado.next()) {

                    int id = resultado.getInt("id_pago");
                    int idPedidoResultado = resultado.getInt("id_pedido");
                    String metodo = resultado.getString("metodo");

                    java.sql.Timestamp fecha = resultado.getTimestamp("fecha_pago");
                    java.time.LocalDateTime fechaPago = null;

                    if (fecha != null) {
                        fechaPago = fecha.toLocalDateTime();
                    }

                    java.math.BigDecimal importe = resultado.getBigDecimal("importe");
                    String estado = resultado.getString("estado");

                    return new Pago(id, idPedidoResultado, metodo, fechaPago, importe, estado);
                }
            }
        }
        return null;
    }

    public void actualizarEstado(int idPedido, String estado) throws SQLException {

        String sql = "UPDATE pago SET estado = ? WHERE id_pedido = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, estado);
            sentencia.setInt(2, idPedido);
            sentencia.executeUpdate();
        }
    }

    public List<Pago> listarTodos() throws SQLException {

        List<Pago> pagos = new ArrayList<>();

        String sql = "SELECT * FROM pago";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            try (ResultSet resultado = sentencia.executeQuery()) {

                while (resultado.next()) {

                    int id = resultado.getInt("id_pago");
                    int idPedido = resultado.getInt("id_pedido");
                    String metodo = resultado.getString("metodo");

                    java.sql.Timestamp fecha = resultado.getTimestamp("fecha_pago");
                    java.time.LocalDateTime fechaPago = null;

                    if (fecha != null) {
                        fechaPago = fecha.toLocalDateTime();
                    }

                    java.math.BigDecimal importe = resultado.getBigDecimal("importe");
                    String estado = resultado.getString("estado");

                    Pago pago = new Pago(id, idPedido, metodo, fechaPago, importe, estado);

                    pagos.add(pago);
                }
            }
        }
        return pagos;
    }
}
