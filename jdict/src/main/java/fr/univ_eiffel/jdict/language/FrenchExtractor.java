package fr.univ_eiffel.jdict.language;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Reconnaît la section française et ses types de mots. */
public final class FrenchExtractor extends AbstractWiktionaryExtractor {
    private static final Pattern LANGUAGE = Pattern.compile("==\\s*\\{\\{langue\\|fr\\}\\}\\s*==");
    private static final Pattern WORD_TYPE = Pattern.compile("^={3,}\\s*\\{\\{S\\|([^|}]+)\\|fr(\\||\\}\\})");

    /** Donne le code de la langue française. */
    @Override
    public String languageCode() {
        return "fr";
    }

    @Override
    protected boolean isLanguageHeading(String line) {
        return LANGUAGE.matcher(line.strip()).matches();
    }

    @Override
    protected Optional<String> wordTypeOf(String headingLine) {
        Matcher match = WORD_TYPE.matcher(headingLine);
        if (match.find()) {
            String type = match.group(1).strip();
            if (!type.isEmpty()) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }
}
