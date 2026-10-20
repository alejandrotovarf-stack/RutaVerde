package rutaverde.modelo.excepciones;

public class ErrorAlmacenamientoException extends RuntimeException {

    public ErrorAlmacenamientoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public ErrorAlmacenamientoException(String mensaje) {
        super(mensaje);
    }
}
