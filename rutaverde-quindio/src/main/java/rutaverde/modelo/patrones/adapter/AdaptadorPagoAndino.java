package rutaverde.modelo.patrones.adapter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import rutaverde.modelo.excepciones.ReglaNegocioException;
import rutaverde.modelo.patrones.adapter.ResultadoPago.Estado;

public class AdaptadorPagoAndino implements PasarelaPago {

    private static final int CODIGO_APROBADO = 0;
    private static final int CODIGO_FONDOS_INSUFICIENTES = 51;
    private static final int CODIGO_TARJETA_INVALIDA = 14;
    private static final int CODIGO_PASARELA_NO_DISPONIBLE = 91;

    private final PagoAndinoAPI pagoAndino;

    public AdaptadorPagoAndino(PagoAndinoAPI pagoAndino) {
        this.pagoAndino = pagoAndino;
    }

    @Override
    public synchronized ResultadoPago cobrar(String numeroTarjeta, BigDecimal valorPesos, String referencia) {
        long valorCentavos;
        try {
            valorCentavos = valorPesos.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
        } catch (ArithmeticException fueraDeRango) {
            throw new ReglaNegocioException("El importe a cobrar está fuera del rango admitido por la pasarela.");
        }
        Estado estado = traducir(pagoAndino.procesarCobro(numeroTarjeta, valorCentavos, referencia));
        String autorizacion = estado == Estado.APROBADO ? pagoAndino.ultimaAutorizacion() : null;
        return new ResultadoPago(estado, autorizacion);
    }

    private Estado traducir(int codigo) {
        return switch (codigo) {
            case CODIGO_APROBADO -> Estado.APROBADO;
            case CODIGO_FONDOS_INSUFICIENTES -> Estado.FONDOS_INSUFICIENTES;
            case CODIGO_TARJETA_INVALIDA -> Estado.TARJETA_INVALIDA;
            case CODIGO_PASARELA_NO_DISPONIBLE -> Estado.PASARELA_NO_DISPONIBLE;
            default -> Estado.PASARELA_NO_DISPONIBLE;
        };
    }
}
