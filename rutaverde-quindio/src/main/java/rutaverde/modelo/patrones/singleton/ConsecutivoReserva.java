package rutaverde.modelo.patrones.singleton;

public final class ConsecutivoReserva {

    private static final ConsecutivoReserva INSTANCIA = new ConsecutivoReserva();

    private int ultimoNumero;

    private ConsecutivoReserva() {
    }

    public static ConsecutivoReserva getInstancia() {
        return INSTANCIA;
    }

    public synchronized int siguienteNumero() {
        ultimoNumero++;
        return ultimoNumero;
    }
}
