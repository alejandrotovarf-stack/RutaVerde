package rutaverde.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import rutaverde.aplicacion.ContextoAplicacion;
import rutaverde.modelo.excepciones.ReglaNegocioException;
import rutaverde.modelo.servicios.FormatoMoneda;

public class ReportesControlador {

    private final ContextoAplicacion contexto;

    @FXML
    private DatePicker fechaInicial;
    @FXML
    private DatePicker fechaFinal;
    @FXML
    private Label etiquetaIngresos;
    @FXML
    private Label etiquetaProductoTop;

    public ReportesControlador(ContextoAplicacion contexto) {
        this.contexto = contexto;
    }

    @FXML
    private void calcular() {
        try {
            etiquetaIngresos.setText(FormatoMoneda.pesos(
                    contexto.reporte.calcularIngresos(fechaInicial.getValue(), fechaFinal.getValue())));
        } catch (ReglaNegocioException | NullPointerException rechazo) {
            etiquetaIngresos.setText("Seleccione fechas válidas: la inicial no puede superar la final.");
        }
        etiquetaProductoTop.setText(contexto.reporte.productoConMasViajeros()
                .map(top -> top.producto().obtenerNombre() + " (" + top.viajeros() + " viajeros)")
                .orElse("Sin reservas confirmadas"));
    }
}
