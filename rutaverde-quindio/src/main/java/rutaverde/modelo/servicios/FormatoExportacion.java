package rutaverde.modelo.servicios;

import java.util.function.Supplier;

public enum FormatoExportacion {
    TXT("txt", ExportadorTxt::new),
    PDF("pdf", ExportadorPdf::new),
    PNG("png", ExportadorPng::new);

    private final String extension;
    private final Supplier<ExportadorVoucher> fabrica;

    FormatoExportacion(String extension, Supplier<ExportadorVoucher> fabrica) {
        this.extension = extension;
        this.fabrica = fabrica;
    }

    public ExportadorVoucher crearExportador() {
        return fabrica.get();
    }

    public String getExtension() {
        return extension;
    }
}
