package practica.jdbc.servicio;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import practica.jdbc.config.ConexionBD;
import practica.jdbc.dao.InventarioDAO;
import practica.jdbc.dao.MovimientoStockDAO;
import practica.jdbc.datos.Inventario;
import practica.jdbc.datos.MovimientoStock;
import practica.jdbc.excepciones.DatosInvalidosException;
import practica.jdbc.excepciones.NegocioException;
import practica.jdbc.excepciones.StockInsuficienteException;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class InventarioServicio {

    private static final Logger log = LogManager.getLogger(InventarioServicio.class);

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
                    throw new DatosInvalidosException("No existe inventario para el vinilo: " + idVinilo);
                }

                inventarioDAO.aumentarStock(idVinilo, cantidad);

                MovimientoStock movimiento = new MovimientoStock(0, idVinilo, "ENTRADA", cantidad, LocalDateTime.now(), motivo);
                movimientoStockDAO.insertar(movimiento);

                conexion.commit();
                log.info("Entrada de stock vinilo={} cantidad={} motivo={}", idVinilo, cantidad, motivo);

            } catch (SQLException | RuntimeException e) {
                revertir(conexion, e);
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

                if (inventario == null) {
                    throw new DatosInvalidosException("No existe inventario para el vinilo: " + idVinilo);
                }

                if (!inventarioDAO.reducirStock(idVinilo, cantidad)) {
                    throw new StockInsuficienteException("Stock insuficiente para el vinilo " + idVinilo
                            + ": hay " + inventario.getStockActual() + " y se piden " + cantidad);
                }

                MovimientoStock movimiento = new MovimientoStock(0, idVinilo, "SALIDA", cantidad, LocalDateTime.now(), motivo);
                movimientoStockDAO.insertar(movimiento);

                conexion.commit();
                log.info("Salida de stock vinilo={} cantidad={} motivo={}", idVinilo, cantidad, motivo);

            } catch (SQLException | RuntimeException e) {
                revertir(conexion, e);
                throw e;
            }
        }
    }

    private void validarMovimiento(int idVinilo, int cantidad, String motivo) {

        if (idVinilo <= 0) {
            throw new DatosInvalidosException("El identificador del vinilo no es válido");
        }

        if (cantidad <= 0) {
            throw new DatosInvalidosException("La cantidad debe ser mayor que cero");
        }

        if (motivo == null || motivo.isBlank()) {
            throw new DatosInvalidosException("El motivo del movimiento es obligatorio");
        }
    }

    private void revertir(Connection conexion, Exception e) throws SQLException {

        if (e instanceof NegocioException) {
            log.warn("Regla de negocio incumplida; se revierte la transacción: {}", e.getMessage());
        } else {
            log.error("Error técnico; se revierte la transacción: {}", e.getMessage());
        }

        conexion.rollback();
        log.info("Rollback completado");
    }
}