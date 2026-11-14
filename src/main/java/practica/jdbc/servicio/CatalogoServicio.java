package practica.jdbc.servicio;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import practica.jdbc.config.ConexionBD;
import practica.jdbc.dao.AlbumArtistaDAO;
import practica.jdbc.dao.AlbumDAO;
import practica.jdbc.dao.ArtistaDAO;
import practica.jdbc.dao.InventarioDAO;
import practica.jdbc.dao.MovimientoStockDAO;
import practica.jdbc.dao.ViniloDAO;
import practica.jdbc.datos.Album;
import practica.jdbc.datos.AlbumArtista;
import practica.jdbc.datos.Artista;
import practica.jdbc.datos.MovimientoStock;
import practica.jdbc.datos.Vinilo;
import practica.jdbc.excepciones.DatosInvalidosException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CatalogoServicio {

    private static final Logger log = LogManager.getLogger(CatalogoServicio.class);

    private final ConexionBD conexionBD;

    public CatalogoServicio(ConexionBD conexionBD) {
        this.conexionBD = conexionBD;
    }

    public Vinilo buscarVinilo(String referencia) throws SQLException {

        try (Connection conexion = conexionBD.obtenerConexion()) {

            for (Vinilo vinilo : new ViniloDAO(conexion).listarTodos()) {
                if (vinilo.getReferencia().equals(referencia)) {
                    return vinilo;
                }
            }
        }

        throw new DatosInvalidosException("No existe el vinilo con referencia " + referencia);
    }

    public int registrarVinilo(String nombreAlbum, String referencia, String color, BigDecimal precio, int stockInicial) throws SQLException {

        if (referencia == null || referencia.isBlank() || color == null || color.isBlank()) {
            throw new DatosInvalidosException("La referencia y el color son obligatorios");
        }

        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DatosInvalidosException("El precio debe ser mayor que cero");
        }

        if (stockInicial < 0) {
            throw new DatosInvalidosException("El stock inicial no puede ser negativo");
        }

        try (Connection conexion = conexionBD.obtenerConexion()) {

            conexion.setAutoCommit(false);

            try {
                ViniloDAO viniloDAO = new ViniloDAO(conexion);
                InventarioDAO inventarioDAO = new InventarioDAO(conexion);
                MovimientoStockDAO movimientoStockDAO = new MovimientoStockDAO(conexion);

                Album album = buscarAlbum(new AlbumDAO(conexion), nombreAlbum);

                int idVinilo = viniloDAO.insertarConId(new Vinilo(0, album.getIdAlbum(), referencia, color, precio));
                inventarioDAO.insertar(idVinilo, stockInicial);

                if (stockInicial > 0) {
                    movimientoStockDAO.insertar(new MovimientoStock(0, idVinilo, "ENTRADA", stockInicial,
                            LocalDateTime.now(), "STOCK INICIAL"));
                }

                conexion.commit();
                log.info("Vinilo registrado id={} referencia={}", idVinilo, referencia);

                return idVinilo;

            } catch (SQLException | RuntimeException e) {
                conexion.rollback();
                log.warn("Registro de vinilo revertido: {}", e.getMessage());
                throw e;
            }
        }
    }

    public void cambiarPrecio(String referencia, BigDecimal nuevoPrecio) throws SQLException {

        if (nuevoPrecio == null || nuevoPrecio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DatosInvalidosException("El precio debe ser mayor que cero");
        }

        Vinilo vinilo = buscarVinilo(referencia);
        vinilo.setPrecio(nuevoPrecio);

        try (Connection conexion = conexionBD.obtenerConexion()) {
            new ViniloDAO(conexion).actualizar(vinilo);
        }

        log.info("Precio modificado referencia={} nuevoPrecio={}", referencia, nuevoPrecio);
    }

    public void cambiarDisponibilidad(String referencia, boolean activo) throws SQLException {

        Vinilo vinilo = buscarVinilo(referencia);

        try (Connection conexion = conexionBD.obtenerConexion()) {
            new ViniloDAO(conexion).cambiarActivo(vinilo.getIdVinilo(), activo);
        }

        log.info("Disponibilidad modificada referencia={} activo={}", referencia, activo);
    }

    public void asociarArtista(String nombreAlbum, String nombreArtista) throws SQLException {

        try (Connection conexion = conexionBD.obtenerConexion()) {

            Album album = buscarAlbum(new AlbumDAO(conexion), nombreAlbum);
            Artista artista = null;

            for (Artista a : new ArtistaDAO(conexion).listarTodos()) {
                if (a.getNombre().equals(nombreArtista)) {
                    artista = a;
                }
            }

            if (artista == null) {
                throw new DatosInvalidosException("No existe el artista " + nombreArtista);
            }

            new AlbumArtistaDAO(conexion).asociarArtistaAlbum(new AlbumArtista(album.getIdAlbum(), artista.getIdArtista()));

            log.info("Artista asociado album={} artista={}", nombreAlbum, nombreArtista);
        }
    }

    public List<String> artistasDeAlbum(String nombreAlbum) throws SQLException {

        List<String> nombres = new ArrayList<>();

        try (Connection conexion = conexionBD.obtenerConexion()) {

            Album album = buscarAlbum(new AlbumDAO(conexion), nombreAlbum);

            for (Artista artista : new AlbumArtistaDAO(conexion).obtenerArtistasPorAlbum(album.getIdAlbum())) {
                nombres.add(artista.getNombre());
            }
        }

        return nombres;
    }

    private Album buscarAlbum(AlbumDAO albumDAO, String nombreAlbum) throws SQLException {

        for (Album album : albumDAO.listarTodos()) {
            if (album.getNombre().equals(nombreAlbum)) {
                return album;
            }
        }

        throw new DatosInvalidosException("No existe el álbum " + nombreAlbum);
    }
}