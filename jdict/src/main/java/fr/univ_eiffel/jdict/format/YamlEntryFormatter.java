package fr.univ_eiffel.jdict.format;

import fr.univ_eiffel.jdict.model.DictionaryFormatException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONParserConfiguration;

import java.util.HexFormat;
import java.util.TreeSet;

/** Affiche une entrée en YAML avec les types de mots triés. */
public final class YamlEntryFormatter implements EntryFormatter {
    private static final JSONParserConfiguration STRICT_JSON = new JSONParserConfiguration().withStrictMode();
    private static final HexFormat UPPER_HEX = HexFormat.of().withUpperCase();

    /** Formate le titre et les définitions en un document YAML. */
    @Override
    public String format(String jsonLine) throws DictionaryFormatException {
        try {
            JSONObject fields = new JSONObject(jsonLine, STRICT_JSON);
            JSONObject definitions = fields.getJSONObject("definitions");
            StringBuilder out = new StringBuilder("---\ntitle: ");
            appendQuoted(out, fields.getString("title"));
            out.append("\ndefinitions:\n");
            for (String type : new TreeSet<>(definitions.keySet())) {
                out.append("  ");
                appendQuoted(out, type);
                out.append(":\n");
                JSONArray values = definitions.getJSONArray(type);
                for (int i = 0; i < values.length(); i++) {
                    out.append("    - ");
                    appendQuoted(out, values.getString(i));
                    out.append('\n');
                }
            }
            return out.toString();
        } catch (JSONException error) {
            throw new DictionaryFormatException("Entree JSON invalide pour le format YAML.", error);
        }
    }

    private static void appendQuoted(StringBuilder out, String value) {
        out.append('"');
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            switch (character) {
                case '\\' -> out.append("\\\\");
                case '"' -> out.append("\\\"");
                case '\n' -> out.append("\\n");
                case '\t' -> out.append("\\t");
                case '\r' -> out.append("\\r");
                default -> {
                    if (character < 0x20) {
                        out.append("\\u").append(UPPER_HEX.toHexDigits((short) character));
                    } else {
                        out.append(character);
                    }
                }
            }
        }
        out.append('"');
    }
}
