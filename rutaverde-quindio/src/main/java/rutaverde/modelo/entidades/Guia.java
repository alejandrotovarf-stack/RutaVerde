package rutaverde.modelo.entidades;

import java.math.BigDecimal;
import java.util.List;
import rutaverde.modelo.excepciones.ReglaNegocioException;

public class Guia {

    private final String identificacion;
    private final String nombre;
    private final List<String> idiomas;
    private final String telefono;
    private final BigDecimal tarifaDia;

    public Guia(String identificacion, String nombre, List<String> idiomas,
                String telefono, BigDecimal tarifaDia) {
        if (identificacion == null || identificacion.isBlank()) {
            throw new ReglaNegocioException("La identificación del guía es obligatoria.");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaNegocioException("El nombre del guía es obligatorio.");
        }
        if (idiomas == null || idiomas.isEmpty()) {
            throw new ReglaNegocioException("El guía debe hablar al menos un idioma.");
        }
        if (tarifaDia == null || tarifaDia.signum() <= 0) {
            throw new ReglaNegocioException("La tarifa por día debe ser mayor a cero.");
        }
        this.identificacion = identificacion.trim();
        this.nombre = nombre;
        this.idiomas = List.copyOf(idiomas);
        this.telefono = telefono;
        this.tarifaDia = tarifaDia;
    }

    public boolean habla(String idioma) {
        return idiomas.stream().anyMatch(conocido -> conocido.equalsIgnoreCase(idioma));
    }

    public String getIdentificacion() { return identificacion; }
    public String getNombre() { return nombre; }
    public List<String> getIdiomas() { return idiomas; }
    public String getTelefono() { return telefono; }
    public BigDecimal getTarifaDia() { return tarifaDia; }
}
