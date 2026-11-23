package fr.univ_eiffel.jdict.store;

import fr.univ_eiffel.jdict.model.DefinitionFileHeader;
import fr.univ_eiffel.jdict.model.DictionaryEntry;
import fr.univ_eiffel.jdict.model.DictionaryFormatException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RandomEntryReaderTest {
    @Test
    void readsExactJsonAndTitleAtTheirByteOffsets(@TempDir Path directory) throws IOException {
        Path file = directory.resolve("definitions.dict");
        PlainCodec codec = new PlainCodec();
        JsonEntrySerializer serializer = new JsonEntrySerializer();
        DictionaryEntry congres = new DictionaryEntry("congres", Map.of("nom", List.of("D\u00e9finition.")));
        IndexEntry position;
        try (DefinitionFileWriter writer = new DefinitionFileWriter(file, DefinitionFileHeader.forLanguage("fr", Instant.EPOCH),
                serializer, codec)) {
            writer.write(new DictionaryEntry("congr\u00e8s", Map.of("nom", List.of("Assembl\u00e9e."))));
            position = writer.write(congres);
        }
        try (RandomEntryReader reader = new RandomEntryReader(file, codec)) {
            EntryRange range = new EntryRange(position.start(), position.end());
            assertEquals(serializer.serialize(congres), reader.read(range));
            assertEquals("congres", reader.readTitle(range));
            assertThrows(DictionaryFormatException.class, () -> reader.read(new EntryRange(0, position.end() + 1)));
        }
    }

    @Test
    void reportsTheByteOffsetOfAnInvalidTitle(@TempDir Path directory) throws IOException {
        Path file = Files.writeString(directory.resolve("definitions.dict"), "x{}\n", StandardCharsets.UTF_8);
        try (RandomEntryReader reader = new RandomEntryReader(file, new PlainCodec())) {
            DictionaryFormatException error = assertThrows(DictionaryFormatException.class,
                    () -> reader.readTitle(new EntryRange(1, 3)));
            assertTrue(error.getMessage().contains("octet 1"));
        }
    }
}
