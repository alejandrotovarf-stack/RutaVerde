package rutaverde.modelo.persistencia;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import rutaverde.modelo.entidades.Actividad;
import rutaverde.modelo.entidades.Dificultad;
import rutaverde.modelo.excepciones.ErrorAlmacenamientoException;
import rutaverde.modelo.excepciones.ReglaNegocioException;

public class RepositorioActividadesArchivo implements RepositorioActividades {

    private static final String SEPARADOR = "\t";
    private static final int CAMPOS = 10;

    private final Path archivo;

    public RepositorioActividadesArchivo(Path archivo) {
        this.archivo = archivo;
    }

    @Override
    public List<Actividad> cargar() {
        if (!Files.exists(archivo)) {
            return new ArrayList<>();
        }
        List<String> lineas;
        try {
            lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
        } catch (IOException error) {
            throw new ErrorAlmacenamientoException("No se pudo leer el archivo de actividades " + archivo + ".", error);
        }
        List<Actividad> actividades = new ArrayList<>();
        for (int numeroLinea = 0; numeroLinea < lineas.size(); numeroLinea++) {
            if (!lineas.get(numeroLinea).isBlank()) {
                actividades.add(interpretar(lineas.get(numeroLinea), numeroLinea + 1));
            }
        }
        return actividades;
    }

    @Override
    public void guardar(List<Actividad> actividades) {
        List<String> lineas = actividades.stream().map(this::serializar).toList();
        Path temporal = archivo.resolveSibling(archivo.getFileName() + ".tmp");
        try {
            Files.createDirectories(archivo.toAbsolutePath().getParent());
            Files.write(temporal, lineas, StandardCharsets.UTF_8);
            Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException error) {
            throw new ErrorAlmacenamientoException("No se pudo guardar el archivo de actividades " + archivo + ": "
                    + error.getMessage(), error);
        }
    }

    private String serializar(Actividad actividad) {
        return String.join(SEPARADOR, limpio(actividad.obtenerCodigo()), limpio(actividad.obtenerNombre()),
                limpio(actividad.getMunicipio()), String.valueOf(actividad.calcularDuracionHoras()),
                actividad.calcularPrecioPorPersona().toPlainString(), String.valueOf(actividad.getCupoMaximoDia()),
                actividad.getDificultad().name(), actividad.estaSuspendida() ? "SUSPENDIDA" : "DISPONIBLE",
                limpio(actividad.getDescripcion()), limpio(actividad.getRutaImagen()));
    }

    private Actividad interpretar(String linea, int numeroLinea) {
        String[] campos = linea.split(SEPARADOR, -1);
        try {
            if (campos.length != CAMPOS) {
                throw new IllegalArgumentException("número de campos incorrecto");
            }
            Actividad actividad = new Actividad(campos[0], campos[1], campos[2], Integer.parseInt(campos[3]),
                    new BigDecimal(campos[4]), Integer.parseInt(campos[5]), Dificultad.valueOf(campos[6]));
            if (campos[7].equals("SUSPENDIDA")) {
                actividad.suspender();
            }
            actividad.establecerDescripcion(campos[8]);
            actividad.establecerRutaImagen(campos[9]);
            return actividad;
        } catch (IllegalArgumentException | ReglaNegocioException error) {
            throw new ErrorAlmacenamientoException("El archivo " + archivo + " está dañado en la línea "
                    + numeroLinea + " (" + error.getMessage() + "). No se modificó; corríjalo o muévalo.", error);
        }
    }

    private String limpio(String texto) {
        return texto.replace('\t', ' ').replace('\r', ' ').replace('\n', ' ');
    }
}
