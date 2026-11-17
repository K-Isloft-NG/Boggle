package fr.univ_eiffel.jdict.export;

import fr.univ_eiffel.jdict.store.DefinitionFileReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

final class NormalizedWordExporterTest {
    @Test
    void writesSortedDistinctWordsWithoutClosingOutput(@TempDir Path directory) throws IOException {
        NormalizedWordExporter exporter = collect(copyFixture(directory), Set.of());
        TrackingOutput out = new TrackingOutput();

        exporter.writeTo(out);

        assertEquals(10, exporter.size());
        assertArrayEquals("A\nAESIR\nCA\nCOEUR\nCONGRES\nMR\nPARIS\nPLAGE\nSNCF\nZEBRE\n"
                .getBytes(StandardCharsets.US_ASCII), out.toByteArray());
        assertFalse(out.closed);
    }

    @Test
    void selectsExactTypesAndWritesNothingWhenNoneMatch(@TempDir Path directory) throws IOException {
        Path file = copyFixture(directory);
        assertEquals("AESIR\nPARIS\n", render(file, Set.of("nom-pr")));
        assertEquals("SNCF\n", render(file, Set.of("sigle")));
        assertEquals("COEUR\nCONGRES\nMR\nPLAGE\nZEBRE\n", render(file, Set.of("nom", "verbe")));
        assertEquals("PLAGE\n", render(file, Set.of("verbe")));
        assertEquals("", render(file, Set.of("adverbe")));
        assertEquals("", render(file, Set.of("inconnu")));
    }

    private static Path copyFixture(Path directory) throws IOException {
        Path file = directory.resolve("definitions.dict");
        try (InputStream resource = NormalizedWordExporterTest.class.getClassLoader()
                .getResourceAsStream("mini-definitions-fr.dict")) {
            Files.copy(resource, file);
        }
        return file;
    }

    private static NormalizedWordExporter collect(Path file, Set<String> types) throws IOException {
        NormalizedWordExporter exporter = new NormalizedWordExporter(types);
        try (DefinitionFileReader reader = new DefinitionFileReader(file)) {
            reader.forEachEntry(exporter::accept);
        }
        return exporter;
    }

    private static String render(Path file, Set<String> types) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        collect(file, types).writeTo(out);
        return out.toString(StandardCharsets.US_ASCII);
    }

    private static final class TrackingOutput extends ByteArrayOutputStream {
        private boolean closed;

        @Override
        public void close() throws IOException {
            closed = true;
            super.close();
        }
    }
}
