package fr.univ_eiffel.jdict.text;

/** Vérifie qu'un mot normalisé est utilisable par le moteur Boggle. */
public final class BoggleWordFilter {
    private BoggleWordFilter() {
    }

    /** Accepte les mots non vides composés uniquement de lettres de A à Z. */
    public static boolean isBoggleWord(String normalized) {
        if (normalized.isEmpty()) {
            return false;
        }
        for (int i = 0; i < normalized.length(); i++) {
            char letter = normalized.charAt(i);
            if (letter < 'A' || letter > 'Z') {
                return false;
            }
        }
        return true;
    }
}
