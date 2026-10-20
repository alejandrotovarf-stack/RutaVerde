package rutaverde.modelo.servicios;

public interface ServicioCorreo {

    void enviar(String destinatario, String asunto, String cuerpo);
}
