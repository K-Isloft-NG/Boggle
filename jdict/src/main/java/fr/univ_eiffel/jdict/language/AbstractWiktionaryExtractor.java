package fr.univ_eiffel.jdict.language;

import fr.univ_eiffel.jdict.model.DictionaryEntry;
import fr.univ_eiffel.jdict.model.WikiPage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Parcourt les sections et collecte les définitions d'une page. */
public abstract class AbstractWiktionaryExtractor implements LanguageExtractor {
    private static final String REDIRECT = "#REDIRECT";

    /** Indique si un titre de niveau 2 ouvre la section de la langue. */
    protected abstract boolean isLanguageHeading(String line);

    /** Renvoie le type de mot introduit par un titre de niveau 3 ou plus. */
    protected abstract Optional<String> wordTypeOf(String headingLine);

    /** Extrait les définitions de la langue, dans l'ordre de la page. */
    @Override
    public final Optional<DictionaryEntry> extract(WikiPage page) {
        String text = page.text();
        if (text.regionMatches(true, 0, REDIRECT, 0, REDIRECT.length())) {
            return Optional.empty();
        }

        Map<String, List<String>> definitions = new LinkedHashMap<>();
        boolean inLanguage = false;
        String type = null;
        Iterator<String> lines = text.lines().iterator();
        while (lines.hasNext()) {
            String line = lines.next();
            if (line.startsWith("===")) {
                if (inLanguage) {
                    type = wordTypeOf(line).orElse(null);
                }
            } else if (line.startsWith("==") && line.length() > 2) {
                inLanguage = isLanguageHeading(line);
                type = null;
            } else if (inLanguage && type != null && isDefinition(line)) {
                String definition = line.substring(1).strip();
                if (!definition.isEmpty()) {
                    definitions.computeIfAbsent(type, key -> new ArrayList<>()).add(definition);
                }
            }
        }
        if (definitions.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new DictionaryEntry(page.title(), definitions));
    }

    private static boolean isDefinition(String line) {
        return line.length() > 1 && line.charAt(0) == '#' && "*:#;".indexOf(line.charAt(1)) < 0;
    }
}
