package rutaverde.controlador;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import rutaverde.aplicacion.ContextoAplicacion;
import rutaverde.modelo.entidades.Actividad;
import rutaverde.modelo.entidades.Dificultad;
import rutaverde.modelo.excepciones.ErrorAlmacenamientoException;
import rutaverde.modelo.servicios.FormatoMoneda;

public class ActividadesControlador {

    private static final String TODOS_LOS_MUNICIPIOS = "Todos los municipios";
    private static final String TODAS_LAS_DIFICULTADES = "Todas las dificultades";
    private static final double ANCHO_TARJETA = 322;
    private static final double ALTO_FOTO = 170;

    private final ContextoAplicacion contexto;

    @FXML
    private FlowPane tarjetas;
    @FXML
    private TextField campoBuscar;
    @FXML
    private ComboBox<String> comboMunicipio;
    @FXML
    private ComboBox<String> comboDificultad;
    @FXML
    private Label etiquetaMensaje;

    public ActividadesControlador(ContextoAplicacion contexto) {
        this.contexto = contexto;
    }

    @FXML
    private void initialize() {
        List<String> dificultades = new ArrayList<>(List.of(TODAS_LAS_DIFICULTADES));
        Arrays.stream(Dificultad.values()).map(Dificultad::getEtiqueta).forEach(dificultades::add);
        comboDificultad.setItems(FXCollections.observableArrayList(dificultades));
        comboDificultad.setValue(TODAS_LAS_DIFICULTADES);
        campoBuscar.textProperty().addListener((o, a, n) -> reconstruir());
        comboMunicipio.valueProperty().addListener((o, a, n) -> reconstruir());
        comboDificultad.valueProperty().addListener((o, a, n) -> reconstruir());
        actualizarMunicipios();
        reconstruir();
    }

    @FXML
    private void nuevaActividad() {
        try {
            FXMLLoader cargador = new FXMLLoader(getClass().getResource("/rutaverde/vista/fxml/actividadFormulario.fxml"));
            cargador.setControllerFactory(FabricaControladores.para(contexto));
            Parent vista = cargador.load();
            Stage ventana = new Stage();
            ventana.initModality(Modality.APPLICATION_MODAL);
            ventana.setTitle("Nueva actividad");
            Scene escena = new Scene(vista);
            escena.getStylesheets().addAll(tarjetas.getScene().getStylesheets());
            escena.getRoot().getStyleClass().addAll(tarjetas.getScene().getRoot().getStyleClass());
            ventana.setScene(escena);
            ventana.showAndWait();
            if (cargador.<ActividadFormularioControlador>getController().fueGuardada()) {
                actualizarMunicipios();
                reconstruir();
                Efectos.mostrar(etiquetaMensaje, "Actividad guardada. Ya aparece en el listado.", true);
            }
        } catch (IOException error) {
            Efectos.mostrar(etiquetaMensaje, "No se pudo abrir el formulario: " + error.getMessage(), false);
        }
    }

    private void actualizarMunicipios() {
        String seleccionado = comboMunicipio.getValue();
        List<String> municipios = new ArrayList<>(List.of(TODOS_LOS_MUNICIPIOS));
        contexto.registroActividades.listar().stream().map(Actividad::getMunicipio).distinct().sorted()
                .forEach(municipios::add);
        comboMunicipio.setItems(FXCollections.observableArrayList(municipios));
        comboMunicipio.setValue(seleccionado != null && municipios.contains(seleccionado) ? seleccionado : TODOS_LOS_MUNICIPIOS);
    }

    private void reconstruir() {
        String texto = campoBuscar.getText() == null ? "" : campoBuscar.getText().trim().toLowerCase();
        String municipio = comboMunicipio.getValue();
        String dificultad = comboDificultad.getValue();
        tarjetas.getChildren().clear();
        contexto.registroActividades.listar().stream()
                .filter(actividad -> texto.isEmpty() || actividad.obtenerNombre().toLowerCase().contains(texto)
                        || actividad.getMunicipio().toLowerCase().contains(texto))
                .filter(actividad -> municipio == null || municipio.equals(TODOS_LOS_MUNICIPIOS)
                        || actividad.getMunicipio().equals(municipio))
                .filter(actividad -> dificultad == null || dificultad.equals(TODAS_LAS_DIFICULTADES)
                        || actividad.getDificultad().getEtiqueta().equals(dificultad))
                .forEach(actividad -> tarjetas.getChildren().add(crearTarjeta(actividad)));
        if (tarjetas.getChildren().isEmpty()) {
            Label vacio = new Label("No hay actividades que coincidan con los filtros.");
            vacio.getStyleClass().add("texto-suave");
            tarjetas.getChildren().add(vacio);
        }
    }

    private VBox crearTarjeta(Actividad actividad) {
        ImageView foto = new ImageView(cargarImagen(actividad));
        foto.setFitWidth(ANCHO_TARJETA);
        foto.setFitHeight(ALTO_FOTO);
        StackPane portada = new StackPane(foto);
        portada.setPrefSize(ANCHO_TARJETA, ALTO_FOTO);
        portada.setMaxSize(ANCHO_TARJETA, ALTO_FOTO);
        Rectangle recorte = new Rectangle(ANCHO_TARJETA, ALTO_FOTO);
        recorte.setArcWidth(36);
        recorte.setArcHeight(36);
        portada.setClip(recorte);

        Label estado = new Label(actividad.estaSuspendida() ? "Suspendida" : "Disponible");
        estado.getStyleClass().addAll("etiqueta", actividad.estaSuspendida() ? "estado-suspendida" : "estado-disponible");
        StackPane.setAlignment(estado, Pos.TOP_LEFT);
        StackPane.setMargin(estado, new javafx.geometry.Insets(10));
        Label dificultad = new Label(actividad.getDificultad().getEtiqueta());
        dificultad.getStyleClass().addAll("etiqueta", "etiqueta-dificultad");
        StackPane.setAlignment(dificultad, Pos.TOP_RIGHT);
        StackPane.setMargin(dificultad, new javafx.geometry.Insets(10));
        portada.getChildren().addAll(estado, dificultad);

        Label nombre = new Label(actividad.obtenerNombre());
        nombre.getStyleClass().add("titulo-tarjeta");
        HBox datos = new HBox(14, dato("icono-lugar", actividad.getMunicipio()),
                dato("icono-reloj", actividad.calcularDuracionHoras() + " h"),
                dato("icono-grupo", "Cupo " + actividad.getCupoMaximoDia()));
        datos.setAlignment(Pos.CENTER_LEFT);
        Label descripcion = new Label(actividad.getDescripcion());
        descripcion.getStyleClass().add("texto-suave");
        descripcion.setWrapText(true);
        descripcion.setMinHeight(40);
        descripcion.setMaxHeight(40);
        Label precio = new Label(FormatoMoneda.pesos(actividad.calcularPrecioPorPersona()));
        precio.getStyleClass().add("precio");
        Label porPersona = new Label("por persona");
        porPersona.getStyleClass().add("precio-sufijo");
        Button detalles = new Button("Ver detalles");
        detalles.getStyleClass().add("boton-suave");
        Region flecha = new Region();
        flecha.getStyleClass().addAll("icono", "icono-flecha", "icono-pequeno", "icono-terracota");
        detalles.setGraphic(flecha);
        detalles.setContentDisplay(javafx.scene.control.ContentDisplay.RIGHT);
        detalles.setOnAction(evento -> mostrarDetalles(actividad));
        Region relleno = new Region();
        HBox.setHgrow(relleno, Priority.ALWAYS);
        HBox pie = new HBox(6, precio, porPersona, relleno, detalles);
        pie.setAlignment(Pos.CENTER_LEFT);

        VBox cuerpo = new VBox(8, nombre, datos, descripcion, pie);
        cuerpo.setPadding(new javafx.geometry.Insets(14, 16, 16, 16));
        VBox tarjeta = new VBox(portada, cuerpo);
        tarjeta.getStyleClass().add("tarjeta-actividad");
        tarjeta.setPrefWidth(ANCHO_TARJETA);
        tarjeta.setMaxWidth(ANCHO_TARJETA);
        return tarjeta;
    }

    private HBox dato(String claseIcono, String texto) {
        Region icono = new Region();
        icono.getStyleClass().addAll("icono", claseIcono, "icono-pequeno", "icono-terracota");
        Label etiqueta = new Label(texto);
        etiqueta.getStyleClass().add("dato-tarjeta");
        HBox contenedor = new HBox(5, icono, etiqueta);
        contenedor.setAlignment(Pos.CENTER_LEFT);
        return contenedor;
    }

    private void mostrarDetalles(Actividad actividad) {
        ButtonType alternar = new ButtonType(actividad.estaSuspendida() ? "Habilitar actividad" : "Suspender actividad");
        Alert detalle = new Alert(Alert.AlertType.NONE);
        detalle.setTitle("Detalles de la actividad");
        detalle.setHeaderText(actividad.obtenerNombre() + " (" + actividad.obtenerCodigo() + ")");
        detalle.setContentText(String.format("%s%n%nMunicipio: %s%nDuración: %d h%nCupo máximo por día: %d%nDificultad: %s%n"
                        + "Precio por persona: %s%nEstado: %s", actividad.getDescripcion(), actividad.getMunicipio(),
                actividad.calcularDuracionHoras(), actividad.getCupoMaximoDia(), actividad.getDificultad().getEtiqueta(),
                FormatoMoneda.pesos(actividad.calcularPrecioPorPersona()),
                actividad.estaSuspendida() ? "Suspendida (no se puede reservar)" : "Disponible"));
        detalle.getButtonTypes().setAll(alternar, ButtonType.CLOSE);
        detalle.getDialogPane().getStylesheets().addAll(tarjetas.getScene().getStylesheets());
        detalle.showAndWait().filter(boton -> boton == alternar).ifPresent(boton -> alternarDisponibilidad(actividad));
    }

    private void alternarDisponibilidad(Actividad actividad) {
        try {
            if (actividad.estaSuspendida()) {
                contexto.registroActividades.habilitar(actividad.obtenerCodigo());
            } else {
                contexto.registroActividades.suspender(actividad.obtenerCodigo());
            }
            reconstruir();
            Efectos.mostrar(etiquetaMensaje, "Disponibilidad actualizada: " + actividad.obtenerNombre(), true);
        } catch (ErrorAlmacenamientoException error) {
            Efectos.mostrar(etiquetaMensaje, error.getMessage(), false);
        }
    }

    private Image cargarImagen(Actividad actividad) {
        if (!actividad.getRutaImagen().isBlank()) {
            File archivo = new File(actividad.getRutaImagen());
            if (archivo.isFile()) {
                return new Image(archivo.toURI().toString(), ANCHO_TARJETA, ALTO_FOTO, false, true, true);
            }
        }
        URL recurso = getClass().getResource("/rutaverde/vista/imagenes/" + actividad.obtenerCodigo() + ".jpg");
        if (recurso == null) {
            recurso = getClass().getResource("/rutaverde/vista/imagenes/actividad-predeterminada.jpg");
        }
        return new Image(recurso.toExternalForm(), ANCHO_TARJETA, ALTO_FOTO, false, true, true);
    }
}
