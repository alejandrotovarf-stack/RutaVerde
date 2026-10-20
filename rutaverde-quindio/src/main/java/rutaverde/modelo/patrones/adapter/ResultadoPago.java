package rutaverde.modelo.patrones.adapter;

import rutaverde.modelo.entidades.MotivoRechazo;

public record ResultadoPago(Estado estado, String autorizacion) {

    public enum Estado {
        APROBADO, FONDOS_INSUFICIENTES, TARJETA_INVALIDA, PASARELA_NO_DISPONIBLE
    }

    public boolean fueAprobado() {
        return estado == Estado.APROBADO;
    }

    public MotivoRechazo motivoRechazo() {
        return switch (estado) {
            case APROBADO -> null;
            case FONDOS_INSUFICIENTES -> MotivoRechazo.FONDOS_INSUFICIENTES;
            case TARJETA_INVALIDA -> MotivoRechazo.TARJETA_INVALIDA;
            case PASARELA_NO_DISPONIBLE -> MotivoRechazo.PASARELA_NO_DISPONIBLE;
        };
    }
}
