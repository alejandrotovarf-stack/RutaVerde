package rutaverde.modelo.servicios;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;

public class ExportadorPng implements ExportadorVoucher {

    private static final int ANCHO = 820;
    private static final int ALTO_LINEA = 24;
    private static final int MARGEN = 36;

    static {
        System.setProperty("java.awt.headless", "true");
    }

    @Override
    public void exportar(String voucher, Path destino) throws IOException {
        List<String> lineas = voucher.lines().toList();
        int alto = MARGEN * 2 + ALTO_LINEA * lineas.size() + 12;
        BufferedImage imagen = new BufferedImage(ANCHO, alto, BufferedImage.TYPE_INT_RGB);
        Graphics2D lienzo = imagen.createGraphics();
        try {
            lienzo.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            lienzo.setColor(new Color(0xF6F0E4));
            lienzo.fillRect(0, 0, ANCHO, alto);
            lienzo.setColor(new Color(0x0F5F43));
            lienzo.fillRect(0, 0, ANCHO, 10);
            lienzo.setColor(new Color(0x20170F));
            lienzo.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
            int y = MARGEN + 12;
            for (String linea : lineas) {
                lienzo.drawString(linea, MARGEN, y);
                y += ALTO_LINEA;
            }
        } finally {
            lienzo.dispose();
        }
        if (!ImageIO.write(imagen, "png", destino.toFile())) {
            throw new IOException("No se pudo generar la imagen PNG.");
        }
    }
}
