package cl.aduana.libertadores.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Hashing irreversible de contraseñas de empleados con BCrypt.
 */
public final class PasswordUtil {

    private static final int ROUNDS = 12;

    private PasswordUtil() {
    }

    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(ROUNDS));
    }

    public static boolean verify(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
