package practica.jdbc.excepciones;

public class StockInsuficienteException extends NegocioException {

    public StockInsuficienteException(String mensaje) {
        super(mensaje);
    }
}