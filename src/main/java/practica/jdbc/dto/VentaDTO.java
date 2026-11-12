package practica.jdbc.dto;

import java.math.BigDecimal;

public record VentaDTO(String referencia, String album, int unidadesVendidas, BigDecimal ingresos) {
}