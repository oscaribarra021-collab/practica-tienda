package practica.jdbc.dao;

import practica.jdbc.dto.CatalogoDTO;
import practica.jdbc.dto.DetallePedidoDTO;
import practica.jdbc.dto.EstadoBDDTO;
import practica.jdbc.dto.HistorialClienteDTO;
import practica.jdbc.dto.ResumenClienteDTO;
import practica.jdbc.dto.VentaDTO;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ConsultaDAO {

    private static final String BASE_CATALOGO =
            "SELECT v.id_vinilo, v.referencia, a.nombre AS album, g.nombre AS genero, v.color, v.precio, i.stock_actual "
                    + "FROM vinilo v "
                    + "JOIN album a ON v.id_album = a.id_album "
                    + "JOIN genero g ON a.id_genero = g.id_genero "
                    + "JOIN inventario i ON v.id_vinilo = i.id_vinilo ";

    private final Connection conexion;

    public ConsultaDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public List<CatalogoDTO> catalogoFiltrado(String genero, BigDecimal precioMin, BigDecimal precioMax,
                                              boolean soloDisponibles, int pagina, int tamanoPagina) throws SQLException {

        StringBuilder sql = new StringBuilder(BASE_CATALOGO + "WHERE v.activo = TRUE");
        List<Object> parametros = new ArrayList<>();

        if (genero != null) {
            sql.append(" AND g.nombre = ?");
            parametros.add(genero);
        }

        if (precioMin != null) {
            sql.append(" AND v.precio >= ?");
            parametros.add(precioMin);
        }

        if (precioMax != null) {
            sql.append(" AND v.precio <= ?");
            parametros.add(precioMax);
        }

        if (soloDisponibles) {
            sql.append(" AND i.stock_actual > 0");
        }

        sql.append(" ORDER BY a.nombre, v.referencia LIMIT ? OFFSET ?");
        parametros.add(tamanoPagina);
        parametros.add((pagina - 1) * tamanoPagina);

        List<CatalogoDTO> lista = new ArrayList<>();

        try (PreparedStatement sentencia = conexion.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                sentencia.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    lista.add(mapearCatalogo(resultado));
                }
            }
        }

        return lista;
    }

    public List<CatalogoDTO> stockBajo() throws SQLException {

        String sql = BASE_CATALOGO
                + "WHERE i.control_stock = TRUE AND i.stock_actual <= i.stock_minimo "
                + "ORDER BY i.stock_actual, v.referencia";

        List<CatalogoDTO> lista = new ArrayList<>();

        try (PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                lista.add(mapearCatalogo(resultado));
            }
        }

        return lista;
    }

    public List<CatalogoDTO> vinilosPorArtista(String nombreArtista) throws SQLException {

        String sql = BASE_CATALOGO
                + "JOIN album_artista aa ON a.id_album = aa.id_album "
                + "JOIN artista ar ON aa.id_artista = ar.id_artista "
                + "WHERE ar.nombre = ? "
                + "ORDER BY a.anio_lanzamiento, v.referencia";

        List<CatalogoDTO> lista = new ArrayList<>();

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, nombreArtista);

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    lista.add(mapearCatalogo(resultado));
                }
            }
        }

        return lista;
    }

    private CatalogoDTO mapearCatalogo(ResultSet resultado) throws SQLException {

        return new CatalogoDTO(
                resultado.getInt("id_vinilo"),
                resultado.getString("referencia"),
                resultado.getString("album"),
                resultado.getString("genero"),
                resultado.getString("color"),
                resultado.getBigDecimal("precio"),
                resultado.getInt("stock_actual"));
    }

    public List<DetallePedidoDTO> detallePedido(int idPedido) throws SQLException {

        String sql = "SELECT p.id_pedido, c.nombre, c.apellidos, p.fecha_pedido, p.estado AS estado_pedido, "
                + "pg.estado AS estado_pago, v.referencia, a.nombre AS album, l.cantidad, l.precio_unitario, "
                + "(l.cantidad * l.precio_unitario) AS importe "
                + "FROM pedido p "
                + "JOIN cliente c ON p.id_cliente = c.id_cliente "
                + "JOIN pago pg ON p.id_pedido = pg.id_pedido "
                + "JOIN linea_pedido l ON p.id_pedido = l.id_pedido "
                + "JOIN vinilo v ON l.id_vinilo = v.id_vinilo "
                + "JOIN album a ON v.id_album = a.id_album "
                + "WHERE p.id_pedido = ? "
                + "ORDER BY l.id_linea";

        List<DetallePedidoDTO> lista = new ArrayList<>();

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idPedido);

            try (ResultSet resultado = sentencia.executeQuery()) {

                while (resultado.next()) {

                    String cliente = resultado.getString("nombre") + " " + resultado.getString("apellidos");

                    lista.add(new DetallePedidoDTO(
                            resultado.getInt("id_pedido"),
                            cliente,
                            resultado.getTimestamp("fecha_pedido").toLocalDateTime(),
                            resultado.getString("estado_pedido"),
                            resultado.getString("estado_pago"),
                            resultado.getString("referencia"),
                            resultado.getString("album"),
                            resultado.getInt("cantidad"),
                            resultado.getBigDecimal("precio_unitario"),
                            resultado.getBigDecimal("importe")));
                }
            }
        }

        return lista;
    }

    public List<HistorialClienteDTO> historialCliente(int idCliente) throws SQLException {

        String sql = "SELECT p.id_pedido, p.fecha_pedido, p.estado, p.total, "
                + "COUNT(l.id_linea) AS num_lineas, COALESCE(SUM(l.cantidad), 0) AS unidades "
                + "FROM pedido p "
                + "LEFT JOIN linea_pedido l ON p.id_pedido = l.id_pedido "
                + "WHERE p.id_cliente = ? "
                + "GROUP BY p.id_pedido, p.fecha_pedido, p.estado, p.total "
                + "ORDER BY p.fecha_pedido, p.id_pedido";

        List<HistorialClienteDTO> lista = new ArrayList<>();

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idCliente);

            try (ResultSet resultado = sentencia.executeQuery()) {

                while (resultado.next()) {

                    lista.add(new HistorialClienteDTO(
                            resultado.getInt("id_pedido"),
                            resultado.getTimestamp("fecha_pedido").toLocalDateTime(),
                            resultado.getString("estado"),
                            resultado.getBigDecimal("total"),
                            resultado.getInt("num_lineas"),
                            resultado.getInt("unidades")));
                }
            }
        }

        return lista;
    }

    public List<VentaDTO> masVendidos(int limite) throws SQLException {

        String sql = "SELECT v.referencia, a.nombre AS album, SUM(l.cantidad) AS unidades, "
                + "SUM(l.cantidad * l.precio_unitario) AS ingresos "
                + "FROM linea_pedido l "
                + "JOIN pedido p ON l.id_pedido = p.id_pedido "
                + "JOIN vinilo v ON l.id_vinilo = v.id_vinilo "
                + "JOIN album a ON v.id_album = a.id_album "
                + "WHERE p.estado <> 'CANCELADO' "
                + "GROUP BY v.id_vinilo, v.referencia, a.nombre "
                + "ORDER BY unidades DESC, v.referencia "
                + "LIMIT ?";

        List<VentaDTO> lista = new ArrayList<>();

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, limite);

            try (ResultSet resultado = sentencia.executeQuery()) {

                while (resultado.next()) {

                    lista.add(new VentaDTO(
                            resultado.getString("referencia"),
                            resultado.getString("album"),
                            resultado.getInt("unidades"),
                            resultado.getBigDecimal("ingresos")));
                }
            }
        }

        return lista;
    }

    public ResumenClienteDTO resumenCliente(int idCliente) throws SQLException {

        try (CallableStatement llamada = conexion.prepareCall("{CALL resumen_cliente(?, ?, ?)}")) {

            llamada.setInt(1, idCliente);
            llamada.registerOutParameter(2, Types.INTEGER);
            llamada.registerOutParameter(3, Types.DECIMAL);

            llamada.execute();

            return new ResumenClienteDTO(llamada.getInt(2), llamada.getBigDecimal(3));
        }
    }

    public EstadoBDDTO obtenerEstado(int idVinilo) throws SQLException {

        return new EstadoBDDTO(
                contar("pedido"),
                contar("linea_pedido"),
                contar("movimiento_stock"),
                contar("pago"),
                stockDe(idVinilo));
    }

    private int contar(String tabla) throws SQLException {

        try (PreparedStatement sentencia = conexion.prepareStatement("SELECT COUNT(*) FROM " + tabla);
             ResultSet resultado = sentencia.executeQuery()) {

            resultado.next();
            return resultado.getInt(1);
        }
    }

    private int stockDe(int idVinilo) throws SQLException {

        try (PreparedStatement sentencia = conexion.prepareStatement("SELECT stock_actual FROM inventario WHERE id_vinilo = ?")) {

            sentencia.setInt(1, idVinilo);

            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getInt(1);
            }
        }
    }
}