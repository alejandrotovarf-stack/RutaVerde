package rutaverde.modelo.entidades;

public enum MotivoRechazo {
    CUPO_INSUFICIENTE("No hay cupo suficiente para la fecha solicitada."),
    TARJETA_INVALIDA("El número de tarjeta no es válido."),
    FONDOS_INSUFICIENTES("La tarjeta no tiene fondos suficientes."),
    PASARELA_NO_DISPONIBLE("La pasarela de pagos no está disponible en este momento.");

    private final String descripcion;

    MotivoRechazo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
