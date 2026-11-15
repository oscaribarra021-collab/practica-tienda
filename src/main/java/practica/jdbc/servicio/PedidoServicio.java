package practica.jdbc.servicio;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import practica.jdbc.config.ConexionBD;
import practica.jdbc.dao.ClienteDAO;
import practica.jdbc.dao.InventarioDAO;
import practica.jdbc.dao.LineaPedidoDAO;
import practica.jdbc.dao.MovimientoStockDAO;
import practica.jdbc.dao.PagoDAO;
import practica.jdbc.dao.PedidoDAO;
import practica.jdbc.dao.ViniloDAO;
import practica.jdbc.datos.LineaPedido;
import practica.jdbc.datos.MovimientoStock;
import practica.jdbc.datos.Pago;
import practica.jdbc.datos.Pedido;
import practica.jdbc.datos.Vinilo;
import practica.jdbc.excepciones.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class PedidoServicio {

    private static final Logger log = LogManager.getLogger(PedidoServicio.class);

    private final ConexionBD conexionBD;

    public PedidoServicio(ConexionBD conexionBD) {
        this.conexionBD = conexionBD;
    }

    public int crearPedido(Pedido pedido, List<LineaPedido> lineas, Pago pago) throws SQLException {
        return procesarPedido(pedido, lineas, pago, false);
    }

    public int crearPedidoConFallo(Pedido pedido, List<LineaPedido> lineas, Pago pago) throws SQLException {
        return procesarPedido(pedido, lineas, pago, true);
    }

    private int procesarPedido(Pedido pedido, List<LineaPedido> lineas, Pago pago, boolean provocarFallo) throws SQLException {

        if (pedido == null || pago == null || lineas == null || lineas.isEmpty()) {
            throw new DatosInvalidosException("Los datos del pedido no son válidos");
        }

        log.info("Inicio pedido cliente={}", pedido.getIdCliente());

        try (Connection conexion = conexionBD.obtenerConexion()) {

            conexion.setAutoCommit(false);
            log.debug("Inicio de transacción de pedido");

            try {
                PedidoDAO pedidoDAO = new PedidoDAO(conexion);
                LineaPedidoDAO lineaPedidoDAO = new LineaPedidoDAO(conexion);
                InventarioDAO inventarioDAO = new InventarioDAO(conexion);
                MovimientoStockDAO movimientoStockDAO = new MovimientoStockDAO(conexion);
                PagoDAO pagoDAO = new PagoDAO(conexion);
                ClienteDAO clienteDAO = new ClienteDAO(conexion);
                ViniloDAO viniloDAO = new ViniloDAO(conexion);

                if (clienteDAO.buscarPorId(pedido.getIdCliente()) == null) {
                    throw new DatosInvalidosException("El cliente no existe: " + pedido.getIdCliente());
                }

                BigDecimal total = BigDecimal.ZERO;

                for (LineaPedido linea : lineas) {

                    if (linea.getCantidad() <= 0) {
                        throw new DatosInvalidosException("La cantidad debe ser mayor que cero");
                    }

                    Vinilo vinilo = viniloDAO.buscarPorId(linea.getIdVinilo());

                    if (vinilo == null) {
                        throw new DatosInvalidosException("El vinilo no existe: " + linea.getIdVinilo());
                    }

                    if (!viniloDAO.estaActivo(vinilo.getIdVinilo())) {
                        throw new DatosInvalidosException("El vinilo no está disponible: " + vinilo.getReferencia());
                    }

                    total = total.add(vinilo.getPrecio().multiply(BigDecimal.valueOf(linea.getCantidad())));
                }

                pedido.setTotal(total);
                pedido.setEstado("PENDIENTE");

                int idPedido = pedidoDAO.insertar(pedido);
                pedido.setIdPedido(idPedido);
                log.debug("Pedido insertado id={}", idPedido);

                int numeroLineas = 0;

                for (LineaPedido linea : lineas) {

                    Vinilo vinilo = viniloDAO.buscarPorId(linea.getIdVinilo());
                    int idVinilo = vinilo.getIdVinilo();
                    int cantidad = linea.getCantidad();

                    if (inventarioDAO.exigeControlStock(idVinilo)) {

                        if (!inventarioDAO.reducirStock(idVinilo, cantidad)) {
                            throw new StockInsuficienteException("Stock insuficiente para el vinilo " + vinilo.getReferencia());
                        }

                        MovimientoStock movimiento = new MovimientoStock(0, idVinilo, "SALIDA", cantidad,
                                LocalDateTime.now(), "VENTA pedido " + idPedido);
                        movimientoStockDAO.insertar(movimiento);
                    }

                    lineaPedidoDAO.insertar(new LineaPedido(0, idPedido, idVinilo, cantidad, vinilo.getPrecio()));

                    numeroLineas++;

                    if (provocarFallo && numeroLineas == 1) {
                        throw new FalloTransaccionException("Fallo controlado tras modificar la primera línea del pedido " + idPedido);
                    }
                }

                Pago nuevoPago = new Pago(0, idPedido, pago.getMetodo(), LocalDateTime.now(), total, pago.getEstado());
                pagoDAO.insertar(nuevoPago);

                if ("COMPLETADO".equals(pago.getEstado())) {
                    pedidoDAO.actualizarEstado(idPedido, "PAGADO");
                }

                conexion.commit();
                log.info("Commit pedido={} total={}", idPedido, total);

                return idPedido;

            } catch (SQLException | RuntimeException e) {
                revertir(conexion, e);
                throw e;
            }
        }
    }

    public void pagarPedido(int idPedido) throws SQLException {

        log.info("Inicio pago pedido={}", idPedido);

        try (Connection conexion = conexionBD.obtenerConexion()) {

            conexion.setAutoCommit(false);

            try {
                PedidoDAO pedidoDAO = new PedidoDAO(conexion);
                PagoDAO pagoDAO = new PagoDAO(conexion);

                Pedido pedido = pedidoDAO.buscarPorId(idPedido);

                if (pedido == null) {
                    throw new DatosInvalidosException("El pedido no existe: " + idPedido);
                }

                if (!"PENDIENTE".equals(pedido.getEstado())) {
                    throw new DatosInvalidosException("Solo se pueden pagar pedidos pendientes. Estado actual: " + pedido.getEstado());
                }

                if (pagoDAO.buscarPorPedido(idPedido) == null) {
                    throw new DatosInvalidosException("El pedido no tiene pago registrado: " + idPedido);
                }

                pagoDAO.actualizarEstado(idPedido, "COMPLETADO");
                pedidoDAO.actualizarEstado(idPedido, "PAGADO");

                conexion.commit();
                log.info("Pago completado pedido={}", idPedido);

            } catch (SQLException | RuntimeException e) {
                revertir(conexion, e);
                throw e;
            }
        }
    }

    public void cancelarPedido(int idPedido) throws SQLException {

        log.info("Inicio cancelación pedido={}", idPedido);

        try (Connection conexion = conexionBD.obtenerConexion()) {

            conexion.setAutoCommit(false);

            try {
                PedidoDAO pedidoDAO = new PedidoDAO(conexion);
                LineaPedidoDAO lineaPedidoDAO = new LineaPedidoDAO(conexion);
                InventarioDAO inventarioDAO = new InventarioDAO(conexion);
                MovimientoStockDAO movimientoStockDAO = new MovimientoStockDAO(conexion);
                PagoDAO pagoDAO = new PagoDAO(conexion);

                Pedido pedido = pedidoDAO.buscarPorId(idPedido);

                if (pedido == null) {
                    throw new DatosInvalidosException("El pedido no existe: " + idPedido);
                }

                if ("CANCELADO".equals(pedido.getEstado())) {
                    throw new PedidoCanceladoException("El pedido " + idPedido + " ya está cancelado");
                }

                for (LineaPedido linea : lineaPedidoDAO.obtenerPorPedido(idPedido)) {

                    int idVinilo = linea.getIdVinilo();
                    int cantidad = linea.getCantidad();

                    if (inventarioDAO.exigeControlStock(idVinilo)) {

                        inventarioDAO.aumentarStock(idVinilo, cantidad);

                        MovimientoStock movimiento = new MovimientoStock(0, idVinilo, "ENTRADA", cantidad,
                                LocalDateTime.now(), "CANCELACION pedido " + idPedido);
                        movimientoStockDAO.insertar(movimiento);
                    }
                }

                pedidoDAO.actualizarEstado(idPedido, "CANCELADO");

                Pago pago = pagoDAO.buscarPorPedido(idPedido);

                if (pago != null) {
                    if ("COMPLETADO".equals(pago.getEstado())) {
                        pagoDAO.actualizarEstado(idPedido, "REEMBOLSADO");
                    } else if ("PENDIENTE".equals(pago.getEstado())) {
                        pagoDAO.actualizarEstado(idPedido, "RECHAZADO");
                    }
                }

                conexion.commit();
                log.info("Commit cancelación pedido={}", idPedido);

            } catch (SQLException | RuntimeException e) {
                revertir(conexion, e);
                throw e;
            }
        }
    }

    private void revertir(Connection conexion, Exception e) throws SQLException {

        if (e instanceof FalloTransaccionException) {
            log.warn("Fallo controlado; se revierte la transacción: {}", e.getMessage());
        } else if (e instanceof NegocioException) {
            log.warn("Regla de negocio incumplida; se revierte la transacción: {}", e.getMessage());
        } else {
            log.error("Error técnico; se revierte la transacción: {}", e.getMessage());
        }

        conexion.rollback();
        log.info("Rollback completado");
    }
}