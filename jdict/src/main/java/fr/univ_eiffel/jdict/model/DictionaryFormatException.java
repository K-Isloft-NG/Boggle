package fr.univ_eiffel.jdict.model;

import java.io.IOException;

/** Signale une erreur dans le format des données du dictionnaire. */
public final class DictionaryFormatException extends IOException {
    private static final long serialVersionUID = 1L;

    /** Crée une erreur de format avec son message. */
    public DictionaryFormatException(String message) {
        super(message);
    }

    /** Crée une erreur de format en conservant sa cause. */
    public DictionaryFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
