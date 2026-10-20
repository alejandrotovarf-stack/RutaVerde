package rutaverde.controlador;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import rutaverde.aplicacion.ContextoAplicacion;
import rutaverde.modelo.entidades.Guia;
import rutaverde.modelo.excepciones.ReglaNegocioException;

public class GuiasControlador {

    private final ContextoAplicacion contexto;

    @FXML
    private VBox formulario;
    @FXML
    private TextField campoIdentificacion;
    @FXML
    private TextField campoNombre;
    @FXML
    private TextField campoIdiomas;
    @FXML
    private TextField campoTelefono;
    @FXML
    private TextField campoTarifa;
    @FXML
    private TableView<Guia> tabla;
    @FXML
    private TextField campoBuscar;
    @FXML
    private Label etiquetaMensaje;

    public GuiasControlador(ContextoAplicacion contexto) {
        this.contexto = contexto;
    }

    @FXML
    private void initialize() {
        refrescarTabla();
        campoBuscar.textProperty().addListener((o, a, n) -> filtrar());
    }

    private void filtrar() {
        String texto = campoBuscar.getText().trim().toLowerCase();
        tabla.setItems(FXCollections.observableArrayList(contexto.registroGuias.listar().stream()
                .filter(guia -> guia.getNombre().toLowerCase().contains(texto)
                        || guia.getIdentificacion().toLowerCase().contains(texto)).toList()));
    }

    @FXML
    private void registrar() {
        try {
            List<String> idiomas = Arrays.stream(campoIdiomas.getText().split(","))
                    .map(String::trim).filter(idioma -> !idioma.isEmpty()).toList();
            Guia guia = new Guia(campoIdentificacion.getText(), campoNombre.getText(), idiomas,
                    campoTelefono.getText(), new BigDecimal(campoTarifa.getText().trim()));
            contexto.registroGuias.registrar(guia);
            refrescarTabla();
            limpiar();
            Efectos.mostrar(etiquetaMensaje, "Guía registrado: " + guia.getNombre(), true);
        } catch (ReglaNegocioException rechazo) {
            Efectos.mostrar(etiquetaMensaje, rechazo.getMessage(), false);
            Efectos.sacudir(formulario);
        } catch (NumberFormatException tarifaNoNumerica) {
            Efectos.mostrar(etiquetaMensaje, "La tarifa por día debe ser un número, por ejemplo 120000.", false);
            Efectos.sacudir(formulario);
        }
    }

    private void refrescarTabla() {
        filtrar();
    }

    private void limpiar() {
        campoIdentificacion.clear();
        campoNombre.clear();
        campoIdiomas.clear();
        campoTelefono.clear();
        campoTarifa.clear();
    }
}
