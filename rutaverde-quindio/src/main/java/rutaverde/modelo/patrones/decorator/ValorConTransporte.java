package rutaverde.modelo.patrones.decorator;

import java.math.BigDecimal;
import rutaverde.modelo.servicios.FormatoMoneda;

public class ValorConTransporte implements ValorReserva {

    private static final BigDecimal TARIFA_POR_VIAJERO = BigDecimal.valueOf(35_000);

    private final ValorReserva envuelto;
    private final int viajeros;

    public ValorConTransporte(ValorReserva envuelto, int viajeros) {
        this.envuelto = envuelto;
        this.viajeros = viajeros;
    }

    private BigDecimal costoTransporte() {
        return TARIFA_POR_VIAJERO.multiply(BigDecimal.valueOf(viajeros));
    }

    @Override
    public BigDecimal calcular() {
        return envuelto.calcular().add(costoTransporte());
    }

    @Override
    public String detalle() {
        return envuelto.detalle() + "\n" + String.format("Transporte: %d viajero(s) x %s = %s", viajeros,
                FormatoMoneda.pesos(TARIFA_POR_VIAJERO), FormatoMoneda.pesos(costoTransporte()));
    }
}
