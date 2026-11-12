package practica.jdbc.dto;

import java.math.BigDecimal;

public record CatalogoDTO(int idVinilo, String referencia, String album, String genero,
                          String color, BigDecimal precio, int stock) {
}