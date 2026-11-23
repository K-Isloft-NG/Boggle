package fr.univ_eiffel.jdict.format;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class JsonEntryFormatterTest {
    @Test
    void preservesOriginalJsonIncludingSpacesAndAccents() {
        String line = "{ \"definitions\": {\"nom\":[\"\u00e9t\u00e9\"]}, \"title\": \"plage\" }";
        assertEquals(line + "\n", new JsonEntryFormatter().format(line));
    }
}
