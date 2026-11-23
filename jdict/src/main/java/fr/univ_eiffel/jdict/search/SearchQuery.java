package fr.univ_eiffel.jdict.search;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

/** Représente un mot recherché et son format d'affichage. */
public record SearchQuery(String word, boolean yaml) {
    /** Refuse un mot vide. */
    public SearchQuery {
        if (word.isEmpty()) {
            throw new IllegalArgumentException("Requete vide");
        }
    }

    /** Reconnaît le préfixe yaml: et conserve le mot tel qu'il est saisi. */
    public static SearchQuery parse(String text) {
        if (text.startsWith("yaml:")) {
            return new SearchQuery(text.substring("yaml:".length()), true);
        }
        return new SearchQuery(text, false);
    }

    /** Indique si le mot est déjà en majuscules. */
    public boolean normalized() {
        return word.equals(word.toUpperCase(Locale.ROOT));
    }

    /** Renvoie la requête et ses variantes d'apostrophes sans doublon. */
    public List<String> variants() {
        return List.copyOf(new LinkedHashSet<>(List.of(word, word.replace('\'', '’'), word.replace('’', '\''))));
    }
}
