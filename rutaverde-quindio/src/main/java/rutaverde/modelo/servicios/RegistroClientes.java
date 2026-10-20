package rutaverde.modelo.servicios;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import rutaverde.modelo.entidades.Cliente;
import rutaverde.modelo.excepciones.ReglaNegocioException;

public class RegistroClientes {

    private final List<Cliente> clientes = new ArrayList<>();

    public void registrar(Cliente cliente) {
        boolean yaRegistrado = clientes.stream()
                .anyMatch(existente -> existente.getDocumentoIdentidad().equalsIgnoreCase(cliente.getDocumentoIdentidad()));
        if (yaRegistrado) {
            throw new ReglaNegocioException("Ya existe un turista registrado con el documento "
                    + cliente.getDocumentoIdentidad() + ".");
        }
        clientes.add(cliente);
    }

    public List<Cliente> listar() {
        return Collections.unmodifiableList(clientes);
    }
}
