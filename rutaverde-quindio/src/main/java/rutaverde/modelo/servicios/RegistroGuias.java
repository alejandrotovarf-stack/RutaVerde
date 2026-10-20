package rutaverde.modelo.servicios;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import rutaverde.modelo.entidades.Guia;
import rutaverde.modelo.excepciones.ReglaNegocioException;

public class RegistroGuias {

    private final List<Guia> guias = new ArrayList<>();

    public void registrar(Guia guia) {
        boolean yaRegistrado = guias.stream()
                .anyMatch(existente -> existente.getIdentificacion().equalsIgnoreCase(guia.getIdentificacion()));
        if (yaRegistrado) {
            throw new ReglaNegocioException("Ya existe un guía registrado con la identificación "
                    + guia.getIdentificacion() + ".");
        }
        guias.add(guia);
    }

    public List<Guia> listar() {
        return Collections.unmodifiableList(guias);
    }
}
