package practica.jdbc.excepciones;

public class DatosInvalidosException extends NegocioException {

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}