package practica.jdbc.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class ConexionBD {

    private final ConfiguracionBD configuracionBD;

    private HikariDataSource poolConexiones;

    public ConexionBD(ConfiguracionBD configuracionBD) {
        this.configuracionBD = configuracionBD;
    }

    public Connection obtenerConexion() throws SQLException {

        String mecanismo = configuracionBD.obtener("connection.mode");

        if ("POOL".equals(mecanismo)) {

            if (poolConexiones == null) {
                configurarPool();
            }

            return poolConexiones.getConnection();
        }


        return DriverManager.getConnection(
                obtenerDatoBD("url"),
                obtenerDatoBD("usuario"),
                obtenerDatoBD("contrasena"));
    }

    private String obtenerDatoBD(String dato) {

        String tipoBD = configuracionBD.obtener("database.engine");

        if ("MYSQL".equals(tipoBD)) {
            return configuracionBD.obtener("mysql." + dato);
        }

        return configuracionBD.obtener("h2." + dato);
    }

    private void configurarPool() {

        HikariConfig configuracion = new HikariConfig();

        configuracion.setJdbcUrl(obtenerDatoBD("url"));
        configuracion.setUsername(obtenerDatoBD("usuario"));
        configuracion.setPassword(obtenerDatoBD("contrasena"));

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