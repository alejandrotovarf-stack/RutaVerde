package rutaverde.modelo.excepciones;

public class ErrorEnvioCorreoException extends RuntimeException {

    public ErrorEnvioCorreoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public ErrorEnvioCorreoException(String mensaje) {
        super(mensaje);
    }
}
