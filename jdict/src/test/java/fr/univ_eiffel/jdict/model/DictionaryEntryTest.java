package fr.univ_eiffel.jdict.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class DictionaryEntryTest {
    @Test
    void rejectsInvalidData() {
        Map<String, List<String>> valid = Map.of("nom", List.of("Un mot."));
        assertThrows(NullPointerException.class, () -> new DictionaryEntry(null, valid));
        assertThrows(IllegalArgumentException.class, () -> new DictionaryEntry("", valid));
        assertThrows(IllegalArgumentException.class, () -> new DictionaryEntry(" \t", valid));
        assertThrows(NullPointerException.class, () -> new DictionaryEntry("mot", null));
        assertThrows(IllegalArgumentException.class, () -> new DictionaryEntry("mot", Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new DictionaryEntry("mot", Map.of("nom", List.of())));
        Map<String, List<String>> missingList = new LinkedHashMap<>();
        missingList.put("nom", null);
        assertThrows(NullPointerException.class, () -> new DictionaryEntry("mot", missingList));
    }

    @Test
    void keepsTypeOrderAndCopiesLists() {
        List<String> verbs = new ArrayList<>(List.of("Faire."));
        Map<String, List<String>> definitions = new LinkedHashMap<>();
        definitions.put("verbe", verbs);
        definitions.put("nom", List.of("Un mot."));
        DictionaryEntry entry = new DictionaryEntry("mot", definitions);
        verbs.clear();
        definitions.clear();

        assertEquals(List.of("verbe", "nom"), new ArrayList<>(entry.definitions().keySet()));
        assertEquals(List.of("Faire."), entry.definitions().get("verbe"));
        assertThrows(UnsupportedOperationException.class, () -> entry.definitions().clear());
        assertThrows(UnsupportedOperationException.class, () -> entry.definitions().get("verbe").clear());
    }
}
