package fr.univ_eiffel.jdict.text;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/** Calcule la forme normalisée des mots du dictionnaire. */
public final class TextNormalizer {
    private static final Pattern MARKS = Pattern.compile("\\p{M}");

    private TextNormalizer() {
    }

    /** Retire les marques diacritiques, met en majuscules et développe les ligatures. */
    public static String normalize(String text) {
        String decomposed = Normalizer.normalize(text, Normalizer.Form.NFD);
        String withoutMarks = MARKS.matcher(decomposed).replaceAll("");
        return withoutMarks.toUpperCase(Locale.ROOT).replace("Œ", "OE").replace("Æ", "AE");
    }
}
