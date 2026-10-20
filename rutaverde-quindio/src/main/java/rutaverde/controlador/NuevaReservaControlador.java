package rutaverde.controlador;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.util.StringConverter;
import rutaverde.aplicacion.ContextoAplicacion;
import rutaverde.modelo.entidades.Cliente;
import rutaverde.modelo.entidades.Guia;
import rutaverde.modelo.excepciones.ReglaNegocioException;
import rutaverde.modelo.patrones.builder.Reserva;
import rutaverde.modelo.patrones.composite.ProductoTuristico;
import rutaverde.modelo.patrones.facade.PasoConfirmacion;
import rutaverde.modelo.patrones.facade.ResultadoConfirmacion;
import rutaverde.modelo.servicios.FormatoExportacion;
import rutaverde.modelo.servicios.FormatoMoneda;

public class NuevaReservaControlador {

    private final ContextoAplicacion contexto;
    private final Map<PasoConfirmacion, Label> etiquetasDePasos = new EnumMap<>(PasoConfirmacion.class);
    private String ultimoVoucher = "";
    private int ultimoNumero;

    @FXML
    private ComboBox<Cliente> comboCliente;
    @FXML
    private ComboBox<ProductoTuristico> comboProducto;
    @FXML
    private ComboBox<Guia> comboGuia;
    @FXML
    private DatePicker selectorFecha;
    @FXML
    private Spinner<Integer> selectorViajeros;
    @FXML
    private CheckBox usarGuia;
    @FXML
    private CheckBox usarTransporte;
    @FXML
    private CheckBox usarSeguro;
    @FXML
    private TextField campoTarjeta;
    @FXML
    private Label etiquetaValor;
    @FXML
    private Label etiquetaMensaje;
    @FXML
    private VBox listaPasos;
    @FXML
    private TextArea areaVoucher;
    @FXML
    private Button botonConfirmar;
    @FXML
    private ComboBox<FormatoExportacion> comboFormato;
    @FXML
    private Button botonExportar;

    public NuevaReservaControlador(ContextoAplicacion contexto) {
        this.contexto = contexto;
    }

    @FXML
    private void initialize() {
        comboCliente.setItems(FXCollections.observableArrayList(contexto.registroClientes.listar()));
        comboProducto.setItems(FXCollections.observableArrayList(contexto.productosDisponibles()));
        comboGuia.setItems(FXCollections.observableArrayList(contexto.registroGuias.listar()));
        comboCliente.setConverter(convertidor(Cliente::getNombreCompleto));
        comboProducto.setConverter(convertidor(ProductoTuristico::obtenerNombre));
        comboGuia.setConverter(convertidor(Guia::getNombre));
        selectorViajeros.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 15, 1));
        comboFormato.setItems(FXCollections.observableArrayList(FormatoExportacion.values()));
        comboFormato.getSelectionModel().selectFirst();
        botonExportar.setDisable(true);
        comboGuia.disableProperty().bind(usarGuia.selectedProperty().not());
        for (PasoConfirmacion paso : PasoConfirmacion.values()) {
            Label etiqueta = new Label("○  " + paso.getTitulo());
            etiqueta.getStyleClass().add("paso");
            etiquetasDePasos.put(paso, etiqueta);
            listaPasos.getChildren().add(etiqueta);
        }
        comboCliente.valueProperty().addListener((o, a, n) -> actualizarValor());
        comboProducto.valueProperty().addListener((o, a, n) -> actualizarValor());
        comboGuia.valueProperty().addListener((o, a, n) -> actualizarValor());
        selectorFecha.valueProperty().addListener((o, a, n) -> actualizarValor());
        selectorViajeros.valueProperty().addListener((o, a, n) -> actualizarValor());
        usarGuia.selectedProperty().addListener((o, a, n) -> actualizarValor());
        usarTransporte.selectedProperty().addListener((o, a, n) -> actualizarValor());
        usarSeguro.selectedProperty().addListener((o, a, n) -> actualizarValor());
        actualizarValor();
    }

    @FXML
    private void confirmar() {
        try {
            Reserva reserva = construirReserva();
            List<Runnable> cuadros = new ArrayList<>();
            ResultadoConfirmacion resultado = contexto.confirmador.confirmar(reserva, campoTarjeta.getText(),
                    contexto.emisorAsesor, (paso, exitoso) -> cuadros.add(() -> marcarPaso(paso, exitoso)));
            reiniciarPasos();
            animar(cuadros, () -> mostrarResultado(resultado));
        } catch (ReglaNegocioException rechazo) {
            Efectos.mostrar(etiquetaMensaje, rechazo.getMessage(), false);
            Efectos.sacudir(etiquetaMensaje);
        }
    }

    @FXML
    private void exportar() {
        FormatoExportacion formato = comboFormato.getValue();
        FileChooser selector = new FileChooser();
        selector.setInitialFileName("voucher-" + ultimoNumero + "." + formato.getExtension());
        selector.getExtensionFilters().add(new FileChooser.ExtensionFilter(formato.name(), "*." + formato.getExtension()));
        File destino = selector.showSaveDialog(botonExportar.getScene().getWindow());
        if (destino == null) {
            return;
        }
        try {
            formato.crearExportador().exportar(ultimoVoucher, destino.toPath());
            Efectos.mostrar(etiquetaMensaje, "Voucher exportado: " + destino.getName(), true);
        } catch (IOException error) {
            Efectos.mostrar(etiquetaMensaje, "No se pudo exportar el voucher: " + error.getMessage(), false);
        }
    }

    private void mostrarResultado(ResultadoConfirmacion resultado) {
        String estadoCorreo = resultado.correoAceptado()
                ? " Correo entregado al servidor de correo (revise bandeja y spam)."
                : " El correo NO se envió: " + resultado.errorCorreo();
        Efectos.mostrar(etiquetaMensaje, resultado.mensaje() + estadoCorreo,
                resultado.confirmada() && resultado.correoAceptado());
        areaVoucher.setText(resultado.confirmada() ? resultado.voucher() : "");
        botonExportar.setDisable(!resultado.confirmada());
        if (resultado.confirmada()) {
            ultimoVoucher = resultado.voucher();
            ultimoNumero = resultado.reserva().getNumero();
        }
    }

    private Reserva construirReserva() {
        return new Reserva.Builder()
                .cliente(comboCliente.getValue())
                .producto(comboProducto.getValue())
                .fechaViaje(selectorFecha.getValue())
                .viajeros(selectorViajeros.getValue())
                .guia(usarGuia.isSelected() ? comboGuia.getValue() : null)
                .conTransporte(usarTransporte.isSelected())
                .conSeguro(usarSeguro.isSelected())
                .build();
    }

    private void actualizarValor() {
        try {
            etiquetaValor.setText(FormatoMoneda.pesos(construirReserva().calcularValor()));
            etiquetaMensaje.setText("");
        } catch (ReglaNegocioException rechazo) {
            etiquetaValor.setText("—");
            etiquetaMensaje.setText(rechazo.getMessage());
        }
    }

    private void reiniciarPasos() {
        etiquetasDePasos.forEach((paso, etiqueta) -> etiqueta.setText("○  " + paso.getTitulo()));
    }

    private void marcarPaso(PasoConfirmacion paso, boolean exitoso) {
        etiquetasDePasos.get(paso).setText((exitoso ? "✔  " : "✖  ") + paso.getTitulo());
    }

    private void animar(List<Runnable> cuadros, Runnable alTerminar) {
        Timeline linea = new Timeline();
        for (int indice = 0; indice < cuadros.size(); indice++) {
            Runnable cuadro = cuadros.get(indice);
            linea.getKeyFrames().add(new KeyFrame(Duration.millis(320.0 * (indice + 1)), evento -> cuadro.run()));
        }
        linea.setOnFinished(evento -> alTerminar.run());
        linea.play();
    }

    private <T> StringConverter<T> convertidor(java.util.function.Function<T, String> texto) {
        return new StringConverter<>() {
            @Override
            public String toString(T objeto) {
                return objeto == null ? "" : texto.apply(objeto);
            }

            @Override
            public T fromString(String cadena) {
                return null;
            }
        };
    }
}
