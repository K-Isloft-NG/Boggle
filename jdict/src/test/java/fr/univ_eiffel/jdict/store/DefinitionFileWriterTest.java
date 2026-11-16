package fr.univ_eiffel.jdict.store;

import fr.univ_eiffel.jdict.model.DefinitionFileHeader;
import fr.univ_eiffel.jdict.model.DictionaryEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class DefinitionFileWriterTest {
    @Test
    void recordsContiguousUtf8ByteRanges(@TempDir Path directory) throws IOException {
        Path subdirectory = directory.resolve("sortie");
        Path file = subdirectory.resolve("essai.dict");
        DefinitionFileHeader header = new DefinitionFileHeader("definition file", "20261116T083000Z", "fr");
        String headerLine = "{\"description\":\"definition file\",\"created_on\":\"20261116T083000Z\",\"language\":\"fr\"}\n";
        List<DictionaryEntry> entries = List.of(entry("caf\u00e9"), entry("c\u0153ur"), entry("plage"));
        List<String> lines = List.of(
                "{\"title\":\"caf\u00e9\",\"definitions\":{\"nom\":[\"Texte.\"]}}\n",
                "{\"title\":\"c\u0153ur\",\"definitions\":{\"nom\":[\"Texte.\"]}}\n",
                "{\"title\":\"plage\",\"definitions\":{\"nom\":[\"Texte.\"]}}\n");
        IndexEntry[] positions = new IndexEntry[entries.size()];
        try (DefinitionFileWriter writer = new DefinitionFileWriter(file, header, new JsonEntrySerializer(), new PlainCodec())) {
            for (int i = 0; i < entries.size(); i++) {
                positions[i] = writer.write(entries.get(i));
            }
        }

        assertTrue(Files.isDirectory(subdirectory));
        byte[] stored = Files.readAllBytes(file);
        assertArrayEquals(utf8(headerLine + String.join("", lines)), stored);
        int start = utf8(headerLine).length;
        for (int i = 0; i < positions.length; i++) {
            IndexEntry position = positions[i];
            assertEquals(entries.get(i).title(), position.title());
            assertEquals(start, position.start());
            assertArrayEquals(utf8(lines.get(i)), Arrays.copyOfRange(stored, start, position.end() + 1));
            start = position.end() + 1;
        }
        assertEquals(stored.length, start);
    }

    private static DictionaryEntry entry(String title) {
        return new DictionaryEntry(title, Map.of("nom", List.of("Texte.")));
    }

    private static byte[] utf8(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }
}
