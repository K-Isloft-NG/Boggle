package fr.univ_eiffel.jdict.store;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

final class BinaryIndexWriterTest {
    static final String EXAMPLE = "44494354494e4458000002030000044c0000000000000202";

    @Test
    void writesExactBigEndianBytes(@TempDir Path directory) throws IOException {
        Path file = directory.resolve("out").resolve("definitions.index");
        IndexWriter writer = new BinaryIndexWriter();
        writer.write(file, List.of(new IndexEntry("congres", 515, 1100), new IndexEntry("congr\u00e8s", 0, 514)));
        assertArrayEquals(HexFormat.of().parseHex(EXAMPLE), Files.readAllBytes(file));
    }
}
