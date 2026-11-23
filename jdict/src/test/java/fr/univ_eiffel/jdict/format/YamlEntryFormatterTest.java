package fr.univ_eiffel.jdict.format;

import fr.univ_eiffel.jdict.model.DictionaryFormatException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class YamlEntryFormatterTest {
    @Test
    void sortsTypesAndPreservesOrderWithinEachType() throws DictionaryFormatException {
        String json = "{\"title\":\"plage\",\"definitions\":{\"verbe\":[\"x\"],"
                + "\"nom\":[\"a \\\"b\\\"\",\"c\\\\d\",\"\u00e9t\u00e9 \u2019\"]}}";
        assertEquals("""
                ---
                title: "plage"
                definitions:
                  "nom":
                    - "a \\"b\\""
                    - "c\\\\d"
                    - "\u00e9t\u00e9 \u2019"
                  "verbe":
                    - "x"
                """, new YamlEntryFormatter().format(json));
    }

    @Test
    void usesOnlyTheSpecifiedControlEscapes() throws DictionaryFormatException {
        String json = "{\"title\":\"a\",\"definitions\":{\"nom\":[\"\\n\\t\\r\\u001f\\b\"]}}";
        assertEquals("""
                ---
                title: "a"
                definitions:
                  "nom":
                    - "\\n\\t\\r\\u001F\\u0008"
                """, new YamlEntryFormatter().format(json));
    }

    @Test
    void rejectsInvalidJson() {
        assertThrows(DictionaryFormatException.class, () -> new YamlEntryFormatter().format("non JSON"));
        assertThrows(DictionaryFormatException.class, () -> new YamlEntryFormatter().format("{\"title\":\"plage\"}"));
    }
}
