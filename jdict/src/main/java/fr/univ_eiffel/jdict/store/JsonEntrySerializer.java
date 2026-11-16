package fr.univ_eiffel.jdict.store;

import fr.univ_eiffel.jdict.model.DefinitionFileHeader;
import fr.univ_eiffel.jdict.model.DictionaryEntry;

import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/** Écrit les lignes JSON du dictionnaire en conservant l'ordre des champs. */
public final class JsonEntrySerializer implements EntrySerializer {
    /** Sérialise le titre et ses définitions sur une ligne JSON. */
    @Override
    public String serialize(DictionaryEntry entry) {
        StringJoiner types = new StringJoiner(",", "{", "}");
        for (Map.Entry<String, List<String>> type : entry.definitions().entrySet()) {
            StringJoiner values = new StringJoiner(",", "[", "]");
            for (String definition : type.getValue()) {
                values.add(quote(definition));
            }
            types.add(quote(type.getKey()) + ":" + values);
        }
        return "{\"title\":" + quote(entry.title()) + ",\"definitions\":" + types + "}";
    }

    /** Sérialise les métadonnées sur une ligne JSON. */
    @Override
    public String serializeHeader(DefinitionFileHeader header) {
        return "{\"description\":" + quote(header.description()) + ",\"created_on\":" + quote(header.createdOn())
                + ",\"language\":" + quote(header.language()) + "}";
    }

    private static String quote(String value) {
        StringBuilder result = new StringBuilder().append('"');
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            switch (character) {
                case '"' -> result.append("\\\"");
                case '\\' -> result.append("\\\\");
                case '\n' -> result.append("\\n");
                case '\r' -> result.append("\\r");
                case '\t' -> result.append("\\t");
                case '\b' -> result.append("\\b");
                case '\f' -> result.append("\\f");
                default -> {
                    if (character < 0x20) {
                        result.append("\\u00").append(HexFormat.of().toHexDigits((byte) character));
                    } else {
                        result.append(character);
                    }
                }
            }
        }
        return result.append('"').toString();
    }
}
