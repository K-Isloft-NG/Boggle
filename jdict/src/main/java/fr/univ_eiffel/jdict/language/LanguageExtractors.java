package fr.univ_eiffel.jdict.language;

import java.util.Map;
import java.util.Set;

/** Donne accès aux extracteurs par code de langue. */
public final class LanguageExtractors {
    private static final Map<String, LanguageExtractor> EXTRACTORS = Map.of("fr", new FrenchExtractor());

    private LanguageExtractors() {
    }

    /** Renvoie l'extracteur d'une langue prise en charge. */
    public static LanguageExtractor forCode(String code) {
        LanguageExtractor extractor = EXTRACTORS.get(code);
        if (extractor == null) {
            throw new IllegalArgumentException("Langue non prise en charge : " + code
                    + " (langues : " + String.join(", ", supportedCodes()) + ")");
        }
        return extractor;
    }

    /** Renvoie les codes de langue pris en charge. */
    public static Set<String> supportedCodes() {
        return EXTRACTORS.keySet();
    }
}
