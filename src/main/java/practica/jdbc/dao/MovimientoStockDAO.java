package practica.jdbc.dao;

import practica.jdbc.datos.MovimientoStock;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MovimientoStockDAO {

    private final Connection conexion;

    public MovimientoStockDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public void insertar(MovimientoStock movimientoStock) throws SQLException {

        String sql = "INSERT INTO movimiento_stock (id_vinilo, tipo, cantidad, fecha_movimiento, motivo) " +
                    "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, movimientoStock.getIdVinilo());
            sentencia.setString(2, movimientoStock.getTipo());
            sentencia.setInt(3, movimientoStock.getCantidad());
            sentencia.setTimestamp(4, java.sql.Timestamp.valueOf(movimientoStock.getFechaMovimiento()));
            sentencia.setString(5, movimientoStock.getMotivo());
            sentencia.executeUpdate();
        }
    }

    public List<MovimientoStock> obtenerPorVinilo(int idVinilo) throws SQLException {

        List<MovimientoStock> movimientos = new ArrayList<>();

        String sql = "SELECT * FROM movimiento_stock WHERE id_vinilo = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idVinilo);

            try (ResultSet resultado = sentencia.executeQuery()) {

                while (resultado.next()) {

                    int id = resultado.getInt("id_movimiento");
                    int idViniloResultado = resultado.getInt("id_vinilo");
                    String tipo = resultado.getString("tipo");
                    int cantidad = resultado.getInt("cantidad");
                    java.time.LocalDateTime fechaMovimiento = resultado.getTimestamp("fecha_movimiento").toLocalDateTime();
                    String motivo = resultado.getString("motivo");

                    MovimientoStock movimientoStock = new MovimientoStock(id, idViniloResultado, tipo, cantidad, fechaMovimiento, motivo);

                    movimientos.add(movimientoStock);
                }
            }
        }
        return movimientos;
    }

    public List<MovimientoStock> listarTodos() throws SQLException {

        List<MovimientoStock> movimientos = new ArrayList<>();

        String sql = "SELECT * FROM movimiento_stock";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            try (ResultSet resultado = sentencia.executeQuery()) {

                while (resultado.next()) {

                    int id = resultado.getInt("id_movimiento");
                    int idVinilo = resultado.getInt("id_vinilo");
                    String tipo = resultado.getString("tipo");
                    int cantidad = resultado.getInt("cantidad");
                    java.time.LocalDateTime fechaMovimiento = resultado.getTimestamp("fecha_movimiento").toLocalDateTime();
                    String motivo = resultado.getString("motivo");

                    MovimientoStock movimientoStock = new MovimientoStock(id, idVinilo, tipo, cantidad, fechaMovimiento, motivo);

                    movimientos.add(movimientoStock);
                }
            }
        }
        return movimientos;
    }


}