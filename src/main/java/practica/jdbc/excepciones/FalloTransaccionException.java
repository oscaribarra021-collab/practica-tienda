package practica.jdbc.excepciones;

public class FalloTransaccionException extends RuntimeException {

    public FalloTransaccionException(String mensaje) {
        super(mensaje);
    }
}