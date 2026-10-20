package rutaverde.modelo.patrones.composite;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import rutaverde.modelo.entidades.Actividad;
import rutaverde.modelo.excepciones.ReglaNegocioException;

public class Paquete implements ProductoTuristico {

    private static final double DESCUENTO_MINIMO = 0;
    private static final double DESCUENTO_MAXIMO = 20;
    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private final String codigo;
    private final String nombre;
    private final double descuento;
    private final List<ProductoTuristico> componentes = new ArrayList<>();

    public Paquete(String codigo, String nombre, double descuento) {
        if (descuento < DESCUENTO_MINIMO || descuento > DESCUENTO_MAXIMO) {
            throw new ReglaNegocioException("El descuento del paquete debe estar entre 0 % y 20 %.");
        }
        this.codigo = codigo;
        this.nombre = nombre;
        this.descuento = descuento;
    }

    public void agregar(ProductoTuristico componente) {
        rechazarCiclo(componente);
        rechazarActividadesRepetidas(componente);
        componentes.add(componente);
    }

    private void rechazarCiclo(ProductoTuristico componente) {
        boolean formaCiclo = componente == this
                || (componente instanceof Paquete paquete && paquete.contieneEnProfundidad(this));
        if (formaCiclo) {
            throw new ReglaNegocioException(
                    "El paquete '" + componente.obtenerNombre() + "' no puede agregarse a '" + nombre
                            + "' porque se contendría a sí mismo.");
        }
    }

    private boolean contieneEnProfundidad(Paquete buscado) {
        for (ProductoTuristico componente : componentes) {
            if (componente == buscado) {
                return true;
            }
            if (componente instanceof Paquete paquete && paquete.contieneEnProfundidad(buscado)) {
                return true;
            }
        }
        return false;
    }

    private void rechazarActividadesRepetidas(ProductoTuristico componente) {
        Set<String> codigosIncluidos = new HashSet<>();
        for (Actividad actividad : obtenerActividades()) {
            codigosIncluidos.add(actividad.obtenerCodigo());
        }
        for (Actividad actividad : componente.obtenerActividades()) {
            if (codigosIncluidos.contains(actividad.obtenerCodigo())) {
                throw new ReglaNegocioException(
                        "La actividad '" + actividad.obtenerNombre() + "' ya está incluida en el paquete '"
                                + nombre + "'.");
            }
        }
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
        BigDecimal suma = componentes.stream()
                .map(ProductoTuristico::calcularPrecioPorPersona)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal porcentajeAPagar = BigDecimal.valueOf(100 - descuento);
        return suma.multiply(porcentajeAPagar).divide(CIEN, 2, RoundingMode.HALF_UP);
    }

    @Override
    public int calcularDuracionHoras() {
        return componentes.stream().mapToInt(ProductoTuristico::calcularDuracionHoras).sum();
    }

    @Override
    public List<Actividad> obtenerActividades() {
        List<Actividad> actividades = new ArrayList<>();
        for (ProductoTuristico componente : componentes) {
            actividades.addAll(componente.obtenerActividades());
        }
        return actividades;
    }

    public double obtenerDescuento() {
        return descuento;
    }

    public List<ProductoTuristico> obtenerComponentes() {
        return Collections.unmodifiableList(componentes);
    }
}
