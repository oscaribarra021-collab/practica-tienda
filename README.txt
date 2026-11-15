# VinylStation (Práctica UD2 · JDBC)

Tienda online de vinilos en Java con JDBC puro. Funciona con MySQL o H2, y con DriverManager o HikariCP.

## Entidades y relaciones
Género, artista, álbum, vinilo, inventario, movimiento de stock, cliente, dirección, pedido, línea de pedido y pago.
- 1:N: cliente → pedidos, género → álbumes...
- 1:1: vinilo ↔ inventario, pedido ↔ pago
- N:M: álbum ↔ artista (tabla `album_artista`)

Diagrama en [docs/er.md](docs/er.md).

## Requisitos
Java 25, Maven y, para MySQL, un servidor accesible. H2 no necesita instalación.

## Configuración
1. Copia `src/main/resources/application.properties.example` a `application.properties`.
2. Pon tus credenciales de MySQL (no se suben al repositorio).
3. Elige `database.engine` (MYSQL/H2), `connection.mode` (DRIVER_MANAGER/POOL) y `simulation.mode` (NORMAL/FALLO_TRANSACCION).

## Ejecución
```
mvn compile exec:java -Dexec.mainClass=practica.jdbc.app.Main
mvn test
```
(o ejecutar `Main` desde IntelliJ).

## Cómo comprobar resultados
- La consola muestra los efectos de cada operación leídos de la BD.
- Con `FALLO_TRANSACCION` se imprime `ROLLBACK COMPROBADO` tras comparar el estado antes y después con SQL.
- Evidencias completas en `logs/evidencias/`.

## Diferencias MySQL / H2
El procedimiento almacenado `resumen_cliente` solo existe en MySQL. Con H2 se avisa y se omite: es una limitación del gestor, no un fallo.

Repositorio: https://github.com/oscaribarra021-collab/practica-tienda