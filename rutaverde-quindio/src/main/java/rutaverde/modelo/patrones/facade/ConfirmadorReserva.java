package rutaverde.modelo.patrones.facade;

import rutaverde.modelo.entidades.MotivoRechazo;
import rutaverde.modelo.excepciones.ErrorEnvioCorreoException;
import rutaverde.modelo.excepciones.ReglaNegocioException;
import rutaverde.modelo.patrones.adapter.PasarelaPago;
import rutaverde.modelo.patrones.adapter.ResultadoPago;
import rutaverde.modelo.patrones.builder.Reserva;
import rutaverde.modelo.patrones.factorymethod.EmisorVoucher;
import rutaverde.modelo.patrones.singleton.ConsecutivoReserva;
import rutaverde.modelo.servicios.ControlCupos;
import rutaverde.modelo.servicios.RegistroReservas;
import rutaverde.modelo.servicios.ServicioCorreo;

public class ConfirmadorReserva {

    private final ConsecutivoReserva consecutivo = ConsecutivoReserva.getInstancia();
    private final ControlCupos controlCupos;
    private final PasarelaPago pasarelaPago;
    private final RegistroReservas registroReservas;
    private final ServicioCorreo servicioCorreo;

    public ConfirmadorReserva(ControlCupos controlCupos, PasarelaPago pasarelaPago,
                              RegistroReservas registroReservas, ServicioCorreo servicioCorreo) {
        this.controlCupos = controlCupos;
        this.pasarelaPago = pasarelaPago;
        this.registroReservas = registroReservas;
        this.servicioCorreo = servicioCorreo;
    }

    public ResultadoConfirmacion confirmar(Reserva reserva, String numeroTarjeta, EmisorVoucher emisorVoucher) {
        return confirmar(reserva, numeroTarjeta, emisorVoucher, (paso, exitoso) -> { });
    }

    public ResultadoConfirmacion confirmar(Reserva reserva, String numeroTarjeta, EmisorVoucher emisorVoucher,
                                           ObservadorConfirmacion observador) {
        try {
            controlCupos.verificarCupo(reserva.getProducto(), reserva.getFechaViaje(), reserva.getViajeros());
        } catch (ReglaNegocioException sinCupo) {
            observador.pasoTerminado(PasoConfirmacion.VERIFICAR_CUPO, false);
            return rechazar(reserva, MotivoRechazo.CUPO_INSUFICIENTE, sinCupo.getMessage());
        }
        observador.pasoTerminado(PasoConfirmacion.VERIFICAR_CUPO, true);

        ResultadoPago pago = pasarelaPago.cobrar(numeroTarjeta, reserva.calcularValor(), referenciaDe(reserva));
        if (!pago.fueAprobado()) {
            boolean tarjetaRechazada = pago.estado() == ResultadoPago.Estado.TARJETA_INVALIDA;
            observador.pasoTerminado(PasoConfirmacion.VALIDAR_TARJETA, !tarjetaRechazada);
            if (!tarjetaRechazada) {
                observador.pasoTerminado(PasoConfirmacion.COBRAR, false);
            }
            MotivoRechazo motivo = pago.motivoRechazo();
            return rechazar(reserva, motivo, motivo.getDescripcion());
        }
        observador.pasoTerminado(PasoConfirmacion.VALIDAR_TARJETA, true);
        observador.pasoTerminado(PasoConfirmacion.COBRAR, true);
        reserva.asignarAutorizacion(pago.autorizacion());

        controlCupos.descontar(reserva.getProducto(), reserva.getFechaViaje(), reserva.getViajeros());
        observador.pasoTerminado(PasoConfirmacion.DESCONTAR_CUPOS, true);
        reserva.asignarNumero(consecutivo.siguienteNumero());
        registroReservas.registrar(reserva);
        observador.pasoTerminado(PasoConfirmacion.REGISTRAR_RESERVA, true);
        String voucher = emisorVoucher.emitir(reserva);
        observador.pasoTerminado(PasoConfirmacion.EMITIR_VOUCHER, true);
        String errorCorreo = enviarCorreo(reserva.getCliente().getCorreo(),
                "Reserva confirmada No. " + reserva.getNumero(), voucher);
        observador.pasoTerminado(PasoConfirmacion.ENVIAR_CORREO, errorCorreo == null);
        return ResultadoConfirmacion.exitosa(reserva, voucher, errorCorreo);
    }

    private ResultadoConfirmacion rechazar(Reserva reserva, MotivoRechazo motivo, String mensaje) {
        String errorCorreo = enviarCorreo(reserva.getCliente().getCorreo(), "Reserva no confirmada",
                "No fue posible confirmar su reserva. Motivo: " + mensaje);
        return ResultadoConfirmacion.rechazada(reserva, motivo, mensaje, errorCorreo);
    }

    private String enviarCorreo(String destinatario, String asunto, String cuerpo) {
        try {
            servicioCorreo.enviar(destinatario, asunto, cuerpo);
            return null;
        } catch (ErrorEnvioCorreoException fallo) {
            return fallo.getMessage();
        }
    }

    private String referenciaDe(Reserva reserva) {
        return "RV-" + reserva.getCliente().getDocumentoIdentidad() + "-" + reserva.getFechaViaje();
    }
}
