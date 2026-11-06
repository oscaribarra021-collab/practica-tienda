package practica.jdbc.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class ConexionBD {

    private final ConfiguracionBD configuracionBD;

    public ConexionBD(ConfiguracionBD configuracionBD) {
        this.configuracionBD = configuracionBD;
    }

    // CONEXIÓN A CUALQUIER BASE DE DATOS, COMPRUEBA EN CUÁL ESTÁ Y SE CONECTA. LO MISMO CON EL MECANISMO DE CONEXIÓN
    public Connection obtenerConexion() throws SQLException {

        String tipoBD = configuracionBD.obtener("bd.tipo");
        String mecanismo = configuracionBD.obtener("bd.conexion");

        // CONEXIÓN A BASE DE DATOS
        if ("mysql".equals(tipoBD)) {

            String url = configuracionBD.obtener("mysql.url");
            String usuario = configuracionBD.obtener("mysql.usuario");
            String contrasena = configuracionBD.obtener("mysql.contrasena");

            return DriverManager.getConnection(url, usuario, contrasena);
        }

        if ("h2".equals(tipoBD)) {

            String url = configuracionBD.obtener("h2.url");
            String usuario = configuracionBD.obtener("h2.usuario");
            String contrasena = configuracionBD.obtener("h2.contrasena");

            return DriverManager.getConnection(url, usuario, contrasena);
        }

        // MECANISMO DE CONEXIÓN
        if ("hikari".equals(mecanismo)) {

            if (poolConexiones == null) {
                configurarPool();
            }

            return poolConexiones.getConnection();
        }

        throw new IllegalArgumentException("Tipo de base de datos no valido: " + tipoBD);
    }

    private HikariDataSource poolConexiones;

    // LE DICE DE DÓNDE SACAR LOS DATOS AL POOL
    private void configurarPool() {

        HikariConfig configuracion = new HikariConfig();

        String tipoBD = configuracionBD.obtener("bd.tipo");

        String url;
        String usuario;
        String contrasena;

        if ("mysql".equals(tipoBD)) {

            url = configuracionBD.obtener("mysql.url");
            usuario = configuracionBD.obtener("mysql.usuario");
            contrasena = configuracionBD.obtener("mysql.contrasena");

        } else if ("h2".equals(tipoBD)) {

            url = configuracionBD.obtener("h2.url");
            usuario = configuracionBD.obtener("h2.usuario");
            contrasena = configuracionBD.obtener("h2.contrasena");

        } else {

            throw new IllegalArgumentException("Tipo de base de datos no valido: " + tipoBD);
        }

        configuracion.setJdbcUrl(url);
        configuracion.setUsername(usuario);
        configuracion.setPassword(contrasena);

        configuracion.setPoolName(configuracionBD.obtener("hikari.pool.nombre"));
        configuracion.setMaximumPoolSize(Integer.parseInt(configuracionBD.obtener("hikari.pool.maximo")));
        configuracion.setMinimumIdle(Integer.parseInt(configuracionBD.obtener("hikari.pool.minimo")));

        poolConexiones = new HikariDataSource(configuracion);
    }

    public void cerrarPool() {

        if (poolConexiones != null) {
            poolConexiones.close();
        }
    }
}
