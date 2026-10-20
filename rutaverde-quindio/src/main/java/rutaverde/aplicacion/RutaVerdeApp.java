package rutaverde.aplicacion;

import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import rutaverde.modelo.excepciones.ErrorAlmacenamientoException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import rutaverde.controlador.FabricaControladores;

public class RutaVerdeApp extends Application {

    @Override
    public void start(Stage escenario) throws IOException {
        ContextoAplicacion contexto;
        try {
            contexto = new ContextoAplicacion();
        } catch (ErrorAlmacenamientoException error) {
            new Alert(Alert.AlertType.ERROR, error.getMessage()).showAndWait();
            Platform.exit();
            return;
        }
        FXMLLoader cargador = new FXMLLoader(getClass().getResource("/rutaverde/vista/fxml/principal.fxml"));
        cargador.setControllerFactory(FabricaControladores.para(contexto));
        Scene escena = new Scene(cargador.<Parent>load(), 1280, 800);
        escena.getStylesheets().add(getClass().getResource("/rutaverde/vista/estilos/tema.css").toExternalForm());
        escenario.setTitle("Agencia Ruta Verde");
        for (String tamano : new String[]{"16", "32", "64", "256"}) {
            escenario.getIcons().add(new Image(getClass().getResourceAsStream("/rutaverde/vista/imagenes/icono-" + tamano + ".png")));
        }
        escenario.setMinWidth(1100);
        escenario.setMinHeight(700);
        escenario.setScene(escena);
        escenario.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
