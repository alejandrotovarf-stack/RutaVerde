package rutaverde.aplicacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import rutaverde.modelo.entidades.Actividad;
import rutaverde.modelo.entidades.AgenciaRutaVerde;
import rutaverde.modelo.entidades.Cliente;
import rutaverde.modelo.entidades.Dificultad;
import rutaverde.modelo.entidades.Guia;
import rutaverde.modelo.entidades.MotivoRechazo;
import rutaverde.modelo.excepciones.ReglaNegocioException;
import rutaverde.modelo.patrones.adapter.AdaptadorPagoAndino;
import rutaverde.modelo.patrones.adapter.PagoAndinoAPI;
import rutaverde.modelo.patrones.adapter.PasarelaPago;
import rutaverde.modelo.patrones.adapter.ResultadoPago;
import rutaverde.modelo.patrones.builder.Reserva;
import rutaverde.modelo.patrones.composite.Paquete;
import rutaverde.modelo.patrones.composite.ProductoTuristico;
import rutaverde.modelo.patrones.facade.ConfirmadorReserva;
import rutaverde.modelo.patrones.facade.ResultadoConfirmacion;
import rutaverde.modelo.patrones.factorymethod.EmisorVoucher;
import rutaverde.modelo.patrones.factorymethod.EmisorVoucherAsesor;
import rutaverde.modelo.patrones.factorymethod.EmisorVoucherKiosco;
import rutaverde.modelo.patrones.proxy.ProxyPasarelaPago;
import rutaverde.modelo.patrones.proxy.ValidadorLuhn;
import rutaverde.modelo.excepciones.ErrorAlmacenamientoException;
import rutaverde.modelo.excepciones.ErrorEnvioCorreoException;
import rutaverde.modelo.persistencia.RepositorioActividadesArchivo;
import rutaverde.modelo.servicios.ControlCupos;
import rutaverde.modelo.servicios.RegistroActividades;
import rutaverde.modelo.servicios.ServicioCorreo;
import rutaverde.modelo.servicios.FormatoMoneda;
import rutaverde.modelo.servicios.FormatoExportacion;
import rutaverde.modelo.servicios.RegistroClientes;
import rutaverde.modelo.servicios.RegistroGuias;
import rutaverde.modelo.servicios.RegistroReservas;
import rutaverde.modelo.servicios.ReporteReservas;
import rutaverde.modelo.servicios.ServicioCorreoConsola;

public class MainConsola {

    private static final String TARJETA_VALIDA = "4539 1488 0343 6467";
    private static final String TARJETA_INVALIDA = "4539 1488 0343 6468";

    private static int pruebasAprobadas;
    private static int pruebasFallidas;

    public static void main(String[] args) {
        DatosPrueba datos = new DatosPrueba();
        probarNumeracionConsecutiva(datos);
        probarComposite(datos);
        probarReglasDePaquetes(datos);
        probarBuilder(datos);
        probarDecorator(datos);
        probarLuhn();
        probarAdaptadorYProxy();
        probarCupos(datos);
        probarFondosInsuficientes(datos);
        probarCanales(datos);
        probarReportes(datos);
        probarRegistrosYExportacion(datos);
        probarPagosYAutorizacion(datos);
        probarActividadesPersistentesYRn05(datos);
        probarFalloDeCorreo(datos);
        System.out.println();
        System.out.println("Resultado final: " + pruebasAprobadas + " verificaciones correctas, "
                + pruebasFallidas + " fallidas.");
    }

    private static void probarNumeracionConsecutiva(DatosPrueba datos) {
        titulo("Numeración consecutiva: dos reservas válidas con una inválida en medio");
        Escenario escenario = Escenario.conPagoAndino(new PagoAndinoAPIDePrueba(), datos);
        LocalDate fecha = datos.fechaBase.plusDays(60);

        ResultadoConfirmacion primera = escenario.confirmarComoAsesor(
                construirReserva(datos.emma, datos.cocora, fecha, 2), TARJETA_VALIDA);
        ResultadoConfirmacion invalida = escenario.confirmarComoAsesor(
                construirReserva(datos.emma, datos.cocora, fecha, 2), TARJETA_INVALIDA);
        ResultadoConfirmacion segunda = escenario.confirmarComoAsesor(
                construirReserva(datos.emma, datos.cocora, fecha, 2), TARJETA_VALIDA);

        verificar("primera reserva recibe el número 1", primera.reserva().getNumero() == 1);
        verificar("reserva con tarjeta inválida rechazada y sin número",
                !invalida.confirmada() && invalida.reserva().getNumero() == null);
        verificar("segunda reserva válida recibe el número 2", segunda.reserva().getNumero() == 2);
    }

    private static void probarComposite(DatosPrueba datos) {
        titulo("Composite: consulta uniforme de productos");
        describir(datos.cocora);
        describir(datos.salento);
        describir(datos.parques);
        describir(datos.quindioTotal);
        verificar("Salento cuesta $126.000, dura 7 h y tiene 2 actividades",
                esIgual(datos.salento.calcularPrecioPorPersona(), 126_000)
                        && datos.salento.calcularDuracionHoras() == 7
                        && datos.salento.obtenerActividades().size() == 2);
        verificar("Parques cuesta $180.500", esIgual(datos.parques.calcularPrecioPorPersona(), 180_500));
        verificar("Quindío Total cuesta $320.850, dura 26 h y tiene 5 actividades",
                esIgual(datos.quindioTotal.calcularPrecioPorPersona(), 320_850)
                        && datos.quindioTotal.calcularDuracionHoras() == 26
                        && datos.quindioTotal.obtenerActividades().size() == 5);
    }

    private static void probarReglasDePaquetes(DatosPrueba datos) {
        titulo("Paquetes: descuentos, ciclos y duplicados");
        esperarRechazo("descuento de 25 %", () -> new Paquete("X", "Descuento excesivo", 25));
        esperarRechazo("descuento de -1 %", () -> new Paquete("X", "Descuento negativo", -1));
        esperarAceptacion("descuento de 0 %", () -> new Paquete("X", "Sin descuento", 0));
        esperarAceptacion("descuento de 20 %", () -> new Paquete("X", "Descuento máximo", 20));
        esperarRechazo("Salento dentro de sí mismo", () -> datos.salento.agregar(datos.salento));
        esperarRechazo("Quindío Total dentro de Salento", () -> datos.salento.agregar(datos.quindioTotal));
        esperarRechazo("Cocora suelta dentro de Quindío Total", () -> datos.quindioTotal.agregar(datos.cocora));
    }

    private static void probarBuilder(DatosPrueba datos) {
        titulo("Builder: validaciones de la reserva");
        LocalDate fecha = datos.fechaBase;
        esperarRechazo("reserva sin cliente", () -> new Reserva.Builder()
                .producto(datos.salento).fechaViaje(fecha).viajeros(2).build());
        esperarRechazo("reserva sin producto", () -> new Reserva.Builder()
                .cliente(datos.emma).fechaViaje(fecha).viajeros(2).build());
        esperarRechazo("reserva con fecha pasada", () -> construirReserva(
                datos.emma, datos.salento, LocalDate.now().minusDays(1), 2));
        esperarRechazo("reserva con 0 viajeros", () -> construirReserva(datos.emma, datos.salento, fecha, 0));
        esperarRechazo("reserva con 16 viajeros", () -> construirReserva(datos.emma, datos.salento, fecha, 16));
        esperarRechazo("guía que no habla el idioma del cliente", () -> new Reserva.Builder()
                .cliente(datos.emma).producto(datos.salento).fechaViaje(fecha).viajeros(2)
                .guia(datos.pedroSoloEspanol).build());
        esperarRechazo("producto con actividad suspendida", () -> construirReserva(
                datos.emma, datos.paqueteConActividadSuspendida, fecha, 2));
        esperarRechazo("paquete sin contenido", () -> construirReserva(
                datos.emma, new Paquete("VAC", "Paquete vacío", 0), fecha, 2));
        esperarAceptacion("reserva con 1 viajero", () -> construirReserva(datos.emma, datos.salento, fecha, 1));
        esperarAceptacion("reserva con 15 viajeros", () -> construirReserva(datos.emma, datos.salento, fecha, 15));
    }

    private static void probarDecorator(DatosPrueba datos) {
        titulo("Decorator: valor de la reserva");
        Reserva sinOpcionales = construirReserva(datos.emma, datos.quindioTotal, datos.fechaBase, 3);
        System.out.println(sinOpcionales.detalleValor());
        verificar("Quindío Total, 3 viajeros, sin opcionales = $962.550",
                esIgual(sinOpcionales.calcularValor(), 962_550));

        Reserva conOpcionales = new Reserva.Builder().cliente(datos.emma).producto(datos.quindioTotal)
                .fechaViaje(datos.fechaBase).viajeros(3).guia(datos.laura)
                .conTransporte(true).conSeguro(true).build();
        System.out.println(conOpcionales.detalleValor());
        verificar("con guía (4 días), transporte y seguro = $1.592.550",
                esIgual(conOpcionales.calcularValor(), 1_592_550));
    }

    private static void probarLuhn() {
        titulo("Luhn: validación de tarjetas");
        verificar("4539 1488 0343 6467 es válida", ValidadorLuhn.esValida(TARJETA_VALIDA));
        verificar("4539 1488 0343 6468 es inválida", !ValidadorLuhn.esValida(TARJETA_INVALIDA));
        verificar("12 dígitos es inválida", !ValidadorLuhn.esValida("453914880343"));
        verificar("20 dígitos es inválida", !ValidadorLuhn.esValida("45391488034364670000"));
        verificar("con letras es inválida", !ValidadorLuhn.esValida("4539 1488 0343 64AB"));
    }

    private static void probarAdaptadorYProxy() {
        titulo("Adapter y Proxy: pasarela de pagos");
        PagoAndinoAPIDePrueba api = new PagoAndinoAPIDePrueba();
        PasarelaPago adaptador = new AdaptadorPagoAndino(api);

        ResultadoPago aprobado = adaptador.cobrar(TARJETA_VALIDA, pesos(1_000_000), "REF-1");
        verificar("$1.000.000 llega como 100.000.000 centavos y se aprueba",
                api.ultimoValorCentavos == 100_000_000L && aprobado.estado() == ResultadoPago.Estado.APROBADO
                        && aprobado.autorizacion() != null);
        ResultadoPago sinFondos = adaptador.cobrar(TARJETA_VALIDA, pesos(4_812_750), "REF-2");
        verificar("$4.812.750 devuelve FONDOS_INSUFICIENTES",
                sinFondos.estado() == ResultadoPago.Estado.FONDOS_INSUFICIENTES);

        api.codigoForzado = 14;
        verificar("código 14 se traduce a TARJETA_INVALIDA",
                adaptador.cobrar(TARJETA_VALIDA, pesos(1_000), "REF-3").estado()
                        == ResultadoPago.Estado.TARJETA_INVALIDA);
        api.codigoForzado = 91;
        verificar("código 91 se traduce a PASARELA_NO_DISPONIBLE",
                adaptador.cobrar(TARJETA_VALIDA, pesos(1_000), "REF-4").estado()
                        == ResultadoPago.Estado.PASARELA_NO_DISPONIBLE);

        PagoAndinoAPIDePrueba apiProtegida = new PagoAndinoAPIDePrueba();
        PasarelaPago proxy = new ProxyPasarelaPago(new AdaptadorPagoAndino(apiProtegida));
        ResultadoPago rechazada = proxy.cobrar(TARJETA_INVALIDA, pesos(1_000), "REF-5");
        verificar("tarjeta inválida devuelve TARJETA_INVALIDA sin llamar a PagoAndino (no aparece la línea [PagoAndino])",
                rechazada.estado() == ResultadoPago.Estado.TARJETA_INVALIDA && apiProtegida.llamadas == 0);
    }

    private static void probarCupos(DatosPrueba datos) {
        titulo("Cupos: Finca cafetera con cupo máximo de 6");
        PagoAndinoAPIDePrueba api = new PagoAndinoAPIDePrueba();
        Escenario escenario = Escenario.conPagoAndino(api, datos);
        LocalDate fecha = datos.fechaBase.plusDays(70);

        ResultadoConfirmacion primera = escenario.confirmarComoAsesor(
                construirReserva(datos.emma, datos.finca, fecha, 4), TARJETA_VALIDA);
        ResultadoConfirmacion excedida = escenario.confirmarComoAsesor(
                construirReserva(datos.emma, datos.finca, fecha, 3), TARJETA_VALIDA);
        verificar("4 viajeros se confirman", primera.confirmada());
        verificar("3 viajeros más el mismo día se rechazan por cupo (7 > 6)",
                !excedida.confirmada() && excedida.motivoRechazo() == MotivoRechazo.CUPO_INSUFICIENTE);
        verificar("la reserva rechazada por cupo no generó cobro", api.llamadas == 1);

        ResultadoConfirmacion siguiente = escenario.confirmarComoAsesor(
                construirReserva(datos.emma, datos.finca, fecha, 2), TARJETA_VALIDA);
        verificar("la reserva rechazada no consumió número",
                siguiente.reserva().getNumero() == primera.reserva().getNumero() + 1);
    }

    private static void probarFondosInsuficientes(DatosPrueba datos) {
        titulo("Fondos insuficientes: correo con el motivo, cupos intactos");
        PagoAndinoAPIDePrueba api = new PagoAndinoAPIDePrueba();
        Escenario escenario = Escenario.conPagoAndino(api, datos);
        LocalDate fecha = datos.fechaBase.plusDays(80);

        ResultadoConfirmacion primera = escenario.confirmarComoAsesor(
                construirReserva(datos.emma, datos.cocora, fecha, 2), TARJETA_VALIDA);
        int cupoAntes = escenario.controlCupos.cupoDisponible(datos.cocora, fecha);

        api.codigoForzado = 51;
        ResultadoConfirmacion sinFondos = escenario.confirmarComoAsesor(
                construirReserva(datos.emma, datos.cocora, fecha, 3), TARJETA_VALIDA);
        verificar("reserva rechazada por FONDOS_INSUFICIENTES",
                !sinFondos.confirmada() && sinFondos.motivoRechazo() == MotivoRechazo.FONDOS_INSUFICIENTES);
        verificar("se envió correo con el motivo",
                escenario.correo.asuntosEnviados().contains("Reserva no confirmada"));
        verificar("los cupos quedaron intactos",
                escenario.controlCupos.cupoDisponible(datos.cocora, fecha) == cupoAntes);

        api.codigoForzado = null;
        ResultadoConfirmacion siguiente = escenario.confirmarComoAsesor(
                construirReserva(datos.emma, datos.cocora, fecha, 3), TARJETA_VALIDA);
        verificar("el número no se consumió",
                siguiente.reserva().getNumero() == primera.reserva().getNumero() + 1);
    }

    private static void probarCanales(DatosPrueba datos) {
        titulo("Canales: asesor, kiosco y una pasarela distinta sin tocar el confirmador");
        Escenario escenario = new Escenario(new PasarelaSimulada(), datos);
        LocalDate fecha = datos.fechaBase.plusDays(90);

        ResultadoConfirmacion asesor = escenario.confirmarComoAsesor(construirReservaCompleta(datos, fecha), TARJETA_VALIDA);
        ResultadoConfirmacion kiosco = escenario.confirmarEnKiosco(construirReservaCompleta(datos, fecha), TARJETA_VALIDA);

        String[] lineasAsesor = asesor.voucher().split("\n");
        String[] lineasKiosco = kiosco.voucher().split("\n");
        verificar("la pasarela simulada funciona sin modificar el confirmador", asesor.confirmada() && kiosco.confirmada());
        verificar("mismo encabezado en ambos canales",
                lineasAsesor[0].equals(lineasKiosco[0]) && lineasAsesor[1].equals(lineasKiosco[1]));
        verificar("mismo pie en ambos canales",
                lineasAsesor[lineasAsesor.length - 1].equals(lineasKiosco[lineasKiosco.length - 1]));
        verificar("el detalle del asesor es largo y el del kiosco es un tiquete de una línea",
                lineasAsesor.length > 6 && lineasKiosco.length == 4 && lineasKiosco[2].startsWith("TIQUETE RV"));
    }

    private static void probarReportes(DatosPrueba datos) {
        titulo("Reportes: ingresos por periodo y producto con más viajeros");
        Escenario escenario = Escenario.conPagoAndino(new PagoAndinoAPIDePrueba(), datos);
        LocalDate inicio = datos.fechaBase.plusDays(100);

        escenario.confirmarComoAsesor(construirReserva(datos.emma, datos.cocora, inicio, 2), TARJETA_VALIDA);
        escenario.confirmarComoAsesor(construirReserva(datos.emma, datos.salento, inicio.plusDays(10), 3), TARJETA_VALIDA);
        escenario.confirmarComoAsesor(construirReserva(datos.emma, datos.salento, inicio.plusDays(20), 4), TARJETA_VALIDA);
        escenario.confirmarComoAsesor(construirReserva(datos.emma, datos.quindioTotal, inicio.plusDays(25), 2), TARJETA_VALIDA);

        ReporteReservas reporte = new ReporteReservas(escenario.registro);
        BigDecimal ingresos = reporte.calcularIngresos(inicio, inicio.plusDays(20));
        System.out.println("Ingresos del periodo: " + FormatoMoneda.pesos(ingresos));
        verificar("ingresos del periodo (extremos incluidos) = $1.042.000", esIgual(ingresos, 1_042_000));

        ReporteReservas.ProductoConViajeros masViajeros = reporte.productoConMasViajeros().orElseThrow();
        System.out.println("Producto con más viajeros: " + masViajeros.producto().obtenerNombre()
                + " (" + masViajeros.viajeros() + ")");
        verificar("producto con más viajeros: Paquete Salento (7)",
                masViajeros.producto() == datos.salento && masViajeros.viajeros() == 7);
    }

    private static void probarRegistrosYExportacion(DatosPrueba datos) {
        titulo("Registro de turistas y guías, y exportación de vouchers");
        RegistroClientes clientes = new RegistroClientes();
        RegistroGuias guias = new RegistroGuias();
        clientes.registrar(datos.emma);
        esperarRechazo("turista con documento repetido", () -> clientes.registrar(datos.emma));
        esperarRechazo("turista con correo inválido", () -> new Cliente("1", "Ana", "300", "sin-arroba", "Colombia", "Español"));
        esperarRechazo("turista sin idioma", () -> new Cliente("2", "Ana", "300", "ana@correo.com", "Colombia", " "));
        guias.registrar(datos.laura);
        esperarRechazo("guía con identificación repetida", () -> guias.registrar(datos.laura));
        esperarRechazo("guía sin idiomas", () -> new Guia("G-9", "Sin idiomas", List.of(), "300", pesos(100_000)));
        esperarRechazo("guía con tarifa cero", () -> new Guia("G-9", "Sin tarifa", List.of("Español"), "300", pesos(0)));

        Escenario escenario = Escenario.conPagoAndino(new PagoAndinoAPIDePrueba(), datos);
        String voucher = escenario.confirmarComoAsesor(construirReservaCompleta(datos, datos.fechaBase.plusDays(110)),
                TARJETA_VALIDA).voucher();
        try {
            java.nio.file.Path carpeta = java.nio.file.Files.createTempDirectory("vouchers");
            for (FormatoExportacion formato : FormatoExportacion.values()) {
                java.nio.file.Path archivo = carpeta.resolve("voucher." + formato.getExtension());
                formato.crearExportador().exportar(voucher, archivo);
                byte[] contenido = java.nio.file.Files.readAllBytes(archivo);
                verificar("exportación " + formato + " genera " + contenido.length + " bytes en " + archivo,
                        contenido.length > 100);
                if (formato == FormatoExportacion.PDF) {
                    verificar("el PDF inicia con la firma %PDF y termina con %%EOF",
                            new String(contenido, 0, 5, java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF-")
                                    && new String(contenido, java.nio.charset.StandardCharsets.ISO_8859_1).trim().endsWith("%%EOF"));
                }
                if (formato == FormatoExportacion.PNG) {
                    verificar("el PNG tiene firma válida", contenido[1] == 'P' && contenido[2] == 'N' && contenido[3] == 'G');
                }
            }
        } catch (java.io.IOException error) {
            verificar("exportación sin errores: " + error.getMessage(), false);
        }
    }

    private static void probarPagosYAutorizacion(DatosPrueba datos) {
        titulo("PagoAndino: autorización, códigos inesperados y rechazos");
        PagoAndinoAPIDePrueba api = new PagoAndinoAPIDePrueba();
        PasarelaPago adaptador = new AdaptadorPagoAndino(api);
        ResultadoPago primero = adaptador.cobrar(TARJETA_VALIDA, pesos(3_000_000), "REF-A");
        ResultadoPago segundo = adaptador.cobrar(TARJETA_VALIDA, pesos(10_000), "REF-B");
        verificar("$3.000.000 (300.000.000 centavos, el límite) se aprueba con autorización PA-1001",
                primero.fueAprobado() && "PA-1001".equals(primero.autorizacion()) && api.ultimoValorCentavos == 10_000L * 100);
        verificar("el siguiente cobro aprobado recibe PA-1002", "PA-1002".equals(segundo.autorizacion()));
        ResultadoPago excedido = adaptador.cobrar(TARJETA_VALIDA, pesos(3_000_001), "REF-C");
        verificar("$3.000.001 devuelve 51 y no trae autorización",
                excedido.estado() == ResultadoPago.Estado.FONDOS_INSUFICIENTES && excedido.autorizacion() == null);
        api.codigoForzado = 77;
        verificar("un código inesperado se trata como error controlado y no se aprueba",
                !adaptador.cobrar(TARJETA_VALIDA, pesos(1_000), "REF-D").fueAprobado());

        PagoAndinoAPIDePrueba apiReserva = new PagoAndinoAPIDePrueba();
        Escenario escenario = Escenario.conPagoAndino(apiReserva, datos);
        ResultadoConfirmacion aprobada = escenario.confirmarComoAsesor(
                construirReserva(datos.emma, datos.cocora, datos.fechaBase.plusDays(120), 2), TARJETA_VALIDA);
        verificar("la autorización queda en la reserva y en el voucher",
                aprobada.reserva().getAutorizacionPago() != null && aprobada.voucher().contains("Autorización de pago: PA-"));
        apiReserva.codigoForzado = 51;
        int registradasAntes = escenario.registro.listar().size();
        ResultadoConfirmacion rechazada = escenario.confirmarComoAsesor(
                construirReserva(datos.emma, datos.cocora, datos.fechaBase.plusDays(121), 2), TARJETA_VALIDA);
        verificar("un cobro rechazado no deja la reserva confirmada ni registrada ni con autorización",
                !rechazada.confirmada() && !rechazada.reserva().estaConfirmada()
                        && rechazada.reserva().getAutorizacionPago() == null
                        && escenario.registro.listar().size() == registradasAntes);
    }

    private static void probarActividadesPersistentesYRn05(DatosPrueba datos) {
        titulo("Actividades persistentes y RN-05");
        try {
            java.nio.file.Path archivo = java.nio.file.Files.createTempDirectory("actividades").resolve("actividades.tsv");
            RegistroActividades registro = new RegistroActividades(new RepositorioActividadesArchivo(archivo));
            Actividad nueva = new Actividad(registro.siguienteCodigo(), "Cabalgata en Filandia", "Filandia", 3,
                    pesos(70_000), 12, Dificultad.MEDIA);
            nueva.establecerDescripcion("Recorrido a caballo por cafetales");
            registro.registrar(nueva);
            RegistroActividades recargado = new RegistroActividades(new RepositorioActividadesArchivo(archivo));
            verificar("la actividad nueva sobrevive a un reinicio (se leyó del archivo)",
                    recargado.listar().size() == 1 && recargado.listar().get(0).obtenerNombre().equals("Cabalgata en Filandia")
                            && recargado.listar().get(0).getDescripcion().startsWith("Recorrido"));
            esperarRechazo("actividad con nombre repetido", () -> registro.registrar(new Actividad(
                    registro.siguienteCodigo(), "cabalgata en filandia", "Filandia", 3, pesos(1), 5, Dificultad.BAJA)));
            esperarRechazo("actividad con precio negativo", () -> new Actividad("X", "A", "B", 3, pesos(-1), 5, Dificultad.BAJA));
            esperarRechazo("actividad con cupo 0", () -> new Actividad("X", "A", "B", 3, pesos(1), 0, Dificultad.BAJA));
            esperarRechazo("actividad sin nombre", () -> new Actividad("X", " ", "B", 3, pesos(1), 5, Dificultad.BAJA));

            esperarAceptacion("reservar actividad disponible", () -> construirReserva(datos.emma, nueva, datos.fechaBase, 2));
            registro.suspender(nueva.obtenerCodigo());
            esperarRechazo("RN-05: reservar una actividad suspendida", () -> construirReserva(datos.emma, nueva, datos.fechaBase, 2));
            Paquete paquete = new Paquete("PAQ-X", "Paquete con cabalgata", 0);
            paquete.agregar(nueva);
            esperarRechazo("RN-05: reservar un paquete que incluye la actividad suspendida", () -> construirReserva(datos.emma, paquete, datos.fechaBase, 2));
            verificar("la suspensión quedó guardada en el archivo",
                    new RegistroActividades(new RepositorioActividadesArchivo(archivo)).listar().get(0).estaSuspendida());
            registro.habilitar(nueva.obtenerCodigo());
            esperarAceptacion("RN-05: al habilitarla se puede reservar de nuevo", () -> construirReserva(datos.emma, paquete, datos.fechaBase, 2));

            java.nio.file.Files.writeString(archivo, "linea dañada sin campos\n");
            try {
                new RegistroActividades(new RepositorioActividadesArchivo(archivo));
                verificar("archivo dañado debía reportarse", false);
            } catch (ErrorAlmacenamientoException error) {
                verificar("archivo dañado -> error de almacenamiento claro: " + error.getMessage(), true);
            }
        } catch (java.io.IOException error) {
            verificar("preparación de la prueba: " + error.getMessage(), false);
        }
    }

    private static void probarFalloDeCorreo(DatosPrueba datos) {
        titulo("RF-14: un fallo de correo se informa y no tumba la reserva");
        Escenario escenario = new Escenario(new PasarelaSimulada(), datos, new CorreoQueFalla());
        ResultadoConfirmacion resultado = escenario.confirmarComoAsesor(
                construirReserva(datos.emma, datos.cocora, datos.fechaBase.plusDays(130), 2), TARJETA_VALIDA);
        verificar("la reserva queda confirmada pero el correo NO se reporta como aceptado",
                resultado.confirmada() && !resultado.correoAceptado() && resultado.errorCorreo().contains("SMTP"));
    }

    private static Reserva construirReserva(Cliente cliente, ProductoTuristico producto, LocalDate fecha, int viajeros) {
        return new Reserva.Builder().cliente(cliente).producto(producto).fechaViaje(fecha).viajeros(viajeros).build();
    }

    private static Reserva construirReservaCompleta(DatosPrueba datos, LocalDate fecha) {
        return new Reserva.Builder().cliente(datos.emma).producto(datos.quindioTotal).fechaViaje(fecha)
                .viajeros(2).guia(datos.laura).conTransporte(true).conSeguro(true).build();
    }

    private static BigDecimal pesos(long valor) {
        return BigDecimal.valueOf(valor);
    }

    private static boolean esIgual(BigDecimal valor, long esperado) {
        return valor.compareTo(BigDecimal.valueOf(esperado)) == 0;
    }

    private static void describir(ProductoTuristico producto) {
        System.out.printf("  %s | %s por persona | %d h | %d actividad(es)%n", producto.obtenerNombre(),
                FormatoMoneda.pesos(producto.calcularPrecioPorPersona()), producto.calcularDuracionHoras(),
                producto.obtenerActividades().size());
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("=== " + texto + " ===");
    }

    private static void verificar(String descripcion, boolean condicion) {
        if (condicion) {
            pruebasAprobadas++;
            System.out.println("  [OK]    " + descripcion);
        } else {
            pruebasFallidas++;
            System.out.println("  [FALLA] " + descripcion);
        }
    }

    private static void esperarRechazo(String descripcion, Runnable accion) {
        try {
            accion.run();
            verificar(descripcion + " debía rechazarse", false);
        } catch (ReglaNegocioException rechazo) {
            verificar(descripcion + " -> rechazado: " + rechazo.getMessage(), true);
        }
    }

    private static void esperarAceptacion(String descripcion, Runnable accion) {
        try {
            accion.run();
            verificar(descripcion + " -> aceptado", true);
        } catch (ReglaNegocioException rechazo) {
            verificar(descripcion + " debía aceptarse pero se rechazó: " + rechazo.getMessage(), false);
        }
    }

    private static class DatosPrueba {

        final LocalDate fechaBase = LocalDate.now().plusDays(30);
        final AgenciaRutaVerde agencia = new AgenciaRutaVerde("RutaVerde Quindío", "901.234.567-8", "RNT-123456",
                "Calle 21 # 14-30, Armenia, Quindío", "606 7410000", "reservas@rutaverde.co", "www.rutaverde.co");
        final Actividad cocora = new Actividad("ACT-01", "Valle del Cocora", "Salento", 4, pesos(80_000), 20, Dificultad.MEDIA);
        final Actividad finca = new Actividad("ACT-02", "Finca cafetera", "Salento", 3, pesos(60_000), 6, Dificultad.BAJA);
        final Actividad aves = new Actividad("ACT-03", "Avistamiento de aves", "Filandia", 5, pesos(50_000), 20, Dificultad.BAJA);
        final Actividad parqueDelCafe = new Actividad("ACT-04", "Parque del Café", "Montenegro", 8, pesos(100_000), 50, Dificultad.BAJA);
        final Actividad panaca = new Actividad("ACT-05", "PANACA", "Quimbaya", 6, pesos(90_000), 50, Dificultad.BAJA);
        final Actividad actividadSuspendida = new Actividad("ACT-06", "Rafting", "Calarcá", 4, pesos(70_000), 10, Dificultad.ALTA);
        final Paquete salento = new Paquete("PAQ-01", "Paquete Salento", 10);
        final Paquete parques = new Paquete("PAQ-02", "Paquete Parques", 5);
        final Paquete quindioTotal = new Paquete("PAQ-03", "Quindío Total", 10);
        final Paquete paqueteConActividadSuspendida = new Paquete("PAQ-04", "Paquete Aventura", 0);
        final Guia laura = new Guia("G-01", "Laura", List.of("Español", "Inglés"), "3001112233", pesos(120_000));
        final Guia pedroSoloEspanol = new Guia("G-02", "Pedro", List.of("Español"), "3004445566", pesos(110_000));
        final Cliente emma = new Cliente("P-998877", "Emma Smith", "+1 555 0101", "emma.smith@correo.com", "Reino Unido", "Inglés");

        DatosPrueba() {
            salento.agregar(cocora);
            salento.agregar(finca);
            parques.agregar(parqueDelCafe);
            parques.agregar(panaca);
            quindioTotal.agregar(salento);
            quindioTotal.agregar(parques);
            quindioTotal.agregar(aves);
            paqueteConActividadSuspendida.agregar(actividadSuspendida);
            actividadSuspendida.suspender();
        }
    }

    private static class Escenario {

        final ControlCupos controlCupos = new ControlCupos();
        final RegistroReservas registro = new RegistroReservas();
        final ServicioCorreoConsola correo = new ServicioCorreoConsola();
        final ConfirmadorReserva confirmador;
        final EmisorVoucher emisorAsesor;
        final EmisorVoucher emisorKiosco;

        Escenario(PasarelaPago pasarela, DatosPrueba datos) {
            this(pasarela, datos, null);
        }

        Escenario(PasarelaPago pasarela, DatosPrueba datos, ServicioCorreo correoAlterno) {
            this.confirmador = new ConfirmadorReserva(controlCupos, pasarela, registro,
                    correoAlterno == null ? correo : correoAlterno);
            this.emisorAsesor = new EmisorVoucherAsesor(datos.agencia);
            this.emisorKiosco = new EmisorVoucherKiosco(datos.agencia);
        }

        static Escenario conPagoAndino(PagoAndinoAPI api, DatosPrueba datos) {
            return new Escenario(new ProxyPasarelaPago(new AdaptadorPagoAndino(api)), datos);
        }

        ResultadoConfirmacion confirmarComoAsesor(Reserva reserva, String tarjeta) {
            return confirmador.confirmar(reserva, tarjeta, emisorAsesor);
        }

        ResultadoConfirmacion confirmarEnKiosco(Reserva reserva, String tarjeta) {
            return confirmador.confirmar(reserva, tarjeta, emisorKiosco);
        }
    }

    private static class PagoAndinoAPIDePrueba extends PagoAndinoAPI {

        Integer codigoForzado;
        int llamadas;
        long ultimoValorCentavos;

        @Override
        public int procesarCobro(String numeroTarjeta, long valorCentavos, String referencia) {
            llamadas++;
            ultimoValorCentavos = valorCentavos;
            if (codigoForzado != null) {
                return codigoForzado;
            }
            return super.procesarCobro(numeroTarjeta, valorCentavos, referencia);
        }
    }

    private static class CorreoQueFalla implements ServicioCorreo {

        @Override
        public void enviar(String destinatario, String asunto, String cuerpo) {
            throw new ErrorEnvioCorreoException("Servidor SMTP inalcanzable (prueba controlada).");
        }
    }

    private static class PasarelaSimulada implements PasarelaPago {

        @Override
        public ResultadoPago cobrar(String numeroTarjeta, BigDecimal valorPesos, String referencia) {
            return new ResultadoPago(ResultadoPago.Estado.APROBADO, "SIM-0001");
        }
    }
}
