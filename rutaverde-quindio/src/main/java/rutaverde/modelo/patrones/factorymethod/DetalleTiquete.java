package rutaverde.modelo.patrones.factorymethod;

import java.time.format.DateTimeFormatter;
import rutaverde.modelo.patrones.builder.Reserva;

public class DetalleTiquete implements DetalleVoucher {

    @Override
    public String generar(Reserva reserva) {
        String codigoEntrada = String.format("RV%05d-%s", reserva.getNumero(),
                reserva.getFechaViaje().format(DateTimeFormatter.BASIC_ISO_DATE));
        return String.format("TIQUETE %s | %s | %s | %d viajero(s)", codigoEntrada,
                reserva.getProducto().obtenerNombre(), reserva.getFechaViaje(), reserva.getViajeros());
    }
}
