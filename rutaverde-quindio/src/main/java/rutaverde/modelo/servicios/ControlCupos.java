package rutaverde.modelo.servicios;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import rutaverde.modelo.entidades.Actividad;
import rutaverde.modelo.excepciones.ReglaNegocioException;
import rutaverde.modelo.patrones.composite.ProductoTuristico;

public class ControlCupos {

    private final Map<String, Integer> viajerosPorActividadYDia = new HashMap<>();

    public void verificarCupo(ProductoTuristico producto, LocalDate fecha, int viajeros) {
        for (Actividad actividad : producto.obtenerActividades()) {
            int disponible = cupoDisponible(actividad, fecha);
            if (viajeros > disponible) {
                throw new ReglaNegocioException(String.format(
                        "No hay cupo en '%s' para el %s: quedan %d lugar(es) y se solicitan %d.",
                        actividad.obtenerNombre(), fecha, disponible, viajeros));
            }
        }
    }

    public void descontar(ProductoTuristico producto, LocalDate fecha, int viajeros) {
        for (Actividad actividad : producto.obtenerActividades()) {
            viajerosPorActividadYDia.merge(clave(actividad, fecha), viajeros, Integer::sum);
        }
    }

    public int cupoDisponible(Actividad actividad, LocalDate fecha) {
        int ocupado = viajerosPorActividadYDia.getOrDefault(clave(actividad, fecha), 0);
        return actividad.getCupoMaximoDia() - ocupado;
    }

    private String clave(Actividad actividad, LocalDate fecha) {
        return actividad.obtenerCodigo() + "|" + fecha;
    }
}
