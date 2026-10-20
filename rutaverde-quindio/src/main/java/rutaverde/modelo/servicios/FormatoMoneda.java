package rutaverde.modelo.servicios;

import java.math.BigDecimal;
import java.util.Locale;

public final class FormatoMoneda {

    private static final Locale COLOMBIA = Locale.forLanguageTag("es-CO");

    private FormatoMoneda() {
    }

    public static String pesos(BigDecimal valor) {
        return String.format(COLOMBIA, "$%,.0f", valor);
    }
}
