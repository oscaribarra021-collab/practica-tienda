
package practica.jdbc.app;

import practica.jdbc.config.ConfiguracionBD;
import practica.jdbc.config.ConexionBD;
import practica.jdbc.config.InicializacionBD;
import practica.jdbc.dao.ClienteDAO;
import practica.jdbc.dao.DireccionDAO;
import practica.jdbc.dao.InventarioDAO;
import practica.jdbc.dao.ViniloDAO;
import practica.jdbc.datos.Cliente;
import practica.jdbc.datos.Direccion;
import practica.jdbc.datos.Inventario;
import practica.jdbc.datos.LineaPedido;
import practica.jdbc.datos.Pago;
import practica.jdbc.datos.Pedido;
import practica.jdbc.datos.Vinilo;
import practica.jdbc.servicio.CargaInicialServicio;
import practica.jdbc.servicio.InventarioServicio;
import practica.jdbc.servicio.PedidoServicio;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Main {

    static void main(String[] args) {

        System.out.println("===============================================");
        System.out.println("          BIENVENIDO A VINYLSTATION");
        System.out.println("===============================================");

        // CONFIGURACIÓN DE APPLICATION.PROPERTIES
        ConfiguracionBD configuracionBD = new ConfiguracionBD();
        System.out.println("Base de datos seleccionada: " + configuracionBD.obtener("bd.tipo"));

        // CONECTA EL PROGRAMA CON LA BASE DE DATOS
        ConexionBD conexionBD = new ConexionBD(configuracionBD);

        try {

            // INICIALIZA LA ESTRUCTURA DE LA BASE DE DATOS
            InicializacionBD inicializacionBD = new InicializacionBD(conexionBD, configuracionBD);
            inicializacionBD.inicializar();

            System.out.println("\nEstructura de la base de datos creada correctamente.");

            // CARGA LOS DATOS INICIALES DE VINYLSTATION
            CargaInicialServicio cargaInicialServicio = new CargaInicialServicio(conexionBD);
            cargaInicialServicio.cargarDatosIniciales();

            System.out.println("Datos iniciales cargados correctamente.");

            // RECUPERA LOS CLIENTES, DIRECCIONES Y VINILOS
            Cliente cliente;
            Direccion direccion;
            Vinilo viniloRock;
            Vinilo viniloMetal;
            Vinilo viniloElectronica;

            try (Connection conexion = conexionBD.obtenerConexion()) {

                ClienteDAO clienteDAO = new ClienteDAO(conexion);
                DireccionDAO direccionDAO = new DireccionDAO(conexion);
                ViniloDAO viniloDAO = new ViniloDAO(conexion);

                cliente = clienteDAO.listarTodos().stream()
                        .filter(c -> c.getEmail().equals("alvaro@gmail.com"))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No se encuentra el cliente"));

                direccion = direccionDAO.listarTodos().stream()
                        .filter(d -> d.getIdCliente() == cliente.getIdCliente())
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No se encuentra la dirección"));

                List<Vinilo> vinilos = viniloDAO.listarTodos();

                viniloRock = vinilos.stream()
                        .filter(v -> v.getReferencia().equals("PF001"))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No se encuentra PF001"));

                viniloMetal = vinilos.stream()
                        .filter(v -> v.getReferencia().equals("BS001"))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No se encuentra BS001"));

                viniloElectronica = vinilos.stream()
                        .filter(v -> v.getReferencia().equals("AV001"))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No se encuentra AV001"));
            }

            // CREA LOS SERVICIOS DE LA APLICACIÓN
            InventarioServicio inventarioServicio = new InventarioServicio(conexionBD);
            PedidoServicio pedidoServicio = new PedidoServicio(conexionBD);

            // REGISTRA UNA ENTRADA DE INVENTARIO
            System.out.println("\n========== ENTRADA DE INVENTARIO ==========");

            inventarioServicio.registrarEntrada(viniloElectronica.getIdVinilo(), 2, "REPOSICION");
            System.out.println("Entrada de 2 unidades registrada correctamente.");

            // REGISTRA UNA SALIDA DE INVENTARIO
            System.out.println("\n========== SALIDA DE INVENTARIO ==========");

            inventarioServicio.registrarSalida(viniloElectronica.getIdVinilo(), 1, "AJUSTE DE STOCK");
            System.out.println("Salida de 1 unidad registrada correctamente.");

            // CREA UN PEDIDO CON DOS LINEAS
            System.out.println("\n========== CREACIÓN DE PEDIDO ==========");

            Pedido pedido = new Pedido(
                    0,
                    cliente.getIdCliente(),
                    direccion.getIdDireccion(),
                    LocalDateTime.now(),
                    "PENDIENTE",
                    BigDecimal.ZERO
            );

            List<LineaPedido> lineas = new ArrayList<>();

            lineas.add(new LineaPedido(0, 0, viniloRock.getIdVinilo(), 2, viniloRock.getPrecio()));
            lineas.add(new LineaPedido(0, 0, viniloMetal.getIdVinilo(), 1, viniloMetal.getPrecio()));

            Pago pago = new Pago(
                    0,
                    0,
                    "TARJETA",
                    LocalDateTime.now(),
                    BigDecimal.ZERO,
                    "COMPLETADO"
            );

            int idPedido = pedidoServicio.crearPedido(pedido, lineas, pago);

            System.out.println("Pedido creado correctamente. ID: " + idPedido);
            System.out.println("Total del pedido: " + pedido.getTotal() + " euros.");

            // CANCELA EL PEDIDO Y DEVUELVE EL STOCK
            System.out.println("\n========== CANCELACIÓN DE PEDIDO ==========");

            pedidoServicio.cancelarPedido(idPedido);

            System.out.println("Pedido cancelado correctamente.");
            System.out.println("El stock de los vinilos ha sido restaurado.");

            // PROVOCA UN FALLO PARA COMPROBAR EL ROLLBACK
            System.out.println("\n========== PRUEBA DE FALLO DE TRANSACCIÓN ==========");

            int stockAntesFallo;

            try (Connection conexion = conexionBD.obtenerConexion()) {
                InventarioDAO inventarioDAO = new InventarioDAO(conexion);
                Inventario inventario = inventarioDAO.buscarPorVinilo(viniloRock.getIdVinilo());
                stockAntesFallo = inventario.getStockActual();
            }

            Pedido pedidoFallo = new Pedido(
                    0,
                    cliente.getIdCliente(),
                    direccion.getIdDireccion(),
                    LocalDateTime.now(),
                    "PENDIENTE",
                    BigDecimal.ZERO
            );

            List<LineaPedido> lineasFallo = new ArrayList<>();
            lineasFallo.add(new LineaPedido(0, 0, viniloRock.getIdVinilo(), 1, viniloRock.getPrecio()));

            Pago pagoFallo = new Pago(
                    0,
                    0,
                    "TARJETA",
                    LocalDateTime.now(),
                    BigDecimal.ZERO,
                    "COMPLETADO"
            );

            try {
                pedidoServicio.crearPedidoConFallo(pedidoFallo, lineasFallo, pagoFallo);
            } catch (RuntimeException e) {
                if ("FALLO_TRANSACCION".equals(e.getMessage())) {
                    System.out.println("FALLO_TRANSACCION provocado correctamente.");
                    System.out.println("Se ha ejecutado el rollback.");
                } else {
                    throw e;
                }
            }

            int stockDespuesFallo;

            try (Connection conexion = conexionBD.obtenerConexion()) {
                InventarioDAO inventarioDAO = new InventarioDAO(conexion);
                Inventario inventario = inventarioDAO.buscarPorVinilo(viniloRock.getIdVinilo());
                stockDespuesFallo = inventario.getStockActual();
            }

            System.out.println("Stock antes del fallo: " + stockAntesFallo);
            System.out.println("Stock después del rollback: " + stockDespuesFallo);

            if (stockAntesFallo == stockDespuesFallo) {
                System.out.println("ROLLBACK COMPROBADO CORRECTAMENTE.");
            } else {
                System.out.println("ERROR: EL ROLLBACK NO HA RESTAURADO EL STOCK.");
            }

            System.out.println("\n========== SIMULACIÓN FINALIZADA ==========");

        } catch (SQLException e) {

            System.out.println("Error durante la ejecución: " + e.getMessage());

        } finally {

            // CIERRA EL POOL DE CONEXIONES
            conexionBD.cerrarPool();
        }
    }
}
