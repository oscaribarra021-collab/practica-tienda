package practica.jdbc.excepciones;

public class PedidoCanceladoException extends NegocioException {

    public PedidoCanceladoException(String mensaje) {
        super(mensaje);
    }
}