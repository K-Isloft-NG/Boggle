package fr.univ_eiffel.jdict.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class DefinitionFileHeaderTest {
    @Test
    void formatsCreationTimeInUtc() {
        DefinitionFileHeader header = DefinitionFileHeader.forLanguage("fr", Instant.parse("2026-11-16T08:30:00Z"));
        assertEquals(new DefinitionFileHeader("definition file", "20261116T083000Z", "fr"), header);
    }

    @Test
    void rejectsNullFields() {
        assertThrows(NullPointerException.class, () -> new DefinitionFileHeader(null, "date", "fr"));
        assertThrows(NullPointerException.class, () -> new DefinitionFileHeader("description", null, "fr"));
        assertThrows(NullPointerException.class, () -> new DefinitionFileHeader("description", "date", null));
    }
}
