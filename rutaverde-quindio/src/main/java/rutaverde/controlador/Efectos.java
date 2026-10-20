package rutaverde.controlador;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.util.Duration;

public final class Efectos {

    private Efectos() {
    }

    public static void mostrar(Label etiqueta, String texto, boolean exito) {
        etiqueta.getStyleClass().removeAll("exito", "error");
        etiqueta.getStyleClass().add(exito ? "exito" : "error");
        etiqueta.setText(texto);
        etiqueta.setOpacity(0);
        FadeTransition entrada = new FadeTransition(Duration.millis(240), etiqueta);
        entrada.setToValue(1);
        entrada.play();
    }

    public static void sacudir(Node nodo) {
        TranslateTransition sacudida = new TranslateTransition(Duration.millis(60), nodo);
        sacudida.setFromX(0);
        sacudida.setByX(8);
        sacudida.setCycleCount(6);
        sacudida.setAutoReverse(true);
        sacudida.setOnFinished(evento -> nodo.setTranslateX(0));
        sacudida.play();
    }
}
