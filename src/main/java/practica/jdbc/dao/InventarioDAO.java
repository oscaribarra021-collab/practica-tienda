package practica.jdbc.dao;

import practica.jdbc.datos.Inventario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InventarioDAO {

    private final Connection conexion;

    public InventarioDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public Inventario buscarPorVinilo(int idVinilo) throws SQLException {

        String sql = "SELECT * FROM inventario WHERE id_vinilo = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idVinilo);

            try (ResultSet resultado = sentencia.executeQuery()) {

                if (resultado.next()) {

                    int id = resultado.getInt("id_inventario");
                    int idViniloResultado = resultado.getInt("id_vinilo");
                    int stockActual = resultado.getInt("stock_actual");

                    return new Inventario(id, idViniloResultado, stockActual);
                }
            }
        }
        return null;
    }

    public List<Inventario> listarTodos() throws SQLException {

        List<Inventario> inventarios = new ArrayList<>();

        String sql = "SELECT * FROM inventario";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            try (ResultSet resultado = sentencia.executeQuery()) {

                while (resultado.next()) {

                    int id = resultado.getInt("id_inventario");
                    int idVinilo = resultado.getInt("id_vinilo");
                    int stockActual = resultado.getInt("stock_actual");

                    Inventario inventario = new Inventario(id, idVinilo, stockActual);

                    inventarios.add(inventario);
                }
            }
        }
        return inventarios;
    }

    public void actualizarStock(int idVinilo, int stockActual) throws SQLException {

        String sql = "UPDATE inventario SET stock_actual = ? WHERE id_vinilo = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, stockActual);
            sentencia.setInt(2, idVinilo);
            sentencia.executeUpdate();
        }
    }

    public void aumentarStock(int idVinilo, int cantidad) throws SQLException {

        String sql = "UPDATE inventario SET stock_actual = stock_actual + ? WHERE id_vinilo = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, cantidad);
            sentencia.setInt(2, idVinilo);
            sentencia.executeUpdate();
        }
    }

    public boolean reducirStock(int idVinilo, int cantidad) throws SQLException {

        String sql = "UPDATE inventario SET stock_actual = stock_actual - ? " + "WHERE id_vinilo = ? AND stock_actual >= ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, cantidad);
            sentencia.setInt(2, idVinilo);
            sentencia.setInt(3, cantidad);

            int filasAfectadas = sentencia.executeUpdate();

            return filasAfectadas > 0;
        }
    }

    public void insertar(int idVinilo, int stockActual) throws SQLException {

        String sql = "INSERT INTO inventario (id_vinilo, stock_actual) VALUES (?, ?)";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, idVinilo);
            sentencia.setInt(2, stockActual);
            sentencia.executeUpdate();
        }
    }

    public boolean exigeControlStock(int idVinilo) throws SQLException {

        String sql = "SELECT control_stock FROM inventario WHERE id_vinilo = ?";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, idVinilo);

            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getBoolean("control_stock");
                }
            }
        }

        return true;
    }
}
