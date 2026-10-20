package rutaverde.modelo.servicios;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ExportadorPdf implements ExportadorVoucher {

    private static final int LINEAS_POR_PAGINA = 48;
    private static final Charset WIN_ANSI = Charset.forName("windows-1252");

    @Override
    public void exportar(String voucher, Path destino) throws IOException {
        List<String> lineas = voucher.lines().toList();
        int paginas = Math.max(1, (lineas.size() + LINEAS_POR_PAGINA - 1) / LINEAS_POR_PAGINA);
        ByteArrayOutputStream documento = new ByteArrayOutputStream();
        List<Integer> posiciones = new ArrayList<>();
        documento.write("%PDF-1.4\n".getBytes(StandardCharsets.US_ASCII));

        StringBuilder hijas = new StringBuilder();
        for (int pagina = 0; pagina < paginas; pagina++) {
            hijas.append(4 + 2 * pagina).append(" 0 R ");
        }
        escribirObjeto(documento, posiciones, "<< /Type /Catalog /Pages 2 0 R >>".getBytes(StandardCharsets.US_ASCII));
        escribirObjeto(documento, posiciones,
                ("<< /Type /Pages /Kids [" + hijas + "] /Count " + paginas + " >>").getBytes(StandardCharsets.US_ASCII));
        escribirObjeto(documento, posiciones,
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>"
                        .getBytes(StandardCharsets.US_ASCII));
        for (int pagina = 0; pagina < paginas; pagina++) {
            int desde = pagina * LINEAS_POR_PAGINA;
            List<String> lineasDePagina = lineas.subList(desde, Math.min(lineas.size(), desde + LINEAS_POR_PAGINA));
            escribirObjeto(documento, posiciones, ("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
                    + "/Resources << /Font << /F1 3 0 R >> >> /Contents " + (5 + 2 * pagina) + " 0 R >>")
                    .getBytes(StandardCharsets.US_ASCII));
            byte[] contenido = construirContenido(lineasDePagina);
            ByteArrayOutputStream flujo = new ByteArrayOutputStream();
            flujo.write(("<< /Length " + contenido.length + " >>\nstream\n").getBytes(StandardCharsets.US_ASCII));
            flujo.write(contenido);
            flujo.write("\nendstream".getBytes(StandardCharsets.US_ASCII));
            escribirObjeto(documento, posiciones, flujo.toByteArray());
        }

        int inicioTabla = documento.size();
        StringBuilder tabla = new StringBuilder("xref\n0 " + (posiciones.size() + 1) + "\n0000000000 65535 f \n");
        posiciones.forEach(posicion -> tabla.append(String.format("%010d 00000 n \n", posicion)));
        tabla.append("trailer\n<< /Size ").append(posiciones.size() + 1).append(" /Root 1 0 R >>\nstartxref\n")
                .append(inicioTabla).append("\n%%EOF\n");
        documento.write(tabla.toString().getBytes(StandardCharsets.US_ASCII));
        Files.write(destino, documento.toByteArray());
    }

    private void escribirObjeto(ByteArrayOutputStream documento, List<Integer> posiciones, byte[] cuerpo)
            throws IOException {
        posiciones.add(documento.size());
        documento.write(((posiciones.size()) + " 0 obj\n").getBytes(StandardCharsets.US_ASCII));
        documento.write(cuerpo);
        documento.write("\nendobj\n".getBytes(StandardCharsets.US_ASCII));
    }

    private byte[] construirContenido(List<String> lineas) {
        StringBuilder contenido = new StringBuilder("BT /F1 12 Tf 50 790 Td 16 TL\n");
        for (String linea : lineas) {
            String segura = linea.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
            contenido.append('(').append(segura).append(") Tj T*\n");
        }
        contenido.append("ET");
        return contenido.toString().getBytes(WIN_ANSI);
    }
}
