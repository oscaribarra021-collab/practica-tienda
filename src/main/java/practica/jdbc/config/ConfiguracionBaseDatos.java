package practica.jdbc.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfiguracionBaseDatos {

    private static final Properties propiedades = new Properties();

    public ConfiguracionBaseDatos(){
        try (InputStream archivoConfiguracion = ConfiguracionBaseDatos.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (archivoConfiguracion == null) {
                throw new RuntimeException("No se encuentra application.properties");
            }

            propiedades.load(archivoConfiguracion);

        } catch (IOException e) {
            throw new RuntimeException("Error al cargar la configuracion", e);
        }
    }

    public String obtener(String clave) {
        return propiedades.getProperty(clave);
    }
}

