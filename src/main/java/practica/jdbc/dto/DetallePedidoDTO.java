package practica.jdbc.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DetallePedidoDTO(int idPedido, String cliente, LocalDateTime fecha, String estadoPedido,
                               String estadoPago, String referencia, String album, int cantidad,
                               BigDecimal precioUnitario, BigDecimal importe) {
}