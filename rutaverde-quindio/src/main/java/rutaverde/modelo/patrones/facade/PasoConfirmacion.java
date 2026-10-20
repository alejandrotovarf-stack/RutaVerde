package rutaverde.modelo.patrones.facade;

public enum PasoConfirmacion {
    VERIFICAR_CUPO("Verificar cupo"),
    VALIDAR_TARJETA("Validar tarjeta"),
    COBRAR("Cobrar en la pasarela"),
    DESCONTAR_CUPOS("Descontar cupos"),
    REGISTRAR_RESERVA("Registrar reserva"),
    EMITIR_VOUCHER("Emitir voucher"),
    ENVIAR_CORREO("Enviar correo");

    private final String titulo;

    PasoConfirmacion(String titulo) {
        this.titulo = titulo;
    }

    public String getTitulo() {
        return titulo;
    }
}
