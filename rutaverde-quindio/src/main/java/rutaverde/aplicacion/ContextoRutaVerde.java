package rutaverde.aplicacion;

import java.math.BigDecimal;
import java.util.List;
import rutaverde.modelo.entidades.Actividad;
import rutaverde.modelo.entidades.AgenciaRutaVerde;
import rutaverde.modelo.entidades.Cliente;
import rutaverde.modelo.entidades.Dificultad;
import rutaverde.modelo.entidades.Guia;
import rutaverde.modelo.patrones.adapter.AdaptadorPagoAndino;
import rutaverde.modelo.patrones.adapter.PagoAndinoAPI;
import rutaverde.modelo.patrones.composite.Paquete;
import rutaverde.modelo.patrones.composite.ProductoTuristico;
import rutaverde.modelo.patrones.facade.ConfirmadorReserva;
import rutaverde.modelo.patrones.factorymethod.EmisorVoucher;
import rutaverde.modelo.patrones.factorymethod.EmisorVoucherAsesor;
import rutaverde.modelo.patrones.proxy.ProxyPasarelaPago;
import rutaverde.modelo.servicios.ControlCupos;
import rutaverde.modelo.servicios.RegistroReservas;
import rutaverde.modelo.servicios.ReporteReservas;
import rutaverde.modelo.servicios.ServicioCorreoConsola;

public class ContextoRutaVerde {

    public final AgenciaRutaVerde agencia = new AgenciaRutaVerde("RutaVerde Quindío", "901.234.567-8",
            "RNT-123456", "Calle 21 # 14-30, Armenia, Quindío", "606 7410000", "reservas@rutaverde.co", "www.rutaverde.co");
    public final RegistroReservas registro = new RegistroReservas();
    public final ReporteReservas reporte = new ReporteReservas(registro);
    public final ConfirmadorReserva confirmador;
    public final EmisorVoucher emisorAsesor = new EmisorVoucherAsesor(agencia);
    public final List<Cliente> clientes;
    public final List<Guia> guias;
    public final List<ProductoTuristico> productos;

    public ContextoRutaVerde() {
        confirmador = new ConfirmadorReserva(new ControlCupos(),
                new ProxyPasarelaPago(new AdaptadorPagoAndino(new PagoAndinoAPI())), registro, new ServicioCorreoConsola());
        Actividad cocora = actividad("ACT-01", "Valle del Cocora", "Salento", 4, 80_000, 20);
        Actividad finca = actividad("ACT-02", "Finca cafetera", "Salento", 3, 60_000, 6);
        Actividad aves = actividad("ACT-03", "Avistamiento de aves", "Filandia", 5, 50_000, 20);
        Actividad parque = actividad("ACT-04", "Parque del Café", "Montenegro", 8, 100_000, 50);
        Actividad panaca = actividad("ACT-05", "PANACA", "Quimbaya", 6, 90_000, 50);
        Paquete salento = paquete("PAQ-01", "Paquete Salento", 10, cocora, finca);
        Paquete parques = paquete("PAQ-02", "Paquete Parques", 5, parque, panaca);
        Paquete total = paquete("PAQ-03", "Quindío Total", 10, salento, parques, aves);
        productos = List.of(cocora, finca, aves, parque, panaca, salento, parques, total);
        guias = List.of(new Guia("G-01", "Laura", List.of("Español", "Inglés"), "3001112233", BigDecimal.valueOf(120_000)));
        clientes = List.of(new Cliente("P-998877", "Emma Smith", "+1 555 0101", "emma.smith@correo.com", "Reino Unido", "Inglés"),
                new Cliente("1094000111", "Carlos Ramírez", "3105550000", "carlos@correo.com", "Colombia", "Español"));
    }

    private Actividad actividad(String codigo, String nombre, String municipio, int horas, long precio, int cupo) {
        return new Actividad(codigo, nombre, municipio, horas, BigDecimal.valueOf(precio), cupo, Dificultad.MEDIA);
    }

    private Paquete paquete(String codigo, String nombre, double descuento, ProductoTuristico... componentes) {
        Paquete paquete = new Paquete(codigo, nombre, descuento);
        for (ProductoTuristico componente : componentes) {
            paquete.agregar(componente);
        }
        return paquete;
    }
}
