package rutaverde.modelo.patrones.factorymethod;

import java.util.ArrayList;
import java.util.List;
import rutaverde.modelo.entidades.Actividad;
import rutaverde.modelo.patrones.builder.Reserva;
import rutaverde.modelo.servicios.FormatoMoneda;

public class DetalleCompleto implements DetalleVoucher {

    @Override
    public String generar(Reserva reserva) {
        List<String> lineas = new ArrayList<>();
        lineas.add("Reserva No. " + reserva.getNumero());
        lineas.add("Cliente: " + reserva.getCliente().getNombreCompleto());
        lineas.add("Fecha de viaje: " + reserva.getFechaViaje());
        lineas.add("Producto: " + reserva.getProducto().obtenerNombre());
        lineas.add("Viajeros: " + reserva.getViajeros());
        for (Actividad actividad : reserva.getProducto().obtenerActividades()) {
            lineas.add(String.format("  - %s (%s, %d h)", actividad.obtenerNombre(),
                    actividad.getMunicipio(), actividad.calcularDuracionHoras()));
        }
        if (reserva.getGuia() != null) {
            lineas.add("Guía: " + reserva.getGuia().getNombre());
        }
        lineas.add("Servicios incluidos: " + describirServicios(reserva));
        lineas.add(reserva.detalleValor());
        if (reserva.getAutorizacionPago() != null) {
            lineas.add("Autorización de pago: " + reserva.getAutorizacionPago());
        }
        lineas.add("TOTAL: " + FormatoMoneda.pesos(reserva.calcularValor()));
        return String.join("\n", lineas);
    }

    private String describirServicios(Reserva reserva) {
        List<String> servicios = new ArrayList<>();
        if (reserva.incluyeTransporte()) {
            servicios.add("transporte");
        }
        if (reserva.incluyeSeguro()) {
            servicios.add("seguro");
        }
        return servicios.isEmpty() ? "ninguno" : String.join(", ", servicios);
    }
}
