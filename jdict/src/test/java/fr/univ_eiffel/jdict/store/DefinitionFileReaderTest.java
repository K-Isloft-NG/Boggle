package fr.univ_eiffel.jdict.store;

import fr.univ_eiffel.jdict.model.DefinitionFileHeader;
import fr.univ_eiffel.jdict.model.DictionaryEntry;
import fr.univ_eiffel.jdict.model.DictionaryFormatException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class DefinitionFileReaderTest {
    private static final String HEADER = """
            {"description":"definition file","created_on":"20261117T090000Z","language":"fr"}
            """;
    private static final String ENTRY = """
            {"title":"plage","definitions":{"nom":["Sable."]}}
            """;

    @Test
    void readsHeaderAndAllEntriesInOrder(@TempDir Path directory) throws IOException {
        Path file = directory.resolve("definitions.dict");
        try (InputStream resource = getClass().getClassLoader().getResourceAsStream("mini-definitions-fr.dict")) {
            Files.copy(resource, file);
        }
        List<DictionaryEntry> entries = new ArrayList<>();
        try (DefinitionFileReader reader = new DefinitionFileReader(file)) {
            assertEquals(new DefinitionFileHeader("definition file", "20261117T090000Z", "fr"), reader.header());
            assertEquals(18, reader.forEachEntry(entries::add));
            assertThrows(IllegalStateException.class, () -> reader.forEachEntry(entries::add));
        }
        List<String> titles = new ArrayList<>();
        for (DictionaryEntry entry : entries) {
            titles.add(entry.title());
        }
        assertEquals(List.of("plage", "congr\u00e8s", "Congr\u00e8s", "congres", "Mr", "SNCF",
                "aujourd\u2019hui", "peut-\u00eatre", "pomme de terre", "mp3", "c\u0153ur", "\u00c6sir",
                "M.", "Paris", "\u00e0", "\u00e7a", "z\u00e8bre", "a"), titles);
        assertEquals(Set.of("nom", "verbe"), entries.get(0).definitions().keySet());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "non JSON\n", "{\"description\":\"definition file\",\"created_on\":\"d\"}\n"})
    void rejectsInvalidHeaders(String content, @TempDir Path directory) throws IOException {
        Path file = write(directory, content);
        DictionaryFormatException error = assertThrows(DictionaryFormatException.class,
                () -> new DefinitionFileReader(file));
        assertTrue(error.getMessage().contains("essai.dict"));
        assertAscii(error.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"non JSON", "{\"title\":\"plage\"}", "{\"title\":\"plage\",\"definitions\":{}}",
        "", "{\"title\":\"plage\",\"definitions\":{\"nom\":[3]}}",
        "{\"title\":\"plage\",\"definitions\":{\"nom\":[\"Sable.\"]}} trailing"})
    void reportsInvalidEntryWithLineNumberAndCause(String invalid, @TempDir Path directory) throws IOException {
        Path file = write(directory, HEADER + ENTRY + invalid + "\n" + ENTRY);
        try (DefinitionFileReader reader = new DefinitionFileReader(file)) {
            DictionaryFormatException error = assertThrows(DictionaryFormatException.class,
                    () -> reader.forEachEntry(entry -> { }));
            assertTrue(error.getMessage().contains("ligne 3"));
            assertNotNull(error.getCause());
            assertAscii(error.getMessage());
        }
    }

    private static Path write(Path directory, String content) throws IOException {
        Path file = directory.resolve("essai.dict");
        return Files.writeString(file, content, StandardCharsets.UTF_8);
    }

    private static void assertAscii(String message) {
        assertTrue(StandardCharsets.US_ASCII.newEncoder().canEncode(message));
    }
}
