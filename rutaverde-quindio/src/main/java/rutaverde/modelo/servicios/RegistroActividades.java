package rutaverde.modelo.servicios;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import rutaverde.modelo.entidades.Actividad;
import rutaverde.modelo.excepciones.ErrorAlmacenamientoException;
import rutaverde.modelo.excepciones.ReglaNegocioException;
import rutaverde.modelo.persistencia.RepositorioActividades;

public class RegistroActividades {

    private final RepositorioActividades repositorio;
    private final List<Actividad> actividades;

    public RegistroActividades(RepositorioActividades repositorio) {
        this.repositorio = repositorio;
        this.actividades = new ArrayList<>(repositorio.cargar());
    }

    public String siguienteCodigo() {
        int mayor = actividades.stream()
                .map(Actividad::obtenerCodigo)
                .filter(codigo -> codigo.matches("ACT-\\d+"))
                .mapToInt(codigo -> Integer.parseInt(codigo.substring(4)))
                .max().orElse(0);
        return String.format("ACT-%02d", mayor + 1);
    }

    public void registrar(Actividad nueva) {
        boolean repetida = actividades.stream().anyMatch(existente ->
                existente.obtenerCodigo().equalsIgnoreCase(nueva.obtenerCodigo())
                        || existente.obtenerNombre().equalsIgnoreCase(nueva.obtenerNombre()));
        if (repetida) {
            throw new ReglaNegocioException("Ya existe una actividad con el nombre o el código '"
                    + nueva.obtenerNombre() + "'.");
        }
        actividades.add(nueva);
        guardarOReversar(() -> actividades.remove(nueva));
    }

    public void suspender(String codigo) {
        Actividad actividad = obtener(codigo);
        actividad.suspender();
        guardarOReversar(actividad::habilitar);
    }

    public void habilitar(String codigo) {
        Actividad actividad = obtener(codigo);
        actividad.habilitar();
        guardarOReversar(actividad::suspender);
    }

    public Optional<Actividad> buscar(String codigo) {
        return actividades.stream().filter(actividad -> actividad.obtenerCodigo().equals(codigo)).findFirst();
    }

    public List<Actividad> listar() {
        return Collections.unmodifiableList(actividades);
    }

    private Actividad obtener(String codigo) {
        return buscar(codigo).orElseThrow(() -> new ReglaNegocioException("No existe la actividad " + codigo + "."));
    }

    private void guardarOReversar(Runnable reversion) {
        try {
            repositorio.guardar(actividades);
        } catch (ErrorAlmacenamientoException fallo) {
            reversion.run();
            throw fallo;
        }
    }
}
