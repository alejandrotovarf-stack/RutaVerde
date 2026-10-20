package rutaverde.aplicacion;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
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
import rutaverde.modelo.persistencia.RepositorioActividadesArchivo;
import rutaverde.modelo.servicios.ControlCupos;
import rutaverde.modelo.servicios.RegistroActividades;
import rutaverde.modelo.servicios.RegistroClientes;
import rutaverde.modelo.servicios.RegistroGuias;
import rutaverde.modelo.servicios.RegistroReservas;
import rutaverde.modelo.servicios.ReporteReservas;
import rutaverde.modelo.servicios.ServicioCorreo;
import rutaverde.modelo.servicios.ServicioCorreoNoConfigurado;
import rutaverde.modelo.servicios.ServicioCorreoSmtp;

public class ContextoAplicacion {

    public final AgenciaRutaVerde agencia = new AgenciaRutaVerde("Ruta Verde", "901.234.567-8", "RNT-123456",
            "Calle 21 # 14-30, Armenia, Quindío", "606 7410000", "reservas@rutaverde.co", "www.rutaverde.co");
    public final RegistroClientes registroClientes = new RegistroClientes();
    public final RegistroGuias registroGuias = new RegistroGuias();
    public final RegistroActividades registroActividades = new RegistroActividades(
            new RepositorioActividadesArchivo(Path.of(System.getProperty("user.home"), ".rutaverde", "actividades.tsv")));
    public final List<Paquete> paquetes = new ArrayList<>();
    public final RegistroReservas registro = new RegistroReservas();
    public final ReporteReservas reporte = new ReporteReservas(registro);
    public final EmisorVoucher emisorAsesor = new EmisorVoucherAsesor(agencia);
    public final ConfirmadorReserva confirmador = new ConfirmadorReserva(new ControlCupos(),
            new ProxyPasarelaPago(new AdaptadorPagoAndino(new PagoAndinoAPI())), registro, crearServicioCorreo());

    public ContextoAplicacion() {
        sembrarActividadesIniciales();
        armarPaquetes();
        registroClientes.registrar(new Cliente("P-998877", "Emma Smith", "+1 555 0101", "emma.smith@correo.com", "Reino Unido", "Inglés"));
        registroClientes.registrar(new Cliente("1094567890", "Camilo Rojas", "3001234567", "camilo.rojas@correo.com", "Colombia", "Español"));
        registroGuias.registrar(new Guia("G-01", "Laura", List.of("Español", "Inglés"), "3001112233", pesos(120_000)));
        registroGuias.registrar(new Guia("G-02", "Pedro", List.of("Español"), "3004445566", pesos(110_000)));
    }

    public List<ProductoTuristico> productosDisponibles() {
        List<ProductoTuristico> productos = new ArrayList<>(registroActividades.listar());
        productos.addAll(paquetes);
        return productos;
    }

    private void sembrarActividadesIniciales() {
        sembrar("ACT-01", "Valle del Cocora", "Salento", 4, 80_000, 20, Dificultad.MEDIA,
                "Caminata entre palmas de cera, paisajes únicos y biodiversidad.");
        sembrar("ACT-02", "Finca cafetera", "Salento", 3, 60_000, 6, Dificultad.BAJA,
                "Recorrido por cultivos de café, proceso tradicional y degustación.");
        sembrar("ACT-03", "Avistamiento de aves", "Filandia", 5, 50_000, 20, Dificultad.BAJA,
                "Observa la increíble diversidad de aves del Eje Cafetero con guías especializados.");
        sembrar("ACT-04", "Parque del Café", "Montenegro", 8, 100_000, 50, Dificultad.BAJA,
                "Atracciones, espectáculos y cultura cafetera en un solo lugar.");
        sembrar("ACT-05", "PANACA", "Quimbaya", 6, 90_000, 50, Dificultad.BAJA,
                "Un encuentro cercano con el campo, los animales y la vida rural.");
    }

    private void sembrar(String codigo, String nombre, String municipio, int horas, long precio, int cupo,
                         Dificultad dificultad, String descripcion) {
        if (registroActividades.buscar(codigo).isEmpty()) {
            Actividad actividad = new Actividad(codigo, nombre, municipio, horas, pesos(precio), cupo, dificultad);
            actividad.establecerDescripcion(descripcion);
            registroActividades.registrar(actividad);
        }
    }

    private void armarPaquetes() {
        Actividad cocora = registroActividades.buscar("ACT-01").orElseThrow();
        Actividad finca = registroActividades.buscar("ACT-02").orElseThrow();
        Actividad aves = registroActividades.buscar("ACT-03").orElseThrow();
        Actividad parque = registroActividades.buscar("ACT-04").orElseThrow();
        Actividad panaca = registroActividades.buscar("ACT-05").orElseThrow();
        Paquete salento = new Paquete("PAQ-01", "Paquete Salento", 10);
        salento.agregar(cocora);
        salento.agregar(finca);
        Paquete parques = new Paquete("PAQ-02", "Paquete Parques", 5);
        parques.agregar(parque);
        parques.agregar(panaca);
        Paquete total = new Paquete("PAQ-03", "Quindío Total", 10);
        total.agregar(salento);
        total.agregar(parques);
        total.agregar(aves);
        paquetes.addAll(List.of(salento, parques, total));
    }

    private static ServicioCorreo crearServicioCorreo() {
        String servidor = System.getenv("RUTAVERDE_SMTP_SERVIDOR");
        String usuario = System.getenv("RUTAVERDE_SMTP_USUARIO");
        String clave = System.getenv("RUTAVERDE_SMTP_CLAVE");
        if (servidor == null || servidor.isBlank() || usuario == null || usuario.isBlank() || clave == null || clave.isBlank()) {
            return new ServicioCorreoNoConfigurado();
        }
        int puerto = Integer.parseInt(System.getenv().getOrDefault("RUTAVERDE_SMTP_PUERTO", "587"));
        return new ServicioCorreoSmtp(servidor, puerto, usuario, clave);
    }

    private static BigDecimal pesos(long valor) {
        return BigDecimal.valueOf(valor);
    }
}
