package rutaverde.modelo.servicios;

import rutaverde.modelo.excepciones.ErrorEnvioCorreoException;

public class ServicioCorreoNoConfigurado implements ServicioCorreo {

    @Override
    public void enviar(String destinatario, String asunto, String cuerpo) {
        throw new ErrorEnvioCorreoException("No hay servidor SMTP configurado. Defina las variables de entorno "
                + "RUTAVERDE_SMTP_SERVIDOR, RUTAVERDE_SMTP_PUERTO, RUTAVERDE_SMTP_USUARIO y RUTAVERDE_SMTP_CLAVE "
                + "antes de abrir la aplicación.");
    }
}
