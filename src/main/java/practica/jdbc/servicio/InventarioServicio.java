
package practica.jdbc.servicio;

import practica.jdbc.config.ConexionBD;
import practica.jdbc.dao.InventarioDAO;
import practica.jdbc.dao.MovimientoStockDAO;
import practica.jdbc.datos.Inventario;
import practica.jdbc.datos.MovimientoStock;
import practica.jdbc.excepciones.StockInsuficienteException;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class InventarioServicio {

    private final ConexionBD conexionBD;

    public InventarioServicio(ConexionBD conexionBD) {
        this.conexionBD = conexionBD;
    }

    public void registrarEntrada(int idVinilo, int cantidad, String motivo) throws SQLException {

        validarMovimiento(idVinilo, cantidad, motivo);

        try (Connection conexion = conexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);

            try {
                InventarioDAO inventarioDAO = new InventarioDAO(conexion);
                MovimientoStockDAO movimientoStockDAO = new MovimientoStockDAO(conexion);

                Inventario inventario = inventarioDAO.buscarPorVinilo(idVinilo);

                if (inventario == null) {
                    throw new IllegalArgumentException("No existe inventario para el vinilo: " + idVinilo);
                }

                inventarioDAO.aumentarStock(idVinilo, cantidad);

                MovimientoStock movimiento = new MovimientoStock(0, idVinilo, "ENTRADA", cantidad, LocalDateTime.now(), motivo);
                movimientoStockDAO.insertar(movimiento);

                conexion.commit();

            } catch (SQLException | RuntimeException e) {
                conexion.rollback();
                throw e;
            }
        }
    }

    public void registrarSalida(int idVinilo, int cantidad, String motivo) throws SQLException {

        validarMovimiento(idVinilo, cantidad, motivo);

        try (Connection conexion = conexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);

            try {
                InventarioDAO inventarioDAO = new InventarioDAO(conexion);
                MovimientoStockDAO movimientoStockDAO = new MovimientoStockDAO(conexion);

                Inventario inventario = inventarioDAO.buscarPorVinilo(idVinilo);

                if (inventario == null || !inventarioDAO.reducirStock(idVinilo, cantidad)) {
                    throw new StockInsuficienteException("Stock insuficiente para el vinilo: " + idVinilo);
                }

                MovimientoStock movimiento = new MovimientoStock(0, idVinilo, "SALIDA", cantidad, LocalDateTime.now(), motivo);
                movimientoStockDAO.insertar(movimiento);

                conexion.commit();

            } catch (SQLException | RuntimeException e) {
                conexion.rollback();
                throw e;
            }
        }
    }

    private void validarMovimiento(int idVinilo, int cantidad, String motivo) {

        if (idVinilo <= 0) {
            throw new IllegalArgumentException("El identificador del vinilo no es válido");
        }

        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }

        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo del movimiento es obligatorio");
        }
    }
}
