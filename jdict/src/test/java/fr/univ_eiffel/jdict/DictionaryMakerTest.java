package fr.univ_eiffel.jdict;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class DictionaryMakerTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-16T08:30:00Z"), ZoneOffset.UTC);

    @Test
    void buildsDictionaryFromBzip2(@TempDir Path directory) throws IOException {
        Path base = directory.resolve("definitions");
        ByteArrayOutputStream diagnostics = new ByteArrayOutputStream();
        assertEquals(0, run(new String[]{"fr", base.toString()}, compressedMiniDump(), diagnostics));

        List<String> lines = Files.readAllLines(directory.resolve("definitions.dict"), StandardCharsets.UTF_8);
        assertEquals(8, lines.size());
        JSONObject header = new JSONObject(lines.get(0));
        assertEquals("fr", header.getString("language"));
        assertEquals("20261116T083000Z", header.getString("created_on"));
        assertEquals("Termine : 11 pages lues, 7 entrees ecrites\n", diagnostics.toString(StandardCharsets.US_ASCII));
    }

    @Test
    void reportsInvalidArgumentsAndBzip2(@TempDir Path directory) {
        String base = directory.resolve("definitions").toString();
        assertError(2, "Usage : DictionaryMaker", new String[]{"fr"});
        assertError(2, "Langue non prise en charge : xx (langues : fr)", new String[]{"xx", base});
        assertError(1, "Erreur : ", new String[]{"fr", base});
    }

    @Test
    void writesIdenticalFilesWithFixedClock(@TempDir Path directory) throws IOException {
        byte[] compressed = compressedMiniDump();
        String first = directory.resolve("premier").toString();
        String second = directory.resolve("second").toString();
        assertEquals(0, run(new String[]{"fr", first}, compressed, new ByteArrayOutputStream()));
        assertEquals(0, run(new String[]{"fr", second}, compressed, new ByteArrayOutputStream()));
        assertArrayEquals(Files.readAllBytes(directory.resolve("premier.dict")),
                Files.readAllBytes(directory.resolve("second.dict")));
    }

    private static void assertError(int code, String message, String[] args) {
        ByteArrayOutputStream diagnostics = new ByteArrayOutputStream();
        byte[] invalid = "pas du bzip2".getBytes(StandardCharsets.UTF_8);
        assertEquals(code, run(args, invalid, diagnostics));
        String actual = diagnostics.toString(StandardCharsets.UTF_8);
        assertTrue(actual.startsWith(message));
        assertArrayEquals(diagnostics.toByteArray(), actual.getBytes(StandardCharsets.US_ASCII));
    }

    private static int run(String[] args, byte[] input, ByteArrayOutputStream diagnostics) {
        try (PrintStream err = new PrintStream(diagnostics, true, StandardCharsets.UTF_8)) {
            return DictionaryMaker.run(args, new ByteArrayInputStream(input), err, CLOCK);
        }
    }

    private static byte[] compressedMiniDump() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (InputStream xml = DictionaryMakerTest.class.getClassLoader().getResourceAsStream("mini-dump-fr.xml");
                BZip2CompressorOutputStream compressed = new BZip2CompressorOutputStream(bytes)) {
            xml.transferTo(compressed);
        }
        return bytes.toByteArray();
    }
}
