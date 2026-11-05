package practica.jdbc.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    private final ConfiguracionBD configuracionBD;

    public ConexionBD(ConfiguracionBD configuracionBD) {
        this.configuracionBD = configuracionBD;
    }

    public Connection obtenerConexion() throws SQLException {

        String tipoBD = configuracionBD.obtener("bd.tipo");

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

        throw new IllegalArgumentException("Tipo de base de datos no valido: " + tipoBD);
    }
}