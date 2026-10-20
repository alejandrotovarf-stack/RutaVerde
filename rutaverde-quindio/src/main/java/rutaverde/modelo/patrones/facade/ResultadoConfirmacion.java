package rutaverde.modelo.patrones.facade;

import rutaverde.modelo.entidades.MotivoRechazo;
import rutaverde.modelo.patrones.builder.Reserva;

public record ResultadoConfirmacion(boolean confirmada, Reserva reserva, String voucher,
                                    MotivoRechazo motivoRechazo, String mensaje, String errorCorreo) {

    static ResultadoConfirmacion exitosa(Reserva reserva, String voucher, String errorCorreo) {
        return new ResultadoConfirmacion(true, reserva, voucher, null, "Reserva confirmada.", errorCorreo);
    }

    static ResultadoConfirmacion rechazada(Reserva reserva, MotivoRechazo motivo, String mensaje, String errorCorreo) {
        return new ResultadoConfirmacion(false, reserva, null, motivo, mensaje, errorCorreo);
    }

    public boolean correoAceptado() {
        return errorCorreo == null;
    }
}
