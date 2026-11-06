package practica.jdbc.app;

import practica.jdbc.config.ConfiguracionBD;
import practica.jdbc.config.ConexionBD;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    static void main(String[] args) {

        System.out.println("===============================================");
        System.out.println("          BIENVENIDO A VINYLSTATION");
        System.out.println("===============================================");

        // CONFIGURACIÓN DE APPLICATION.PROPERTIES
        ConfiguracionBD configuracionBD = new ConfiguracionBD();
        System.out.println("Base de datos seleccionada: " + configuracionBD.obtener("bd.tipo"));


        // CONECTA EL PRGRAMA CON LA BASE DE DATOS
        ConexionBD conexionBD = new ConexionBD(configuracionBD);
        try (Connection conexion = conexionBD.obtenerConexion()) {

            System.out.println("Conexion con la base de datos establecida correctamente.");

        } catch (SQLException e) {

            System.out.println("Error al conectar con la base de datos: " + e.getMessage());

        } finally {

            conexionBD.cerrarPool();
        }


    }
}
