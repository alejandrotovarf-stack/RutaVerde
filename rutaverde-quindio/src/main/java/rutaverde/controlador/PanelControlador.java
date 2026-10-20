package rutaverde.controlador;

import java.time.LocalDate;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.util.Duration;
import rutaverde.aplicacion.ContextoAplicacion;
import rutaverde.modelo.servicios.FormatoMoneda;

public class PanelControlador {

    private final ContextoAplicacion contexto;

    @FXML
    private Label etiquetaReservas;
    @FXML
    private Label etiquetaIngresos;
    @FXML
    private Label etiquetaProductoTop;

    public PanelControlador(ContextoAplicacion contexto) {
        this.contexto = contexto;
    }

    @FXML
    private void initialize() {
        etiquetaReservas.setText(String.valueOf(contexto.registro.listar().size()));
        etiquetaProductoTop.setText(contexto.reporte.productoConMasViajeros()
                .map(top -> top.producto().obtenerNombre() + " (" + top.viajeros() + ")").orElse("Sin reservas"));
        double total = contexto.reporte.calcularIngresos(LocalDate.of(2000, 1, 1), LocalDate.of(2100, 12, 31))
                .doubleValue();
        SimpleDoubleProperty animado = new SimpleDoubleProperty(0);
        animado.addListener((observado, anterior, actual) ->
                etiquetaIngresos.setText(FormatoMoneda.pesos(java.math.BigDecimal.valueOf(actual.doubleValue()))));
        new Timeline(new KeyFrame(Duration.millis(900), new KeyValue(animado, total))).play();
        etiquetaIngresos.setText(FormatoMoneda.pesos(java.math.BigDecimal.ZERO));
    }
}
