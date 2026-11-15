package practica.jdbc.app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import practica.jdbc.config.ConexionBD;
import practica.jdbc.config.ConfiguracionBD;
import practica.jdbc.config.InicializacionBD;
import practica.jdbc.datos.Cliente;
import practica.jdbc.datos.Direccion;
import practica.jdbc.datos.LineaPedido;
import practica.jdbc.datos.MovimientoStock;
import practica.jdbc.datos.Pago;
import practica.jdbc.datos.Pedido;
import practica.jdbc.datos.Vinilo;
import practica.jdbc.dto.CatalogoDTO;
import practica.jdbc.dto.DetallePedidoDTO;
import practica.jdbc.dto.EstadoBDDTO;
import practica.jdbc.dto.HistorialClienteDTO;
import practica.jdbc.dto.ResumenClienteDTO;
import practica.jdbc.dto.VentaDTO;
import practica.jdbc.excepciones.*;
import practica.jdbc.servicio.CargaInicialServicio;
import practica.jdbc.servicio.CatalogoServicio;
import practica.jdbc.servicio.ClienteServicio;
import practica.jdbc.servicio.ConsultaServicio;
import practica.jdbc.servicio.InventarioServicio;
import practica.jdbc.servicio.PedidoServicio;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final Logger log = LogManager.getLogger(Main.class);

    static void main(String[] args) {

        System.out.println("===============================================");
        System.out.println("          BIENVENIDO A VINYLSTATION");
        System.out.println("===============================================");

        // CONFIGURACIÓN DE APPLICATION.PROPERTIES
        ConfiguracionBD configuracionBD = new ConfiguracionBD();
        String modoSimulacion = configuracionBD.obtener("simulation.mode");

        System.out.println("Gestor: " + configuracionBD.obtener("database.engine")
                + " | Conexiones: " + configuracionBD.obtener("connection.mode")
                + " | Simulación: " + modoSimulacion);

        log.info("Configuracion - Gestor={}, conexiones={}, simulacion={}",
                configuracionBD.obtener("database.engine"),
                configuracionBD.obtener("connection.mode"),
                modoSimulacion);

        // CONECTA EL PROGRAMA CON LA BASE DE DATOS
        ConexionBD conexionBD = new ConexionBD(configuracionBD);

        try {

            // INICIALIZA LA ESTRUCTURA Y CARGA LOS DATOS INICIALES
            new InicializacionBD(conexionBD, configuracionBD).inicializar();
            System.out.println("\nEstructura de la base de datos creada correctamente.");

            new CargaInicialServicio(conexionBD).cargarDatosIniciales();
            System.out.println("Datos iniciales cargados correctamente.");
            log.info("Inicializacion - Carga inicial completada");

            // CREA LOS SERVICIOS DE LA APLICACIÓN
            InventarioServicio inventarioServicio = new InventarioServicio(conexionBD);
            PedidoServicio pedidoServicio = new PedidoServicio(conexionBD);
            ClienteServicio clienteServicio = new ClienteServicio(conexionBD);
            CatalogoServicio catalogoServicio = new CatalogoServicio(conexionBD);
            ConsultaServicio consultaServicio = new ConsultaServicio(conexionBD, configuracionBD);

            demostrarCatalogo(catalogoServicio, consultaServicio);
            demostrarClientes(clienteServicio);
            demostrarInventario(inventarioServicio, catalogoServicio, consultaServicio);

            if ("FALLO_TRANSACCION".equals(modoSimulacion)) {
                demostrarFalloTransaccion(pedidoServicio, clienteServicio, catalogoServicio, consultaServicio);
            } else {
                demostrarPedidos(pedidoServicio, clienteServicio, catalogoServicio, consultaServicio);
            }

            demostrarConsultasGestion(clienteServicio, consultaServicio);

            System.out.println("\n========== SIMULACIÓN FINALIZADA ==========");
            log.info("Simulacion finalizada");

        } catch (SQLException e) {

            System.out.println("Error durante la ejecución: " + e.getMessage());
            log.error("Error técnico de base de datos: SQLState={} codigo={} mensaje={}",
                    e.getSQLState(), e.getErrorCode(), e.getMessage());

        } catch (RuntimeException e) {

            System.out.println("Error inesperado: " + e.getMessage());
            log.error("Error inesperado", e);

        } finally {

            // CIERRA EL POOL DE CONEXIONES
            conexionBD.cerrarPool();
            log.info("Pool de conexiones cerrado");
        }
    }

    // ========== CATÁLOGO ==========
    private static void demostrarCatalogo(CatalogoServicio catalogoServicio, ConsultaServicio consultaServicio) throws SQLException {

        System.out.println("\n========== CATÁLOGO ==========");

        System.out.println("\n-- Página 1 (3 por página) --");
        mostrarCatalogo(consultaServicio.catalogo(null, null, null, false, 1, 3));

        System.out.println("\n-- Página 2 (3 por página) --");
        mostrarCatalogo(consultaServicio.catalogo(null, null, null, false, 2, 3));

        System.out.println("\n-- Género Rock --");
        mostrarCatalogo(consultaServicio.catalogo("Rock", null, null, false, 1, 10));

        System.out.println("\n-- Precio entre 22 y 26, solo con stock --");
        mostrarCatalogo(consultaServicio.catalogo(null, new BigDecimal("22"), new BigDecimal("26"), true, 1, 10));

        System.out.println("\n-- Registro y modificación de productos --");

        catalogoServicio.registrarVinilo("Thriller", "MJ002", "Rojo", new BigDecimal("39.99"), 5);
        System.out.println("Vinilo MJ002 (edición roja de Thriller) registrado con 5 unidades.");

        catalogoServicio.cambiarPrecio("MJ002", new BigDecimal("34.99"));
        System.out.println("Precio de MJ002 modificado a 34.99.");

        System.out.println("\n-- Clasificación N:M álbum-artista --");

        catalogoServicio.asociarArtista("Get Rich or Die Tryin'", "Eminem");
        System.out.println("Artistas de Get Rich or Die Tryin': " + catalogoServicio.artistasDeAlbum("Get Rich or Die Tryin'"));

        System.out.println("\n-- Vinilos de Eminem (consulta de la temática) --");
        mostrarCatalogo(consultaServicio.vinilosPorArtista("Eminem"));
    }

    // ========== CLIENTES E INTEGRIDAD ==========
    private static void demostrarClientes(ClienteServicio clienteServicio) throws SQLException {

        System.out.println("\n========== CLIENTES ==========");

        Cliente cliente = new Cliente(0, "Lucía", "Gómez Vega", "lucia@gmail.com", "667890123");
        Direccion direccion = new Direccion(0, 0, "Calle Serrano", "7", "28001", "Madrid", "Madrid", "España");

        int idCliente = clienteServicio.registrarCliente(cliente, direccion);
        System.out.println("Cliente registrado con su dirección. ID generado: " + idCliente);

        // VIOLACIÓN DE INTEGRIDAD REAL: EMAIL DUPLICADO (UNIQUE)
        System.out.println("\n-- Violación de integridad (email duplicado) --");

        try {
            Cliente duplicado = new Cliente(0, "Otra", "Persona", "lucia@gmail.com", "600000000");
            Direccion otra = new Direccion(0, 0, "Calle Falsa", "1", "28001", "Madrid", "Madrid", "España");

            clienteServicio.registrarCliente(duplicado, otra);

        } catch (SQLException e) {
            System.out.println("Error TÉCNICO de base de datos:");
            System.out.println("  Mensaje : " + e.getMessage());
            System.out.println("  SQLState: " + e.getSQLState());
            System.out.println("  Código  : " + e.getErrorCode());
        }

        // CONDICIÓN DE NEGOCIO PREVISTA
        System.out.println("\n-- Condición de negocio (cliente sin nombre) --");

        try {
            Cliente sinNombre = new Cliente(0, "", "Sin Nombre", "sin@gmail.com", null);
            Direccion otra = new Direccion(0, 0, "Calle Falsa", "2", "28001", "Madrid", "Madrid", "España");

            clienteServicio.registrarCliente(sinNombre, otra);

        } catch (DatosInvalidosException e) {
            System.out.println("Error de NEGOCIO: " + e.getMessage());
        }
    }

    // ========== INVENTARIO ==========
    private static void demostrarInventario(InventarioServicio inventarioServicio, CatalogoServicio catalogoServicio,
                                            ConsultaServicio consultaServicio) throws SQLException {

        System.out.println("\n========== INVENTARIO ==========");

        Vinilo electronica = catalogoServicio.buscarVinilo("AV001");

        System.out.println("\n-- Stock bajo antes de reponer --");
        mostrarCatalogo(consultaServicio.stockBajo());

        inventarioServicio.registrarEntrada(electronica.getIdVinilo(), 5, "REPOSICION");
        System.out.println("\nEntrada de 5 unidades de AV001 registrada.");

        inventarioServicio.registrarSalida(electronica.getIdVinilo(), 1, "AJUSTE DE STOCK");
        System.out.println("Salida de 1 unidad de AV001 registrada.");

        try {
            inventarioServicio.registrarSalida(electronica.getIdVinilo(), 500, "AJUSTE DE STOCK");
        } catch (StockInsuficienteException e) {
            System.out.println("Error de NEGOCIO: " + e.getMessage());
        }

        System.out.println("\n-- Movimientos de AV001 --");
        for (MovimientoStock m : consultaServicio.movimientos(electronica.getIdVinilo())) {
            System.out.println("  " + m.getFechaMovimiento() + " | " + m.getTipo() + " | " + m.getCantidad() + " | " + m.getMotivo());
        }

        System.out.println("\n-- Stock bajo después de reponer --");
        mostrarCatalogo(consultaServicio.stockBajo());
    }

    // ========== PEDIDOS (SIMULACIÓN NORMAL) ==========
    private static void demostrarPedidos(PedidoServicio pedidoServicio, ClienteServicio clienteServicio,
                                         CatalogoServicio catalogoServicio, ConsultaServicio consultaServicio) throws SQLException {

        System.out.println("\n========== PEDIDOS ==========");

        Cliente cliente = clienteServicio.buscarPorEmail("alvaro@gmail.com");
        Direccion direccion = clienteServicio.primeraDireccion(cliente.getIdCliente());
        Vinilo rock = catalogoServicio.buscarVinilo("PF001");
        Vinilo metal = catalogoServicio.buscarVinilo("BS001");

        // PEDIDO CON DOS PRODUCTOS Y PAGO PENDIENTE
        Pedido pedido = new Pedido(0, cliente.getIdCliente(), direccion.getIdDireccion(),
                LocalDateTime.now(), "PENDIENTE", BigDecimal.ZERO);

        List<LineaPedido> lineas = new ArrayList<>();
        lineas.add(new LineaPedido(0, 0, rock.getIdVinilo(), 2, rock.getPrecio()));
        lineas.add(new LineaPedido(0, 0, metal.getIdVinilo(), 1, metal.getPrecio()));

        Pago pago = new Pago(0, 0, "TARJETA", LocalDateTime.now(), BigDecimal.ZERO, "PENDIENTE");

        int idPedido = pedidoServicio.crearPedido(pedido, lineas, pago);
        System.out.println("Pedido creado. ID: " + idPedido);
        mostrarDetallePedido(consultaServicio.detallePedido(idPedido));

        // PAGO SIMULADO
        pedidoServicio.pagarPedido(idPedido);
        System.out.println("\nPago simulado completado.");
        mostrarDetallePedido(consultaServicio.detallePedido(idPedido));

        // PEDIDOS QUE NO SE PUEDEN ACEPTAR
        System.out.println("\n-- Pedidos rechazados por reglas de negocio --");

        catalogoServicio.cambiarDisponibilidad("FC001", false);
        intentarPedido(pedidoServicio, cliente, direccion, catalogoServicio.buscarVinilo("FC001"), 1);
        intentarPedido(pedidoServicio, cliente, direccion, catalogoServicio.buscarVinilo("EM001"), 99);
        intentarPedido(pedidoServicio, cliente, direccion, catalogoServicio.buscarVinilo("EM001"), 0);

        // PREVENTA: MJ001 NO CONTROLA STOCK
        System.out.println("\n-- Preventa (producto sin control de stock) --");
        intentarPedido(pedidoServicio, cliente, direccion, catalogoServicio.buscarVinilo("MJ001"), 1);

        // CANCELACIÓN Y SEGUNDA CANCELACIÓN
        System.out.println("\n-- Cancelación --");

        pedidoServicio.cancelarPedido(idPedido);
        System.out.println("Pedido " + idPedido + " cancelado: stock restituido y pago reembolsado.");
        mostrarDetallePedido(consultaServicio.detallePedido(idPedido));

        try {
            pedidoServicio.cancelarPedido(idPedido);
        } catch (PedidoCanceladoException e) {
            System.out.println("Segunda cancelación rechazada: " + e.getMessage());
        }

        System.out.println("\n-- Historial de " + cliente.getNombre() + " --");
        mostrarHistorial(consultaServicio.historialCliente(cliente.getIdCliente()));
    }

    private static void intentarPedido(PedidoServicio pedidoServicio, Cliente cliente, Direccion direccion,
                                       Vinilo vinilo, int cantidad) throws SQLException {

        Pedido pedido = new Pedido(0, cliente.getIdCliente(), direccion.getIdDireccion(),
                LocalDateTime.now(), "PENDIENTE", BigDecimal.ZERO);

        List<LineaPedido> lineas = new ArrayList<>();
        lineas.add(new LineaPedido(0, 0, vinilo.getIdVinilo(), cantidad, vinilo.getPrecio()));

        Pago pago = new Pago(0, 0, "PAYPAL", LocalDateTime.now(), BigDecimal.ZERO, "PENDIENTE");

        try {
            int id = pedidoServicio.crearPedido(pedido, lineas, pago);
            System.out.println("Pedido aceptado (" + vinilo.getReferencia() + " x" + cantidad + "). ID: " + id);

        } catch (NegocioException e) {
            System.out.println("Pedido rechazado (" + vinilo.getReferencia() + " x" + cantidad + "): " + e.getMessage());
        }
    }

    // ========== FALLO DE TRANSACCIÓN ==========
    private static void demostrarFalloTransaccion(PedidoServicio pedidoServicio, ClienteServicio clienteServicio,
                                                  CatalogoServicio catalogoServicio, ConsultaServicio consultaServicio) throws SQLException {

        System.out.println("\n========== FALLO DE TRANSACCIÓN ==========");

        Cliente cliente = clienteServicio.buscarPorEmail("alvaro@gmail.com");
        Direccion direccion = clienteServicio.primeraDireccion(cliente.getIdCliente());
        Vinilo rock = catalogoServicio.buscarVinilo("PF001");
        Vinilo metal = catalogoServicio.buscarVinilo("BS001");

        EstadoBDDTO antes = consultaServicio.estadoBD(rock.getIdVinilo());
        System.out.println("Estado antes   : " + antes);

        Pedido pedido = new Pedido(0, cliente.getIdCliente(), direccion.getIdDireccion(),
                LocalDateTime.now(), "PENDIENTE", BigDecimal.ZERO);

        List<LineaPedido> lineas = new ArrayList<>();
        lineas.add(new LineaPedido(0, 0, rock.getIdVinilo(), 1, rock.getPrecio()));
        lineas.add(new LineaPedido(0, 0, metal.getIdVinilo(), 1, metal.getPrecio()));

        Pago pago = new Pago(0, 0, "TARJETA", LocalDateTime.now(), BigDecimal.ZERO, "COMPLETADO");

        try {
            pedidoServicio.crearPedidoConFallo(pedido, lineas, pago);
        } catch (FalloTransaccionException e) {
            System.out.println("Fallo controlado: " + e.getMessage());
            System.out.println("Se ha ejecutado el rollback.");
        }

        // COMPRUEBA CON SQL REAL QUE NO HA QUEDADO NADA
        EstadoBDDTO despues = consultaServicio.estadoBD(rock.getIdVinilo());
        System.out.println("Estado después : " + despues);

        if (antes.equals(despues)) {
            System.out.println("ROLLBACK COMPROBADO: sin pedido, líneas, movimientos ni pago, y el stock no ha cambiado.");
        } else {
            System.out.println("ERROR: EL ROLLBACK HA DEJADO CAMBIOS PARCIALES.");
        }
    }

    // ========== CONSULTAS DE GESTIÓN ==========
    private static void demostrarConsultasGestion(ClienteServicio clienteServicio, ConsultaServicio consultaServicio) throws SQLException {

        System.out.println("\n========== CONSULTAS DE GESTIÓN ==========");

        System.out.println("\n-- Productos más vendidos --");

        List<VentaDTO> ventas = consultaServicio.masVendidos(3);

        if (ventas.isEmpty()) {
            System.out.println("  (sin ventas)");
        }

        for (VentaDTO v : ventas) {
            System.out.println("  " + v.referencia() + " | " + v.album() + " | " + v.unidadesVendidas() + " uds | " + v.ingresos() + " €");
        }

        System.out.println("\n-- Procedimiento almacenado resumen_cliente (solo MySQL) --");

        Cliente cliente = clienteServicio.buscarPorEmail("alvaro@gmail.com");
        ResumenClienteDTO resumen = consultaServicio.resumenCliente(cliente.getIdCliente());

        if (resumen == null) {
            System.out.println("  Procedimiento específico de MySQL: no disponible con H2.");
        } else {
            System.out.println("  " + cliente.getNombre() + ": " + resumen.numPedidos() + " pedidos no cancelados, "
                    + resumen.totalGastado() + " € gastados");
        }
    }

    // ========== PRESENTACIÓN ==========
    private static void mostrarCatalogo(List<CatalogoDTO> lista) {

        if (lista.isEmpty()) {
            System.out.println("  (sin resultados)");
            return;
        }

        for (CatalogoDTO v : lista) {
            System.out.println("  " + v.referencia() + " | " + v.album() + " | " + v.genero() + " | " + v.color()
                    + " | " + v.precio() + " € | stock " + v.stock());
        }
    }

    private static void mostrarDetallePedido(List<DetallePedidoDTO> detalle) {

        if (detalle.isEmpty()) {
            System.out.println("  (pedido sin detalle)");
            return;
        }

        DetallePedidoDTO primero = detalle.get(0);

        System.out.println("  Pedido " + primero.idPedido() + " | " + primero.cliente() + " | " + primero.fecha()
                + " | pedido: " + primero.estadoPedido() + " | pago: " + primero.estadoPago());

        BigDecimal total = BigDecimal.ZERO;

        for (DetallePedidoDTO linea : detalle) {
            System.out.println("    " + linea.referencia() + " | " + linea.album() + " | " + linea.cantidad()
                    + " x " + linea.precioUnitario() + " = " + linea.importe());
            total = total.add(linea.importe());
        }

        System.out.println("    TOTAL: " + total + " €");
    }

    private static void mostrarHistorial(List<HistorialClienteDTO> historial) {

        if (historial.isEmpty()) {
            System.out.println("  (sin pedidos)");
            return;
        }

        for (HistorialClienteDTO h : historial) {
            System.out.println("  Pedido " + h.idPedido() + " | " + h.fecha() + " | " + h.estado()
                    + " | " + h.numLineas() + " líneas | " + h.unidades() + " uds | " + h.total() + " €");
        }
    }
}