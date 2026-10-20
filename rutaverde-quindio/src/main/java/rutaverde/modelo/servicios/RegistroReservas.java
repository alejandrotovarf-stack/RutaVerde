package rutaverde.modelo.servicios;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import rutaverde.modelo.entidades.Cliente;
import rutaverde.modelo.excepciones.ReglaNegocioException;
import rutaverde.modelo.patrones.builder.Reserva;

public class RegistroReservas {

    private final List<Reserva> reservasConfirmadas = new ArrayList<>();

    public void registrar(Reserva reserva) {
        if (!reserva.estaConfirmada()) {
            throw new ReglaNegocioException("Solo se registran reservas confirmadas con número asignado.");
        }
        reservasConfirmadas.add(reserva);
    }

    public List<Reserva> listar() {
        return Collections.unmodifiableList(reservasConfirmadas);
    }

    public List<Reserva> reservasDe(Cliente cliente) {
        return reservasConfirmadas.stream()
                .filter(reserva -> reserva.getCliente() == cliente)
                .toList();
    }
}
