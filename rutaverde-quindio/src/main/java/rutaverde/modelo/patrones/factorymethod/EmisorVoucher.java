package rutaverde.modelo.patrones.factorymethod;

import rutaverde.modelo.entidades.AgenciaRutaVerde;
import rutaverde.modelo.patrones.builder.Reserva;

public abstract class EmisorVoucher {

    private static final String PIE = "Cancelación sin costo hasta 48 horas antes del viaje.";

    private final AgenciaRutaVerde agencia;

    protected EmisorVoucher(AgenciaRutaVerde agencia) {
        this.agencia = agencia;
    }

    public final String emitir(Reserva reserva) {
        return String.join("\n", encabezado(), crearDetalle().generar(reserva), PIE);
    }

    protected abstract DetalleVoucher crearDetalle();

    private String encabezado() {
        return agencia.getNombreComercial() + "\nRNT: " + agencia.getRnt();
    }
}
