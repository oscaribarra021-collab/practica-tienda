package practica.jdbc.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfiguracionBD {

    private static final Properties propiedades = new Properties();

    public ConfiguracionBD(){
        try (InputStream archivoConfiguracion = ConfiguracionBD.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (archivoConfiguracion == null) {
                throw new RuntimeException("No se encuentra application.properties");
            }

            propiedades.load(archivoConfiguracion);

        } catch (IOException e) {
            throw new RuntimeException("Error al cargar la configuracion", e);
        }

        validar("database.engine", "MYSQL", "H2");
        validar("connection.mode", "DRIVER_MANAGER", "POOL");
        validar("simulation.mode", "NORMAL", "FALLO_TRANSACCION");
    }


    private void validar(String clave, String... valoresPermitidos) {

        String valor = propiedades.getProperty(clave);

        if (valor == null) {
            throw new RuntimeException("Falta la propiedad: " + clave);
        }

        for (String permitido : valoresPermitidos) {
            if (permitido.equals(valor)) {
                return;
            }
        }

        throw new RuntimeException("Valor no valido para " + clave + ": " + valor);
    }

    public String obtener(String clave) {
        return propiedades.getProperty(clave);
    }
}