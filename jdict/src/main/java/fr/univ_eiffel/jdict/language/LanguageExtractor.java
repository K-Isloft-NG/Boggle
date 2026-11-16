package fr.univ_eiffel.jdict.language;

import fr.univ_eiffel.jdict.model.DictionaryEntry;
import fr.univ_eiffel.jdict.model.WikiPage;

import java.util.Optional;

/** Extrait les définitions d'une langue dans une page du Wiktionnaire. */
public interface LanguageExtractor {
    /** Donne le code de la langue traitée. */
    String languageCode();

    /** Renvoie les définitions trouvées pour la langue. */
    Optional<DictionaryEntry> extract(WikiPage page);
}
