package rutaverde.modelo.patrones.proxy;

public final class ValidadorLuhn {

    private static final int LONGITUD_MINIMA = 13;
    private static final int LONGITUD_MAXIMA = 19;

    private ValidadorLuhn() {
    }

    public static boolean esValida(String numeroTarjeta) {
        if (numeroTarjeta == null) {
            return false;
        }
        String digitos = numeroTarjeta.replace(" ", "");
        if (digitos.length() < LONGITUD_MINIMA || digitos.length() > LONGITUD_MAXIMA) {
            return false;
        }
        int suma = 0;
        boolean duplicar = false;
        for (int posicion = digitos.length() - 1; posicion >= 0; posicion--) {
            char caracter = digitos.charAt(posicion);
            if (caracter < '0' || caracter > '9') {
                return false;
            }
            int digito = caracter - '0';
            if (duplicar) {
                digito *= 2;
                if (digito > 9) {
                    digito -= 9;
                }
            }
            suma += digito;
            duplicar = !duplicar;
        }
        return suma % 10 == 0;
    }
}
