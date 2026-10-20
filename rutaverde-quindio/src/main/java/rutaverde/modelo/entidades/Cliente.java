package rutaverde.modelo.entidades;

import rutaverde.modelo.excepciones.ReglaNegocioException;

public class Cliente {

    private final String documentoIdentidad;
    private final String nombreCompleto;
    private final String telefono;
    private final String correo;
    private final String paisOrigen;
    private final String idiomaPreferido;

    public Cliente(String documentoIdentidad, String nombreCompleto, String telefono,
                   String correo, String paisOrigen, String idiomaPreferido) {
        exigirTexto(documentoIdentidad, "El documento de identidad o pasaporte es obligatorio.");
        exigirTexto(nombreCompleto, "El nombre completo es obligatorio.");
        exigirTexto(idiomaPreferido, "El idioma preferido es obligatorio.");
        if (correo == null || !correo.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new ReglaNegocioException("El correo electrónico no es válido.");
        }
        this.documentoIdentidad = documentoIdentidad.trim();
        this.nombreCompleto = nombreCompleto;
        this.telefono = telefono;
        this.correo = correo;
        this.paisOrigen = paisOrigen;
        this.idiomaPreferido = idiomaPreferido;
    }

    private static void exigirTexto(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException(mensaje);
        }
    }

    public String getDocumentoIdentidad() { return documentoIdentidad; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getTelefono() { return telefono; }
    public String getCorreo() { return correo; }
    public String getPaisOrigen() { return paisOrigen; }
    public String getIdiomaPreferido() { return idiomaPreferido; }
}
