import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import practica.jdbc.config.ConexionBD;
import practica.jdbc.config.ConfiguracionBD;
import practica.jdbc.config.InicializacionBD;
import practica.jdbc.datos.*;
import practica.jdbc.dto.EstadoBDDTO;
import practica.jdbc.dto.VentaDTO;
import practica.jdbc.excepciones.*;
import practica.jdbc.servicio.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TiendaTest {

    private ConexionBD conexionBD;
    private ClienteServicio clienteServicio;
    private CatalogoServicio catalogoServicio;
    private PedidoServicio pedidoServicio;
    private ConsultaServicio consultaServicio;

    @BeforeEach
    void preparar() throws SQLException {
        ConfiguracionBD config = new ConfiguracionBD();
        conexionBD = new ConexionBD(config);

        new InicializacionBD(conexionBD, config).inicializar();
        new CargaInicialServicio(conexionBD).cargarDatosIniciales();

        clienteServicio = new ClienteServicio(conexionBD);
        catalogoServicio = new CatalogoServicio(conexionBD);
        pedidoServicio = new PedidoServicio(conexionBD);
        consultaServicio = new ConsultaServicio(conexionBD, config);
    }

    @AfterEach
    void cerrar() {
        conexionBD.cerrarPool();
    }


    private int crearPedido(String referencia, int cantidad) throws SQLException {
        Cliente cliente = clienteServicio.buscarPorEmail("alvaro@gmail.com");
        Direccion direccion = clienteServicio.primeraDireccion(cliente.getIdCliente());
        Vinilo vinilo = catalogoServicio.buscarVinilo(referencia);

        Pedido pedido = new Pedido(0, cliente.getIdCliente(), direccion.getIdDireccion(),
                LocalDateTime.now(), "PENDIENTE", BigDecimal.ZERO);
        List<LineaPedido> lineas = List.of(
                new LineaPedido(0, 0, vinilo.getIdVinilo(), cantidad, vinilo.getPrecio()));
        Pago pago = new Pago(0, 0, "TARJETA", LocalDateTime.now(), BigDecimal.ZERO, "PENDIENTE");

        return pedidoServicio.crearPedido(pedido, lineas, pago);
    }

    private EstadoBDDTO estado(String referencia) throws SQLException {
        return consultaServicio.estadoBD(catalogoServicio.buscarVinilo(referencia).getIdVinilo());
    }


    @Test
    void insertaYRecuperaCliente() throws SQLException {
        Cliente cliente = new Cliente(0, "Lucía", "Gómez Vega", "lucia@gmail.com", "667890123");
        Direccion direccion = new Direccion(0, 0, "Calle Serrano", "7", "28001", "Madrid", "Madrid", "España");

        int id = clienteServicio.registrarCliente(cliente, direccion);

        Cliente guardado = clienteServicio.buscarPorEmail("lucia@gmail.com");
        assertNotNull(guardado);
        assertEquals(id, guardado.getIdCliente());
        assertEquals("Lucía", guardado.getNombre());
    }


    @Test
    void cambiarPrecioSeGuarda() throws SQLException {
        catalogoServicio.cambiarPrecio("PF001", new BigDecimal("30.00"));

        Vinilo vinilo = catalogoServicio.buscarVinilo("PF001");
        assertEquals(0, new BigDecimal("30.00").compareTo(vinilo.getPrecio()));
    }


    @Test
    void claveGeneradaRelacionaClienteYDireccion() throws SQLException {
        Cliente cliente = new Cliente(0, "Marta", "Ruiz", "marta@gmail.com", null);
        Direccion direccion = new Direccion(0, 0, "Calle Sol", "3", "28002", "Madrid", "Madrid", "España");

        int idCliente = clienteServicio.registrarCliente(cliente, direccion);

        Direccion guardada = clienteServicio.primeraDireccion(idCliente);
        assertNotNull(guardada);
        assertEquals(idCliente, guardada.getIdCliente());
    }


    @Test
    void masVendidosDevuelveDTOConValoresEsperados() throws SQLException {
        crearPedido("PF001", 2);
        BigDecimal precio = catalogoServicio.buscarVinilo("PF001").getPrecio();

        List<VentaDTO> ventas = consultaServicio.masVendidos(1);

        assertEquals(1, ventas.size());
        assertEquals("PF001", ventas.get(0).referencia());
        assertEquals(2, ventas.get(0).unidadesVendidas());
        assertEquals(0, precio.multiply(BigDecimal.valueOf(2)).compareTo(ventas.get(0).ingresos()));
    }


    @Test
    void pedidoCorrectoConfirmaTodosLosCambios() throws SQLException {
        EstadoBDDTO antes = estado("PF001");

        crearPedido("PF001", 1);

        EstadoBDDTO despues = estado("PF001");
        assertEquals(antes.pedidos() + 1, despues.pedidos());
        assertEquals(antes.lineas() + 1, despues.lineas());
        assertEquals(antes.movimientos() + 1, despues.movimientos());
        assertEquals(antes.pagos() + 1, despues.pagos());
        assertEquals(antes.stockVinilo() - 1, despues.stockVinilo());
    }


    @Test
    void falloControladoDeshaceTodoElPedido() throws SQLException {
        EstadoBDDTO antes = estado("PF001");

        Cliente cliente = clienteServicio.buscarPorEmail("alvaro@gmail.com");
        Direccion direccion = clienteServicio.primeraDireccion(cliente.getIdCliente());
        Vinilo vinilo = catalogoServicio.buscarVinilo("PF001");

        Pedido pedido = new Pedido(0, cliente.getIdCliente(), direccion.getIdDireccion(),
                LocalDateTime.now(), "PENDIENTE", BigDecimal.ZERO);
        List<LineaPedido> lineas = List.of(
                new LineaPedido(0, 0, vinilo.getIdVinilo(), 1, vinilo.getPrecio()));
        Pago pago = new Pago(0, 0, "TARJETA", LocalDateTime.now(), BigDecimal.ZERO, "COMPLETADO");

        assertThrows(FalloTransaccionException.class,
                () -> pedidoServicio.crearPedidoConFallo(pedido, lineas, pago));

        assertEquals(antes, estado("PF001"));
    }


    @Test
    void stockInsuficienteRechazaPedidoSinCambios() throws SQLException {
        EstadoBDDTO antes = estado("EM001");

        assertThrows(NegocioException.class, () -> crearPedido("EM001", 9999));

        assertEquals(antes, estado("EM001"));
    }


    @Test
    void cancelarDosVecesNoDuplicaLaDevolucion() throws SQLException {
        EstadoBDDTO inicial = estado("PF001");
        int idPedido = crearPedido("PF001", 1);

        pedidoServicio.cancelarPedido(idPedido);
        assertEquals(inicial.stockVinilo(), estado("PF001").stockVinilo());

        assertThrows(PedidoCanceladoException.class, () -> pedidoServicio.cancelarPedido(idPedido));
        assertEquals(inicial.stockVinilo(), estado("PF001").stockVinilo());
    }


    @Test
    void emailDuplicadoLanzaSQLExceptionDeIntegridad() throws SQLException {
        Direccion direccion = new Direccion(0, 0, "Calle A", "1", "28001", "Madrid", "Madrid", "España");
        clienteServicio.registrarCliente(new Cliente(0, "Uno", "Test", "dup@gmail.com", null), direccion);

        Direccion otra = new Direccion(0, 0, "Calle B", "2", "28001", "Madrid", "Madrid", "España");
        SQLException e = assertThrows(SQLException.class,
                () -> clienteServicio.registrarCliente(new Cliente(0, "Dos", "Test", "dup@gmail.com", null), otra));

        assertTrue(e.getSQLState().startsWith("23"));
    }
}