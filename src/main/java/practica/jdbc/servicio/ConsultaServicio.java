package practica.jdbc.servicio;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import practica.jdbc.config.ConexionBD;
import practica.jdbc.config.ConfiguracionBD;
import practica.jdbc.dao.ConsultaDAO;
import practica.jdbc.dao.MovimientoStockDAO;
import practica.jdbc.datos.MovimientoStock;
import practica.jdbc.dto.CatalogoDTO;
import practica.jdbc.dto.DetallePedidoDTO;
import practica.jdbc.dto.EstadoBDDTO;
import practica.jdbc.dto.HistorialClienteDTO;
import practica.jdbc.dto.ResumenClienteDTO;
import practica.jdbc.dto.VentaDTO;
import practica.jdbc.excepciones.DatosInvalidosException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ConsultaServicio {

    private static final Logger log = LogManager.getLogger(ConsultaServicio.class);

    private final ConexionBD conexionBD;
    private final ConfiguracionBD configuracionBD;

    public ConsultaServicio(ConexionBD conexionBD, ConfiguracionBD configuracionBD) {
        this.conexionBD = conexionBD;
        this.configuracionBD = configuracionBD;
    }

    public List<CatalogoDTO> catalogo(String genero, BigDecimal precioMin, BigDecimal precioMax,
                                      boolean soloDisponibles, int pagina, int tamanoPagina) throws SQLException {

        if (pagina < 1 || tamanoPagina < 1) {
            throw new DatosInvalidosException("La página y el tamaño de página deben ser mayores que cero");
        }

        log.debug("Consulta de catálogo genero={} pagina={} tamano={}", genero, pagina, tamanoPagina);

        try (Connection conexion = conexionBD.obtenerConexion()) {
            return new ConsultaDAO(conexion).catalogoFiltrado(genero, precioMin, precioMax, soloDisponibles, pagina, tamanoPagina);
        }
    }

    public List<CatalogoDTO> stockBajo() throws SQLException {

        try (Connection conexion = conexionBD.obtenerConexion()) {
            return new ConsultaDAO(conexion).stockBajo();
        }
    }

    public List<CatalogoDTO> vinilosPorArtista(String nombreArtista) throws SQLException {

        try (Connection conexion = conexionBD.obtenerConexion()) {
            return new ConsultaDAO(conexion).vinilosPorArtista(nombreArtista);
        }
    }

    public List<DetallePedidoDTO> detallePedido(int idPedido) throws SQLException {

        try (Connection conexion = conexionBD.obtenerConexion()) {
            return new ConsultaDAO(conexion).detallePedido(idPedido);
        }
    }

    public List<HistorialClienteDTO> historialCliente(int idCliente) throws SQLException {

        try (Connection conexion = conexionBD.obtenerConexion()) {
            return new ConsultaDAO(conexion).historialCliente(idCliente);
        }
    }

    public List<VentaDTO> masVendidos(int limite) throws SQLException {

        try (Connection conexion = conexionBD.obtenerConexion()) {
            return new ConsultaDAO(conexion).masVendidos(limite);
        }
    }

    public List<MovimientoStock> movimientos(int idVinilo) throws SQLException {

        try (Connection conexion = conexionBD.obtenerConexion()) {
            return new MovimientoStockDAO(conexion).obtenerPorVinilo(idVinilo);
        }
    }

    public EstadoBDDTO estadoBD(int idVinilo) throws SQLException {

        try (Connection conexion = conexionBD.obtenerConexion()) {
            return new ConsultaDAO(conexion).obtenerEstado(idVinilo);
        }
    }

    public ResumenClienteDTO resumenCliente(int idCliente) throws SQLException {

        if (!"MYSQL".equals(configuracionBD.obtener("database.engine"))) {
            log.warn("El procedimiento resumen_cliente es específico de MySQL; no se ejecuta con H2");
            return null;
        }

        try (Connection conexion = conexionBD.obtenerConexion()) {

            ResumenClienteDTO resumen = new ConsultaDAO(conexion).resumenCliente(idCliente);
            log.info("Procedimiento resumen_cliente ejecutado cliente={}", idCliente);

            return resumen;
        }
    }
}