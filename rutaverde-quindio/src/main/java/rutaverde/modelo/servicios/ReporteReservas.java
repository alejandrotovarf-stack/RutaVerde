package rutaverde.modelo.servicios;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import rutaverde.modelo.excepciones.ReglaNegocioException;
import rutaverde.modelo.patrones.builder.Reserva;
import rutaverde.modelo.patrones.composite.ProductoTuristico;

public class ReporteReservas {

    public record ProductoConViajeros(ProductoTuristico producto, int viajeros) {
    }

    private final RegistroReservas registro;

    public ReporteReservas(RegistroReservas registro) {
        this.registro = registro;
    }

    public BigDecimal calcularIngresos(LocalDate fechaInicial, LocalDate fechaFinal) {
        if (fechaInicial.isAfter(fechaFinal)) {
            throw new ReglaNegocioException("La fecha inicial no puede ser posterior a la fecha final.");
        }
        return registro.listar().stream()
                .filter(reserva -> !reserva.getFechaViaje().isBefore(fechaInicial))
                .filter(reserva -> !reserva.getFechaViaje().isAfter(fechaFinal))
                .map(Reserva::calcularValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Optional<ProductoConViajeros> productoConMasViajeros() {
        Map<String, ProductoConViajeros> acumulado = new LinkedHashMap<>();
        for (Reserva reserva : registro.listar()) {
            ProductoTuristico producto = reserva.getProducto();
            acumulado.merge(producto.obtenerCodigo(),
                    new ProductoConViajeros(producto, reserva.getViajeros()),
                    (anterior, nuevo) -> new ProductoConViajeros(producto, anterior.viajeros() + nuevo.viajeros()));
        }
        return acumulado.values().stream()
                .reduce((mayor, candidato) -> candidato.viajeros() > mayor.viajeros() ? candidato : mayor);
    }
}
