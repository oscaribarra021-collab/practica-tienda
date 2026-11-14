
package practica.jdbc.servicio;

import practica.jdbc.config.ConexionBD;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class CargaInicialServicio {

    private final ConexionBD conexionBD;

    public CargaInicialServicio(ConexionBD conexionBD) {
        this.conexionBD = conexionBD;
    }

    public void cargarDatosIniciales() throws SQLException {

        try (Connection conexion = conexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);

            try {
                Map<String, Integer> generos = cargarGeneros(conexion);
                Map<String, Integer> artistas = cargarArtistas(conexion);
                Map<String, Integer> albumes = cargarAlbumes(conexion, generos);

                cargarRelacionesAlbumArtista(conexion, albumes, artistas);

                Map<String, Integer> vinilos = cargarVinilos(conexion, albumes);
                Map<String, Integer> clientes = cargarClientes(conexion);

                cargarDirecciones(conexion, clientes);
                cargarInventario(conexion, vinilos);

                conexion.commit();

            } catch (SQLException | RuntimeException e) {
                conexion.rollback();
                throw e;
            }
        }
    }

    private int insertarYObtenerId(Connection conexion, String sql, Object... valores) throws SQLException {

        try (PreparedStatement sentencia = conexion.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            for (int i = 0; i < valores.length; i++) {
                sentencia.setObject(i + 1, valores[i]);
            }

            sentencia.executeUpdate();

            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    return claves.getInt(1);
                }
            }
        }

        throw new SQLException("No se ha podido recuperar el ID generado.");
    }

    private Map<String, Integer> cargarGeneros(Connection conexion) throws SQLException {

        Map<String, Integer> generos = new LinkedHashMap<>();

        String sql = "INSERT INTO genero (nombre, descripcion) VALUES (?, ?)";

        Object[][] datos = {
                {"Rock", "Música rock"},
                {"Pop", "Música pop"},
                {"Hip-Hop", "Música hip-hop"},
                {"Electrónica", "Música electrónica"}
        };

        for (Object[] dato : datos) {
            int id = insertarYObtenerId(conexion, sql, dato);
            generos.put((String) dato[0], id);
        }

        return generos;
    }

    private Map<String, Integer> cargarArtistas(Connection conexion) throws SQLException {

        Map<String, Integer> artistas = new LinkedHashMap<>();

        String sql = "INSERT INTO artista (nombre, pais_origen) VALUES (?, ?)";

        Object[][] datos = {
                {"Pink Floyd", "Reino Unido"},
                {"Black Sabbath", "Reino Unido"},
                {"Michael Jackson", "Estados Unidos"},
                {"Madonna", "Estados Unidos"},
                {"Eminem", "Estados Unidos"},
                {"50 Cent", "Estados Unidos"},
                {"Avicii", "Suecia"},
                {"Daft Punk", "Francia"}
        };

        for (Object[] dato : datos) {
            int id = insertarYObtenerId(conexion, sql, dato);
            artistas.put((String) dato[0], id);
        }

        return artistas;
    }

    private Map<String, Integer> cargarAlbumes(Connection conexion, Map<String, Integer> generos) throws SQLException {

        Map<String, Integer> albumes = new LinkedHashMap<>();

        String sql = "INSERT INTO album (nombre, anio_lanzamiento, id_genero) VALUES (?, ?, ?)";

        Object[][] datos = {
                {"The Dark Side of the Moon", 1973, "Rock"},
                {"Paranoid", 1970, "Rock"},
                {"Thriller", 1982, "Pop"},
                {"True Blue", 1986, "Pop"},
                {"True", 2013, "Electrónica"},
                {"Discovery", 2001, "Electrónica"},
                {"The Eminem Show", 2002, "Hip-Hop"},
                {"Get Rich or Die Tryin'", 2003, "Hip-Hop"}
        };

        for (Object[] dato : datos) {
            String nombre = (String) dato[0];
            int anio = (int) dato[1];
            int idGenero = generos.get((String) dato[2]);

            int id = insertarYObtenerId(conexion, sql, nombre, anio, idGenero);
            albumes.put(nombre, id);
        }

        return albumes;
    }

    private void cargarRelacionesAlbumArtista(Connection conexion, Map<String, Integer> albumes, Map<String, Integer> artistas) throws SQLException {

        String sql = "INSERT INTO album_artista (id_album, id_artista) VALUES (?, ?)";

        Object[][] datos = {
                {"The Dark Side of the Moon", "Pink Floyd"},
                {"Paranoid", "Black Sabbath"},
                {"Thriller", "Michael Jackson"},
                {"True Blue", "Madonna"},
                {"True", "Avicii"},
                {"Discovery", "Daft Punk"},
                {"The Eminem Show", "Eminem"},
                {"Get Rich or Die Tryin'", "50 Cent"}
        };

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            for (Object[] dato : datos) {
                sentencia.setInt(1, albumes.get((String) dato[0]));
                sentencia.setInt(2, artistas.get((String) dato[1]));
                sentencia.addBatch();
            }

            sentencia.executeBatch();
        }
    }

    private Map<String, Integer> cargarVinilos(Connection conexion, Map<String, Integer> albumes) throws SQLException {

        Map<String, Integer> vinilos = new LinkedHashMap<>();

        String sql = "INSERT INTO vinilo (id_album, referencia, color, precio) VALUES (?, ?, ?, ?)";

        Object[][] datos = {
                {"The Dark Side of the Moon", "PF001", "Negro", "27.99"},
                {"Paranoid", "BS001", "Negro", "24.99"},
                {"Thriller", "MJ001", "Negro", "26.99"},
                {"True Blue", "MD001", "Azul", "23.99"},
                {"True", "AV001", "Negro", "21.99"},
                {"Discovery", "DP001", "Dorado", "29.99"},
                {"The Eminem Show", "EM001", "Negro", "25.99"},
                {"Get Rich or Die Tryin'", "FC001", "Negro", "24.99"}
        };

        for (Object[] dato : datos) {
            int idAlbum = albumes.get((String) dato[0]);
            String referencia = (String) dato[1];
            String color = (String) dato[2];
            BigDecimal precio = new BigDecimal((String) dato[3]);

            int id = insertarYObtenerId(conexion, sql, idAlbum, referencia, color, precio);
            vinilos.put(referencia, id);
        }

        return vinilos;
    }

    private Map<String, Integer> cargarClientes(Connection conexion) throws SQLException {

        Map<String, Integer> clientes = new LinkedHashMap<>();

        String sql = "INSERT INTO cliente (nombre, apellidos, email, telefono) VALUES (?, ?, ?, ?)";

        Object[][] datos = {
                {"Álvaro", "Sun Chan", "alvaro@gmail.com", "612345678"},
                {"Almudena", "Martínez Ruíz", "almudena@gmail.com", "623456789"},
                {"Alejandro", "Fernández Castro", "alejandro@gmail.com", "634567890"},
                {"Jorge", "Tobías Paraguay", "jorge@gmail.com", "645678901"},
                {"Carlos", "López Perú", "carlos@gmail.com", "656789012"}
        };

        for (Object[] dato : datos) {
            int id = insertarYObtenerId(conexion, sql, dato);
            clientes.put((String) dato[2], id);
        }

        return clientes;
    }

    private void cargarDirecciones(Connection conexion, Map<String, Integer> clientes) throws SQLException {

        String sql = "INSERT INTO direccion (id_cliente, calle, numero, codigo_postal, ciudad, provincia, pais) VALUES (?, ?, ?, ?, ?, ?, ?)";

        Object[][] datos = {
                {"alvaro@gmail.com", "Calle Mayor", "12", "28013", "Madrid", "Madrid", "España"},
                {"almudena@gmail.com", "Calle Alcalá", "45", "28014", "Madrid", "Madrid", "España"},
                {"alejandro@gmail.com", "Avenida de Andalucía", "23", "41007", "Sevilla", "Sevilla", "España"},
                {"jorge@gmail.com", "Calle Colón", "18", "46004", "Valencia", "Valencia", "España"},
                {"carlos@gmail.com", "Gran Vía", "30", "48011", "Bilbao", "Vizcaya", "España"}
        };

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            for (Object[] dato : datos) {
                sentencia.setInt(1, clientes.get((String) dato[0]));
                sentencia.setString(2, (String) dato[1]);
                sentencia.setString(3, (String) dato[2]);
                sentencia.setString(4, (String) dato[3]);
                sentencia.setString(5, (String) dato[4]);
                sentencia.setString(6, (String) dato[5]);
                sentencia.setString(7, (String) dato[6]);
                sentencia.addBatch();
            }

            sentencia.executeBatch();
        }
    }

    private void cargarInventario(Connection conexion, Map<String, Integer> vinilos) throws SQLException {

        String sql = "INSERT INTO inventario (id_vinilo, stock_actual) VALUES (?, ?)";

        Object[][] datos = {
                {"PF001", 10},
                {"BS001", 4},
                {"MJ001", 0},
                {"MD001", 7},
                {"AV001", 2},
                {"DP001", 6},
                {"EM001", 3},
                {"FC001", 8}
        };

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            for (Object[] dato : datos) {
                sentencia.setInt(1, vinilos.get((String) dato[0]));
                sentencia.setInt(2, (int) dato[1]);
                sentencia.addBatch();
            }

            sentencia.executeBatch();
        }
    }
}
