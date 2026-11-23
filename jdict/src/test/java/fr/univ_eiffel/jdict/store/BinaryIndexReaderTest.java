package fr.univ_eiffel.jdict.store;

import fr.univ_eiffel.jdict.model.DictionaryFormatException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BinaryIndexReaderTest {
    @Test
    void readsRangesByPosition(@TempDir Path directory) throws IOException {
        Path file = Files.write(directory.resolve("definitions.index"),
                HexFormat.of().parseHex(BinaryIndexWriterTest.EXAMPLE));
        try (IndexReader index = new BinaryIndexReader(file)) {
            assertEquals(2, index.size());
            assertEquals(new EntryRange(515, 1100), index.range(0));
            assertEquals(new EntryRange(0, 514), index.range(1));
            assertThrows(IndexOutOfBoundsException.class, () -> index.range(2));
            assertThrows(IndexOutOfBoundsException.class, () -> index.range(-1));
        }
    }

    @Test
    void rejectsBadHeadersAndSizesAndAcceptsAnEmptyIndex(@TempDir Path directory) throws IOException {
        Path file = directory.resolve("definitions.index");
        for (String invalid : List.of("", "DICTINDY", "DICTINDX12345")) {
            Files.writeString(file, invalid, StandardCharsets.US_ASCII);
            DictionaryFormatException error = assertThrows(DictionaryFormatException.class,
                    () -> new BinaryIndexReader(file));
            assertTrue(error.getMessage().contains("definitions.index"));
            assertTrue(StandardCharsets.US_ASCII.newEncoder().canEncode(error.getMessage()));
        }

        Files.writeString(file, "DICTINDX", StandardCharsets.US_ASCII);
        try (IndexReader index = new BinaryIndexReader(file)) {
            assertEquals(0, index.size());
        }
    }
}
