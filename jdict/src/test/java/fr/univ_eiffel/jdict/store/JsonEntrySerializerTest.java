package fr.univ_eiffel.jdict.store;

import fr.univ_eiffel.jdict.model.DefinitionFileHeader;
import fr.univ_eiffel.jdict.model.DictionaryEntry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class JsonEntrySerializerTest {
    private final EntrySerializer serializer = new JsonEntrySerializer();

    @Test
    void keepsWikitextInCompactJson() {
        DictionaryEntry entry = new DictionaryEntry("congres",
                Map.of("nom", List.of("''Pluriel de'' [[congre#fr|congre]].")));
        assertEquals("{\"title\":\"congres\",\"definitions\":{\"nom\":[\"''Pluriel de'' [[congre#fr|congre]].\"]}}",
                serializer.serialize(entry));
    }

    @Test
    void escapesOnlyJsonSpecialCharacters() {
        DictionaryEntry entry = new DictionaryEntry("a\"b", Map.of("nom", List.of("c\\d\ne\t\u0001\u2019/")));
        assertEquals("{\"title\":\"a\\\"b\",\"definitions\":{\"nom\":[\"c\\\\d\\ne\\t\\u0001\u2019/\"]}}",
                serializer.serialize(entry));
    }

    @Test
    void keepsSupplementaryCharactersAndEscapesOtherControls() {
        DictionaryEntry entry = new DictionaryEntry("mot", Map.of("nom", List.of("\r\b\f\u001f\uD801\uDC00")));
        assertEquals("{\"title\":\"mot\",\"definitions\":{\"nom\":[\"\\r\\b\\f\\u001f\uD801\uDC00\"]}}",
                serializer.serialize(entry));
    }

    @Test
    void writesExactHeader() {
        DefinitionFileHeader header = new DefinitionFileHeader("definition file", "20261116T083000Z", "fr");
        assertEquals("{\"description\":\"definition file\",\"created_on\":\"20261116T083000Z\",\"language\":\"fr\"}",
                serializer.serializeHeader(header));
    }
}
