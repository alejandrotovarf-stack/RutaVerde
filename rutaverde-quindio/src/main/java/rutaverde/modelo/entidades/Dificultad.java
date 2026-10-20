package rutaverde.modelo.entidades;

public enum Dificultad {
    BAJA("Baja"), MEDIA("Media"), ALTA("Alta");

    private final String etiqueta;

    Dificultad(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
