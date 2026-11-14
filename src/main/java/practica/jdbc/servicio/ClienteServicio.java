package practica.jdbc.servicio;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import practica.jdbc.config.ConexionBD;
import practica.jdbc.dao.ClienteDAO;
import practica.jdbc.dao.DireccionDAO;
import practica.jdbc.datos.Cliente;
import practica.jdbc.datos.Direccion;
import practica.jdbc.excepciones.DatosInvalidosException;

import java.sql.Connection;
import java.sql.SQLException;

public class ClienteServicio {

    private static final Logger log = LogManager.getLogger(ClienteServicio.class);

    private final ConexionBD conexionBD;

    public ClienteServicio(ConexionBD conexionBD) {
        this.conexionBD = conexionBD;
    }

    public int registrarCliente(Cliente cliente, Direccion direccion) throws SQLException {

        if (cliente == null || direccion == null) {
            throw new DatosInvalidosException("Los datos del cliente no son válidos");
        }

        if (cliente.getNombre() == null || cliente.getNombre().isBlank()
                || cliente.getEmail() == null || cliente.getEmail().isBlank()) {
            throw new DatosInvalidosException("El nombre y el email del cliente son obligatorios");
        }

        try (Connection conexion = conexionBD.obtenerConexion()) {

            conexion.setAutoCommit(false);

            try {
                ClienteDAO clienteDAO = new ClienteDAO(conexion);
                DireccionDAO direccionDAO = new DireccionDAO(conexion);

                int idCliente = clienteDAO.insertarConId(cliente);

                direccion.setIdCliente(idCliente);
                direccionDAO.insertar(direccion);

                conexion.commit();
                log.info("Cliente registrado id={}", idCliente);

                return idCliente;

            } catch (SQLException e) {
                log.error("Error técnico al registrar cliente: SQLState={} codigo={} mensaje={}",
                        e.getSQLState(), e.getErrorCode(), e.getMessage());
                conexion.rollback();
                log.info("Rollback completado");
                throw e;

            } catch (RuntimeException e) {
                conexion.rollback();
                throw e;
            }
        }
    }

    public Cliente buscarPorEmail(String email) throws SQLException {

        try (Connection conexion = conexionBD.obtenerConexion()) {

            for (Cliente cliente : new ClienteDAO(conexion).listarTodos()) {
                if (cliente.getEmail().equals(email)) {
                    return cliente;
                }
            }
        }

        throw new DatosInvalidosException("No existe el cliente con email " + email);
    }

    public Direccion primeraDireccion(int idCliente) throws SQLException {

        try (Connection conexion = conexionBD.obtenerConexion()) {

            for (Direccion direccion : new DireccionDAO(conexion).listarTodos()) {
                if (direccion.getIdCliente() == idCliente) {
                    return direccion;
                }
            }
        }

        throw new DatosInvalidosException("El cliente " + idCliente + " no tiene direcciones");
    }
}