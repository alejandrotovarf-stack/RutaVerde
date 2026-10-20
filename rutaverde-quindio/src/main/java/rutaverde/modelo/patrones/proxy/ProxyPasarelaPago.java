package rutaverde.modelo.patrones.proxy;

import java.math.BigDecimal;
import rutaverde.modelo.patrones.adapter.PasarelaPago;
import rutaverde.modelo.patrones.adapter.ResultadoPago;

public class ProxyPasarelaPago implements PasarelaPago {

    private final PasarelaPago pasarelaReal;

    public ProxyPasarelaPago(PasarelaPago pasarelaReal) {
        this.pasarelaReal = pasarelaReal;
    }

    @Override
    public ResultadoPago cobrar(String numeroTarjeta, BigDecimal valorPesos, String referencia) {
        if (!ValidadorLuhn.esValida(numeroTarjeta)) {
            return new ResultadoPago(ResultadoPago.Estado.TARJETA_INVALIDA, null);
        }
        return pasarelaReal.cobrar(numeroTarjeta, valorPesos, referencia);
    }
}
