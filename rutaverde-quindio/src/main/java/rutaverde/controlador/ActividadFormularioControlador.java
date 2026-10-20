package rutaverde.controlador;

import java.io.File;
import java.math.BigDecimal;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import rutaverde.aplicacion.ContextoAplicacion;
import rutaverde.modelo.entidades.Actividad;
import rutaverde.modelo.entidades.Dificultad;
import rutaverde.modelo.excepciones.ErrorAlmacenamientoException;
import rutaverde.modelo.excepciones.ReglaNegocioException;

public class ActividadFormularioControlador {

    private final ContextoAplicacion contexto;
    private boolean guardada;
    private String rutaImagen = "";

    @FXML
    private VBox formulario;
    @FXML
    private TextField campoNombre;
    @FXML
    private TextField campoMunicipio;
    @FXML
    private TextArea areaDescripcion;
    @FXML
    private TextField campoPrecio;
    @FXML
    private TextField campoDuracion;
    @FXML
    private TextField campoCupo;
    @FXML
    private ComboBox<Dificultad> comboDificultad;
    @FXML
    private CheckBox checkDisponible;
    @FXML
    private Label etiquetaImagen;
    @FXML
    private Label etiquetaMensaje;

    public ActividadFormularioControlador(ContextoAplicacion contexto) {
        this.contexto = contexto;
    }

    @FXML
    private void initialize() {
        comboDificultad.setItems(FXCollections.observableArrayList(Dificultad.values()));
        comboDificultad.setConverter(new StringConverter<>() {
            @Override
            public String toString(Dificultad dificultad) {
                return dificultad == null ? "" : dificultad.getEtiqueta();
            }

            @Override
            public Dificultad fromString(String texto) {
                return null;
            }
        });
        comboDificultad.setValue(Dificultad.BAJA);
    }

    @FXML
    private void elegirImagen() {
        FileChooser selector = new FileChooser();
        selector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.jpg", "*.jpeg", "*.png", "*.webp"));
        File archivo = selector.showOpenDialog(formulario.getScene().getWindow());
        if (archivo != null) {
            rutaImagen = archivo.getAbsolutePath();
            etiquetaImagen.setText(archivo.getName());
        }
    }

    @FXML
    private void guardar() {
        try {
            int duracion = Integer.parseInt(campoDuracion.getText().trim());
            int cupo = Integer.parseInt(campoCupo.getText().trim());
            BigDecimal precio = new BigDecimal(campoPrecio.getText().trim());
            if (comboDificultad.getValue() == null) {
                throw new ReglaNegocioException("Seleccione el nivel de dificultad.");
            }
            Actividad actividad = new Actividad(contexto.registroActividades.siguienteCodigo(), campoNombre.getText().trim(),
                    campoMunicipio.getText().trim(), duracion, precio, cupo, comboDificultad.getValue());
            actividad.establecerDescripcion(areaDescripcion.getText());
            actividad.establecerRutaImagen(rutaImagen);
            if (!checkDisponible.isSelected()) {
                actividad.suspender();
            }
            contexto.registroActividades.registrar(actividad);
            guardada = true;
            cerrar();
        } catch (NumberFormatException noNumerico) {
            informarError("Precio, duración y cupo deben ser números válidos (duración y cupo, enteros).");
        } catch (ReglaNegocioException | ErrorAlmacenamientoException rechazo) {
            informarError(rechazo.getMessage());
        }
    }

    @FXML
    private void cancelar() {
        cerrar();
    }

    public boolean fueGuardada() {
        return guardada;
    }

    private void informarError(String mensaje) {
        Efectos.mostrar(etiquetaMensaje, mensaje, false);
        Efectos.sacudir(formulario);
    }

    private void cerrar() {
        ((Stage) formulario.getScene().getWindow()).close();
    }
}
