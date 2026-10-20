package rutaverde.modelo.servicios;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServicioCorreoConsola implements ServicioCorreo {

    private final List<String> asuntosEnviados = new ArrayList<>();

    @Override
    public void enviar(String destinatario, String asunto, String cuerpo) {
        asuntosEnviados.add(asunto);
        System.out.println("[Correo] Para: " + destinatario + " | Asunto: " + asunto);
        cuerpo.lines().forEach(linea -> System.out.println("         " + linea));
    }

    public List<String> asuntosEnviados() {
        return Collections.unmodifiableList(asuntosEnviados);
    }
}
