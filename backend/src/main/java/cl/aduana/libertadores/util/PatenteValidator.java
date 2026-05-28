package cl.aduana.libertadores.util;

import java.util.regex.Pattern;

public final class PatenteValidator {

    private static final Pattern CHILENA = Pattern.compile("^[A-Z]{4}[0-9]{2}$");
    private static final Pattern ARGENTINA = Pattern.compile("^[A-Z]{2}[0-9]{3}[A-Z]{2}$");
    private static final Pattern DIPLOMATICA = Pattern.compile("^(CD|CC|OI|PAT)", Pattern.CASE_INSENSITIVE);

    private PatenteValidator() {
    }

    public static boolean esFormatoChileno(String patente) {
        return patente != null && CHILENA.matcher(patente.toUpperCase().replaceAll("[\\s-]", "")).matches();
    }

    public static boolean esFormatoArgentino(String patente) {
        return patente != null && ARGENTINA.matcher(patente.toUpperCase().replaceAll("[\\s-]", "")).matches();
    }

    public static boolean esPlacaDiplomatica(String patente) {
        if (patente == null) {
            return false;
        }
        String normalizada = patente.toUpperCase().replaceAll("[\\s-]", "");
        return DIPLOMATICA.matcher(normalizada).lookingAt();
    }

    public static String normalizar(String patente) {
        return patente == null ? null : patente.toUpperCase().replaceAll("[\\s-]", "");
    }
}
