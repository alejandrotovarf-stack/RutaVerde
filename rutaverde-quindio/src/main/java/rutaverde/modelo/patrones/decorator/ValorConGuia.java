package rutaverde.modelo.patrones.decorator;

import java.math.BigDecimal;
import rutaverde.modelo.entidades.Guia;
import rutaverde.modelo.servicios.FormatoMoneda;

public class ValorConGuia implements ValorReserva {

    private static final int HORAS_POR_DIA_DE_GUIA = 8;

    private final ValorReserva envuelto;
    private final Guia guia;
    private final int diasDeGuia;

    public ValorConGuia(ValorReserva envuelto, Guia guia, int duracionTotalHoras) {
        this.envuelto = envuelto;
        this.guia = guia;
        this.diasDeGuia = (duracionTotalHoras + HORAS_POR_DIA_DE_GUIA - 1) / HORAS_POR_DIA_DE_GUIA;
    }

    private BigDecimal costoGuia() {
        return guia.getTarifaDia().multiply(BigDecimal.valueOf(diasDeGuia));
    }

    @Override
    public BigDecimal calcular() {
        return envuelto.calcular().add(costoGuia());
    }

    @Override
    public String detalle() {
        return envuelto.detalle() + "\n" + String.format("Guía %s: %d día(s) x %s = %s",
                guia.getNombre(), diasDeGuia, FormatoMoneda.pesos(guia.getTarifaDia()),
                FormatoMoneda.pesos(costoGuia()));
    }
}
