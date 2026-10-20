package rutaverde.modelo.patrones.adapter;

public class PagoAndinoAPI {
    private int consecutivo = 1000;

    /**
     * Devuelve 0 = aprobado, 51 = fondos insuficientes,
     * 14 = tarjeta invalida, 91 = pasarela no disponible.
     */
    public int procesarCobro(String numeroTarjeta, long valorCentavos, String referencia) {
        System.out.println("[PagoAndino] ref " + referencia + " por " + valorCentavos + " centavos");

        if (valorCentavos > 300_000_000L) {
            return 51;
        }

        consecutivo++;
        return 0;
    }

    public String ultimaAutorizacion() {
        return "PA-" + consecutivo;
    }
}
