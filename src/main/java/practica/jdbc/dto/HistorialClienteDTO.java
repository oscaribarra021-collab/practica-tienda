package practica.jdbc.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HistorialClienteDTO(int idPedido, LocalDateTime fecha, String estado,
                                  BigDecimal total, int numLineas, int unidades) {
}