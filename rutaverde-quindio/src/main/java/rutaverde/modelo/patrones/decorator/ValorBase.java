package rutaverde.modelo.patrones.decorator;

import java.math.BigDecimal;
import rutaverde.modelo.patrones.composite.ProductoTuristico;
import rutaverde.modelo.servicios.FormatoMoneda;

public class ValorBase implements ValorReserva {

    private final ProductoTuristico producto;
    private final int viajeros;

    public ValorBase(ProductoTuristico producto, int viajeros) {
        this.producto = producto;
        this.viajeros = viajeros;
    }

    @Override
    public BigDecimal calcular() {
        return producto.calcularPrecioPorPersona().multiply(BigDecimal.valueOf(viajeros));
    }

    @Override
    public String detalle() {
        return String.format("%s: %d viajero(s) x %s = %s", producto.obtenerNombre(), viajeros,
                FormatoMoneda.pesos(producto.calcularPrecioPorPersona()), FormatoMoneda.pesos(calcular()));
    }
}
