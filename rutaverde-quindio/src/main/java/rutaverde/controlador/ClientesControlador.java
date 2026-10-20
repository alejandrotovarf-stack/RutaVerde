package rutaverde.controlador;

import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import rutaverde.aplicacion.ContextoAplicacion;
import rutaverde.modelo.entidades.Cliente;
import rutaverde.modelo.excepciones.ReglaNegocioException;

public class ClientesControlador {

    private static final String TODOS_LOS_PAISES = "Todos los países";

    private final ContextoAplicacion contexto;
    private final ObservableList<Cliente> filas = FXCollections.observableArrayList();
    private FilteredList<Cliente> filtradas;

    @FXML
    private VBox formulario;
    @FXML
    private TextField campoDocumento;
    @FXML
    private TextField campoNombre;
    @FXML
    private TextField campoTelefono;
    @FXML
    private TextField campoCorreo;
    @FXML
    private TextField campoPais;
    @FXML
    private ComboBox<String> comboIdioma;
    @FXML
    private TextField campoBuscar;
    @FXML
    private ComboBox<String> comboFiltroPais;
    @FXML
    private TableView<Cliente> tabla;
    @FXML
    private Label etiquetaMensaje;

    public ClientesControlador(ContextoAplicacion contexto) {
        this.contexto = contexto;
    }

    @FXML
    private void initialize() {
        comboIdioma.setItems(FXCollections.observableArrayList("Español", "Inglés", "Francés", "Alemán", "Portugués"));
        filtradas = new FilteredList<>(filas, cliente -> true);
        tabla.setItems(filtradas);
        campoBuscar.textProperty().addListener((o, a, n) -> aplicarFiltro());
        comboFiltroPais.valueProperty().addListener((o, a, n) -> aplicarFiltro());
        refrescar();
    }

    @FXML
    private void registrar() {
        try {
            Cliente cliente = new Cliente(campoDocumento.getText(), campoNombre.getText(), campoTelefono.getText(),
                    campoCorreo.getText(), campoPais.getText(), comboIdioma.getValue());
            contexto.registroClientes.registrar(cliente);
            refrescar();
            limpiar();
            Efectos.mostrar(etiquetaMensaje, "Turista registrado: " + cliente.getNombreCompleto(), true);
        } catch (ReglaNegocioException rechazo) {
            Efectos.mostrar(etiquetaMensaje, rechazo.getMessage(), false);
            Efectos.sacudir(formulario);
        }
    }

    private void refrescar() {
        filas.setAll(contexto.registroClientes.listar());
        String seleccionado = comboFiltroPais.getValue();
        List<String> paises = new ArrayList<>(List.of(TODOS_LOS_PAISES));
        filas.stream().map(Cliente::getPaisOrigen).filter(pais -> pais != null && !pais.isBlank())
                .distinct().sorted().forEach(paises::add);
        comboFiltroPais.setItems(FXCollections.observableArrayList(paises));
        comboFiltroPais.setValue(seleccionado != null && paises.contains(seleccionado) ? seleccionado : TODOS_LOS_PAISES);
    }

    private void aplicarFiltro() {
        String texto = campoBuscar.getText() == null ? "" : campoBuscar.getText().trim().toLowerCase();
        String pais = comboFiltroPais.getValue();
        filtradas.setPredicate(cliente ->
                (texto.isEmpty() || cliente.getNombreCompleto().toLowerCase().contains(texto)
                        || cliente.getDocumentoIdentidad().toLowerCase().contains(texto))
                        && (pais == null || pais.equals(TODOS_LOS_PAISES) || pais.equalsIgnoreCase(cliente.getPaisOrigen())));
    }

    private void limpiar() {
        campoDocumento.clear();
        campoNombre.clear();
        campoTelefono.clear();
        campoCorreo.clear();
        campoPais.clear();
        comboIdioma.getSelectionModel().clearSelection();
    }
}
