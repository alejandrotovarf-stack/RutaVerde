package rutaverde.modelo.persistencia;

import java.util.List;
import rutaverde.modelo.entidades.Actividad;

public interface RepositorioActividades {

    List<Actividad> cargar();

    void guardar(List<Actividad> actividades);
}
