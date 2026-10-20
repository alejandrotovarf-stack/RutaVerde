package rutaverde.modelo.servicios;

import java.io.IOException;
import java.nio.file.Path;

public interface ExportadorVoucher {

    void exportar(String voucher, Path destino) throws IOException;
}
