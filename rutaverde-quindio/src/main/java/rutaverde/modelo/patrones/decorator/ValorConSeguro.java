package rutaverde.modelo.patrones.decorator;

import java.math.BigDecimal;
import rutaverde.modelo.servicios.FormatoMoneda;

public class ValorConSeguro implements ValorReserva {

    private static final BigDecimal TARIFA_POR_VIAJERO = BigDecimal.valueOf(15_000);

    private final ValorReserva envuelto;
    private final int viajeros;

    public ValorConSeguro(ValorReserva envuelto, int viajeros) {
        this.envuelto = envuelto;
        this.viajeros = viajeros;
    }

    private BigDecimal costoSeguro() {
        return TARIFA_POR_VIAJERO.multiply(BigDecimal.valueOf(viajeros));
    }

    @Override
    public BigDecimal calcular() {
        return envuelto.calcular().add(costoSeguro());
    }

    @Override
    public String detalle() {
        return envuelto.detalle() + "\n" + String.format("Seguro: %d viajero(s) x %s = %s", viajeros,
                FormatoMoneda.pesos(TARIFA_POR_VIAJERO), FormatoMoneda.pesos(costoSeguro()));
    }
}
