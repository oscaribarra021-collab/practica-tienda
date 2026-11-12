package practica.jdbc.dto;

import java.math.BigDecimal;

public record ResumenClienteDTO(int numPedidos, BigDecimal totalGastado) {
}