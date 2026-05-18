package com.securitesociale.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Utilitaire de hachage et vérification de mots de passe (BCrypt, coût 12).
 */
public class PasswordUtil {

    private static final int COST = 12;

    /** Retourne le hash BCrypt du mot de passe en clair. */
    public static String hash(String plaintext) {
        return BCrypt.withDefaults().hashToString(COST, plaintext.toCharArray());
    }

    /** Vérifie si le mot de passe en clair correspond au hash stocké. */
    public static boolean verify(String plaintext, String hash) {
        BCrypt.Result result = BCrypt.verifyer().verify(plaintext.toCharArray(), hash);
        return result.verified;
    }
}
