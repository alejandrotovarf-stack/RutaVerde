package rutaverde.modelo.patrones.adapter;

import java.math.BigDecimal;

public interface PasarelaPago {

    ResultadoPago cobrar(String numeroTarjeta, BigDecimal valorPesos, String referencia);
}
