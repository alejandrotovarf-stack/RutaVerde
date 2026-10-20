package rutaverde.modelo.servicios;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ExportadorTxt implements ExportadorVoucher {

    @Override
    public void exportar(String voucher, Path destino) throws IOException {
        Files.writeString(destino, voucher, StandardCharsets.UTF_8);
    }
}
