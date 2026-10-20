package rutaverde.controlador;

import javafx.util.Callback;
import rutaverde.aplicacion.ContextoAplicacion;

public final class FabricaControladores {

    private FabricaControladores() {
    }

    public static Callback<Class<?>, Object> para(ContextoAplicacion contexto) {
        return tipo -> {
            try {
                return tipo.getConstructor(ContextoAplicacion.class).newInstance(contexto);
            } catch (ReflectiveOperationException error) {
                throw new IllegalStateException(error);
            }
        };
    }
}
