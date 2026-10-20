package rutaverde.controlador;

import java.io.IOException;
import java.io.UncheckedIOException;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import java.util.Map;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import rutaverde.aplicacion.ContextoAplicacion;

public class PrincipalControlador {

    private static final Map<String, String> TITULOS = Map.of(
            "clientes", "Turistas", "guias", "Guías", "nuevaReserva", "Nueva reserva",
            "actividades", "Actividades", "panel", "Panel principal", "reportes", "Reportes");

    private final ContextoAplicacion contexto;

    @FXML
    private StackPane contenido;
    @FXML
    private VBox menu;
    @FXML
    private Label tituloVista;

    public PrincipalControlador(ContextoAplicacion contexto) {
        this.contexto = contexto;
    }

    @FXML
    private void initialize() {
        mostrar("clientes");
    }

    @FXML
    private void navegar(ActionEvent evento) {
        mostrar(((Button) evento.getSource()).getId());
    }

    @FXML
    private void alternarTema() {
        var clases = contenido.getScene().getRoot().getStyleClass();
        if (!clases.remove("oscuro")) {
            clases.add("oscuro");
        }
    }

    private void mostrar(String pantalla) {
        try {
            FXMLLoader cargador = new FXMLLoader(getClass().getResource("/rutaverde/vista/fxml/" + pantalla + ".fxml"));
            cargador.setControllerFactory(FabricaControladores.para(contexto));
            Node vista = cargador.load();
            contenido.getChildren().setAll(vista);
            marcarActivo(pantalla);
            tituloVista.setText(TITULOS.getOrDefault(pantalla, pantalla).toUpperCase());
            FadeTransition fundido = new FadeTransition(Duration.millis(280), vista);
            fundido.setFromValue(0);
            fundido.setToValue(1);
            TranslateTransition desplazamiento = new TranslateTransition(Duration.millis(280), vista);
            desplazamiento.setFromY(18);
            desplazamiento.setToY(0);
            new ParallelTransition(fundido, desplazamiento).play();
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
    }

    private void marcarActivo(String pantalla) {
        for (Node nodo : menu.getChildren()) {
            if (nodo instanceof Button boton) {
                boton.getStyleClass().remove("activo");
                if (pantalla.equals(boton.getId())) {
                    boton.getStyleClass().add("activo");
                }
            }
        }
    }
}
