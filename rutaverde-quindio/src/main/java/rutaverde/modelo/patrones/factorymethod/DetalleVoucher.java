package rutaverde.modelo.patrones.factorymethod;

import rutaverde.modelo.patrones.builder.Reserva;

public interface DetalleVoucher {

    String generar(Reserva reserva);
}
