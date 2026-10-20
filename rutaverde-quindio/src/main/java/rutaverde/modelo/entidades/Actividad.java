package rutaverde.modelo.entidades;

import java.math.BigDecimal;
import java.util.List;
import rutaverde.modelo.excepciones.ReglaNegocioException;
import rutaverde.modelo.patrones.composite.ProductoTuristico;

public class Actividad implements ProductoTuristico {

    private final String codigo;
    private final String nombre;
    private final String municipio;
    private final int duracionHoras;
    private final BigDecimal precioPorPersona;
    private final int cupoMaximoDia;
    private final Dificultad dificultad;
    private EstadoActividad estado;
    private String descripcion = "";
    private String rutaImagen = "";

    public Actividad(String codigo, String nombre, String municipio, int duracionHoras,
                     BigDecimal precioPorPersona, int cupoMaximoDia, Dificultad dificultad) {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaNegocioException("El nombre de la actividad es obligatorio.");
        }
        if (municipio == null || municipio.isBlank()) {
            throw new ReglaNegocioException("El municipio de la actividad es obligatorio.");
        }
        if (duracionHoras <= 0) {
            throw new ReglaNegocioException("La duración de la actividad debe ser mayor a cero horas.");
        }
        if (cupoMaximoDia <= 0) {
            throw new ReglaNegocioException("El cupo máximo por día debe ser mayor a cero.");
        }
        if (precioPorPersona.signum() < 0) {
            throw new ReglaNegocioException("El precio por persona no puede ser negativo.");
        }
        this.codigo = codigo;
        this.nombre = nombre;
        this.municipio = municipio;
        this.duracionHoras = duracionHoras;
        this.precioPorPersona = precioPorPersona;
        this.cupoMaximoDia = cupoMaximoDia;
        this.dificultad = dificultad;
        this.estado = EstadoActividad.DISPONIBLE;
    }

    public void suspender() {
        estado = EstadoActividad.SUSPENDIDA;
    }

    public void habilitar() {
        estado = EstadoActividad.DISPONIBLE;
    }

    public boolean estaSuspendida() {
        return estado == EstadoActividad.SUSPENDIDA;
    }

    @Override
    public String obtenerCodigo() {
        return codigo;
    }

    @Override
    public String obtenerNombre() {
        return nombre;
    }

    @Override
    public BigDecimal calcularPrecioPorPersona() {
        return precioPorPersona;
    }

    @Override
    public int calcularDuracionHoras() {
        return duracionHoras;
    }

    @Override
    public List<Actividad> obtenerActividades() {
        return List.of(this);
    }

    public void establecerDescripcion(String descripcion) {
        this.descripcion = descripcion == null ? "" : descripcion.trim();
    }

    public void establecerRutaImagen(String rutaImagen) {
        this.rutaImagen = rutaImagen == null ? "" : rutaImagen.trim();
    }

    public String getDescripcion() { return descripcion; }
    public String getRutaImagen() { return rutaImagen; }
    public String getMunicipio() { return municipio; }
    public int getCupoMaximoDia() { return cupoMaximoDia; }
    public Dificultad getDificultad() { return dificultad; }
    public EstadoActividad getEstado() { return estado; }
}
