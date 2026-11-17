package fr.univ_eiffel.jdict.text;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.regex.Pattern;

/** Adapte les diagnostics à la console. */
public final class ConsoleMessages {
    private static final Pattern MARKS = Pattern.compile("\\p{M}");

    private ConsoleMessages() {
    }

    /** Renvoie le diagnostic en ASCII, en retirant les accents. */
    public static String ascii(String message) {
        // Certaines consoles Windows affichent mal les accents des diagnostics.
        String decomposed = Normalizer.normalize(String.valueOf(message), Normalizer.Form.NFD);
        String withoutMarks = MARKS.matcher(decomposed).replaceAll("");
        return new String(withoutMarks.getBytes(StandardCharsets.US_ASCII), StandardCharsets.US_ASCII);
    }
}
