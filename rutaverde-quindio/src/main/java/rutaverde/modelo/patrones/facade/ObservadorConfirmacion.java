package rutaverde.modelo.patrones.facade;

public interface ObservadorConfirmacion {

    void pasoTerminado(PasoConfirmacion paso, boolean exitoso);
}
