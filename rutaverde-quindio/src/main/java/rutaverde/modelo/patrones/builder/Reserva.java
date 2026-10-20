package rutaverde.modelo.patrones.builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import rutaverde.modelo.entidades.Actividad;
import rutaverde.modelo.entidades.Cliente;
import rutaverde.modelo.entidades.Guia;
import rutaverde.modelo.excepciones.ReglaNegocioException;
import rutaverde.modelo.patrones.composite.ProductoTuristico;
import rutaverde.modelo.patrones.decorator.ValorBase;
import rutaverde.modelo.patrones.decorator.ValorConGuia;
import rutaverde.modelo.patrones.decorator.ValorConSeguro;
import rutaverde.modelo.patrones.decorator.ValorConTransporte;
import rutaverde.modelo.patrones.decorator.ValorReserva;

public class Reserva {

    private Integer numero;
    private final Cliente cliente;
    private final ProductoTuristico producto;
    private final LocalDate fechaViaje;
    private final int viajeros;
    private final Guia guia;
    private final boolean transporte;
    private final boolean seguro;
    private final String observaciones;
    private final ValorReserva valor;
    private String autorizacionPago;

    private Reserva(Builder constructor) {
        this.cliente = constructor.cliente;
        this.producto = constructor.producto;
        this.fechaViaje = constructor.fechaViaje;
        this.viajeros = constructor.viajeros;
        this.guia = constructor.guia;
        this.transporte = constructor.transporte;
        this.seguro = constructor.seguro;
        this.observaciones = constructor.observaciones;
        this.valor = construirValor();
    }

    private ValorReserva construirValor() {
        ValorReserva construido = new ValorBase(producto, viajeros);
        if (guia != null) {
            construido = new ValorConGuia(construido, guia, producto.calcularDuracionHoras());
        }
        if (transporte) {
            construido = new ValorConTransporte(construido, viajeros);
        }
        if (seguro) {
            construido = new ValorConSeguro(construido, viajeros);
        }
        return construido;
    }

    public void asignarNumero(int numeroAsignado) {
        if (numero != null) {
            throw new ReglaNegocioException("La reserva ya tiene el número " + numero + " asignado.");
        }
        this.numero = numeroAsignado;
    }

    public void asignarAutorizacion(String autorizacion) {
        this.autorizacionPago = autorizacion;
    }

    public String getAutorizacionPago() { return autorizacionPago; }

    public boolean estaConfirmada() {
        return numero != null;
    }

    public BigDecimal calcularValor() {
        return valor.calcular();
    }

    public String detalleValor() {
        return valor.detalle();
    }

    public Integer getNumero() { return numero; }
    public Cliente getCliente() { return cliente; }
    public ProductoTuristico getProducto() { return producto; }
    public LocalDate getFechaViaje() { return fechaViaje; }
    public int getViajeros() { return viajeros; }
    public Guia getGuia() { return guia; }
    public boolean incluyeTransporte() { return transporte; }
    public boolean incluyeSeguro() { return seguro; }
    public String getObservaciones() { return observaciones; }

    public static class Builder {

        private static final int VIAJEROS_MINIMOS = 1;
        private static final int VIAJEROS_MAXIMOS = 15;

        private Cliente cliente;
        private ProductoTuristico producto;
        private LocalDate fechaViaje;
        private int viajeros;
        private Guia guia;
        private boolean transporte;
        private boolean seguro;
        private String observaciones = "";

        public Builder cliente(Cliente cliente) {
            this.cliente = cliente;
            return this;
        }

        public Builder producto(ProductoTuristico producto) {
            this.producto = producto;
            return this;
        }

        public Builder fechaViaje(LocalDate fechaViaje) {
            this.fechaViaje = fechaViaje;
            return this;
        }

        public Builder viajeros(int viajeros) {
            this.viajeros = viajeros;
            return this;
        }

        public Builder guia(Guia guia) {
            this.guia = guia;
            return this;
        }

        public Builder conTransporte(boolean transporte) {
            this.transporte = transporte;
            return this;
        }

        public Builder conSeguro(boolean seguro) {
            this.seguro = seguro;
            return this;
        }

        public Builder observaciones(String observaciones) {
            this.observaciones = observaciones;
            return this;
        }

        public Reserva build() {
            if (cliente == null) {
                throw new ReglaNegocioException("Toda reserva requiere un cliente.");
            }
            if (producto == null) {
                throw new ReglaNegocioException("Toda reserva requiere un producto turístico.");
            }
            if (fechaViaje == null) {
                throw new ReglaNegocioException("Toda reserva requiere una fecha de viaje.");
            }
            if (fechaViaje.isBefore(LocalDate.now())) {
                throw new ReglaNegocioException("La fecha de viaje no puede estar en el pasado.");
            }
            if (viajeros < VIAJEROS_MINIMOS || viajeros > VIAJEROS_MAXIMOS) {
                throw new ReglaNegocioException("El número de viajeros debe estar entre "
                        + VIAJEROS_MINIMOS + " y " + VIAJEROS_MAXIMOS + ".");
            }
            if (guia != null && !guia.habla(cliente.getIdiomaPreferido())) {
                throw new ReglaNegocioException("El guía " + guia.getNombre()
                        + " no habla el idioma preferido del cliente (" + cliente.getIdiomaPreferido() + ").");
            }
            validarProductoReservable();
            return new Reserva(this);
        }

        private void validarProductoReservable() {
            List<Actividad> actividades = producto.obtenerActividades();
            if (actividades.isEmpty()) {
                throw new ReglaNegocioException("El paquete '" + producto.obtenerNombre()
                        + "' no tiene contenido y no se puede reservar.");
            }
            for (Actividad actividad : actividades) {
                if (actividad.estaSuspendida()) {
                    throw new ReglaNegocioException("No se puede reservar '" + producto.obtenerNombre()
                            + "' porque la actividad '" + actividad.obtenerNombre() + "' está suspendida.");
                }
            }
        }
    }
}
