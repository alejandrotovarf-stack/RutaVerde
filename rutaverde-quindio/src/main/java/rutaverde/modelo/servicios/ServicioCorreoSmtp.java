package rutaverde.modelo.servicios;

import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import rutaverde.modelo.excepciones.ErrorEnvioCorreoException;

public class ServicioCorreoSmtp implements ServicioCorreo {

    private static final int PUERTO_SSL = 465;
    private static final String TIEMPO_ESPERA_MILISEGUNDOS = "15000";

    private final Session sesion;
    private final String remitente;

    public ServicioCorreoSmtp(String servidor, int puerto, String usuario, String clave) {
        Properties propiedades = new Properties();
        propiedades.put("mail.smtp.host", servidor);
        propiedades.put("mail.smtp.port", String.valueOf(puerto));
        propiedades.put("mail.smtp.auth", "true");
        propiedades.put(puerto == PUERTO_SSL ? "mail.smtp.ssl.enable" : "mail.smtp.starttls.enable", "true");
        propiedades.put("mail.smtp.starttls.required", String.valueOf(puerto != PUERTO_SSL));
        propiedades.put("mail.smtp.connectiontimeout", TIEMPO_ESPERA_MILISEGUNDOS);
        propiedades.put("mail.smtp.timeout", TIEMPO_ESPERA_MILISEGUNDOS);
        propiedades.put("mail.smtp.writetimeout", TIEMPO_ESPERA_MILISEGUNDOS);
        this.remitente = usuario;
        this.sesion = Session.getInstance(propiedades, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(usuario, clave);
            }
        });
    }

    @Override
    public void enviar(String destinatario, String asunto, String cuerpo) {
        try {
            MimeMessage mensaje = new MimeMessage(sesion);
            mensaje.setFrom(new InternetAddress(remitente));
            mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario, true));
            mensaje.setSubject(asunto, "UTF-8");
            mensaje.setText(cuerpo, "UTF-8");
            Transport.send(mensaje);
        } catch (AddressException direccionInvalida) {
            throw new ErrorEnvioCorreoException("La dirección de correo '" + destinatario + "' no es válida.",
                    direccionInvalida);
        } catch (AuthenticationFailedException autenticacionFallida) {
            throw new ErrorEnvioCorreoException("El servidor rechazó el usuario o la clave SMTP. En Gmail se "
                    + "requiere una contraseña de aplicación.", autenticacionFallida);
        } catch (MessagingException error) {
            throw new ErrorEnvioCorreoException("El servidor de correo no aceptó el mensaje: " + error.getMessage(), error);
        }
    }
}
