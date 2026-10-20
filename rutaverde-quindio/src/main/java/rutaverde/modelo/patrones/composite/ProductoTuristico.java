package rutaverde.modelo.patrones.composite;

import java.math.BigDecimal;
import java.util.List;
import rutaverde.modelo.entidades.Actividad;

public interface ProductoTuristico {

    String obtenerCodigo();

    String obtenerNombre();

    BigDecimal calcularPrecioPorPersona();

    int calcularDuracionHoras();

    List<Actividad> obtenerActividades();
}
