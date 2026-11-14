package practica.jdbc.servicio;

import practica.jdbc.config.ConexionBD;
import practica.jdbc.datos.Pedido;
import practica.jdbc.datos.LineaPedido;
import practica.jdbc.datos.Pago;
import practica.jdbc.datos.Inventario;
import practica.jdbc.datos.MovimientoStock;
import practica.jdbc.dao.PedidoDAO;
import practica.jdbc.dao.LineaPedidoDAO;
import practica.jdbc.dao.InventarioDAO;
import practica.jdbc.dao.MovimientoStockDAO;
import practica.jdbc.dao.PagoDAO;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

public class PedidoServicio {

    private final ConexionBD conexionBD;

    public PedidoServicio(ConexionBD conexionBD) {
        this.conexionBD = conexionBD;
    }

    public int crearPedido(Pedido pedido, List<LineaPedido> lineas, Pago pago) throws SQLException {

        try (Connection conexion = conexionBD.obtenerConexion()) {

            conexion.setAutoCommit(false);

            try {

                PedidoDAO pedidoDAO = new PedidoDAO(conexion);
                LineaPedidoDAO lineaPedidoDAO = new LineaPedidoDAO(conexion);
                InventarioDAO inventarioDAO = new InventarioDAO(conexion);
                MovimientoStockDAO movimientoStockDAO = new MovimientoStockDAO(conexion);
                PagoDAO pagoDAO = new PagoDAO(conexion);

                BigDecimal total = BigDecimal.ZERO;

                for (LineaPedido linea : lineas) {

                    if (linea.getCantidad() <= 0) {
                        throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
                    }

                    if (linea.getPrecioUnitario().compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException("El precio debe ser mayor que cero");
                    }

                    total = total.add(linea.getPrecioUnitario().multiply(BigDecimal.valueOf(linea.getCantidad())));
                }

                pedido.setTotal(total);

                int idPedido = pedidoDAO.insertar(pedido);

                for (LineaPedido linea : lineas) {

                    int idVinilo = linea.getIdVinilo();
                    int cantidad = linea.getCantidad();

                    Inventario inventario = inventarioDAO.buscarPorVinilo(idVinilo);

                    if (inventario == null || !inventarioDAO.reducirStock(idVinilo, cantidad)) {
                        throw new IllegalStateException("Stock insuficiente para el vinilo: " + idVinilo);
                    }

                    LineaPedido nuevaLinea = new LineaPedido(0, idPedido, idVinilo, cantidad, linea.getPrecioUnitario());
                    lineaPedidoDAO.insertar(nuevaLinea);
                    MovimientoStock movimiento = new MovimientoStock(0, idVinilo, "SALIDA", cantidad, LocalDateTime.now(), "VENTA");
                    movimientoStockDAO.insertar(movimiento);
                }

                Pago nuevoPago = new Pago(0, idPedido, pago.getMetodo(), pago.getFechaPago(), total, pago.getEstado());
                pagoDAO.insertar(nuevoPago);
                conexion.commit();

                return idPedido;

            } catch (SQLException | RuntimeException e) {
                conexion.rollback();
                throw e;
            }
        }
    }

    public void cancelarPedido(int idPedido) throws SQLException {

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
                    throw new IllegalArgumentException("El pedido no existe");
                }

                if ("CANCELADO".equalsIgnoreCase(pedido.getEstado())) {
                    throw new IllegalStateException("El pedido ya está cancelado");
                }

                List<LineaPedido> lineas = lineaPedidoDAO.obtenerPorPedido(idPedido);

                for (LineaPedido linea : lineas) {

                    int idVinilo = linea.getIdVinilo();
                    int cantidad = linea.getCantidad();

                    if (inventarioDAO.buscarPorVinilo(idVinilo) == null) {
                        throw new IllegalStateException("No existe inventario para el vinilo: " + idVinilo);
                    }

                    inventarioDAO.aumentarStock(idVinilo, cantidad);
                    MovimientoStock movimiento = new MovimientoStock(0, idVinilo, "ENTRADA", cantidad, LocalDateTime.now(), "CANCELACION");
                    movimientoStockDAO.insertar(movimiento);
                }

                pedidoDAO.actualizarEstado(idPedido, "CANCELADO");
                pagoDAO.actualizarEstado(idPedido, "CANCELADO");

                conexion.commit();

            } catch (SQLException | RuntimeException e) {
                conexion.rollback();
                throw e;
            }
        }
    }
}