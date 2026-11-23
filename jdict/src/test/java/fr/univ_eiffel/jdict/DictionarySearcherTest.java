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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class DictionarySearcherTest {
    @Test
    void outputsStoredJsonOrUtf8Yaml(@TempDir Path directory) throws IOException {
        String base = DictionaryLookupTest.writeDictionary(directory, DictionaryLookupTest.SMALL_TITLES).toString();
        Result exact = search(base, "congres");
        assertEquals(0, exact.code());
        assertArrayEquals((DictionaryLookupTest.jsonLine("congres") + "\n").getBytes(StandardCharsets.UTF_8), exact.output());
        assertEquals("", exact.diagnostic());
        assertTitles(search(base, "CONGRES"), List.of("Congr\u00e8s", "congres", "congr\u00e8s"));

        Result yaml = search(base, "yaml:congr\u00e8s");
        assertEquals(0, yaml.code());
        assertTrue(yaml.text().startsWith("---\ntitle: \"congr\u00e8s\"\n"));
        assertArrayEquals(yaml.text().getBytes(StandardCharsets.UTF_8), yaml.output());
        assertError(3, "Aucune entree trouvee", search(base, "zebre"));
    }

    @Test
    void reportsUsageAndFileErrors(@TempDir Path directory) throws IOException {
        String base = directory.resolve("definitions").toString();
        assertError(2, "Usage : DictionarySearcher", search(base));
        assertError(2, "Usage : DictionarySearcher", search(base, "congres", "plage"));
        assertError(2, "Requete vide", search(base, "yaml:"));
        assertError(1, "Erreur : ", search(base, "congres"));

        Path index = directory.resolve("definitions.index");
        Files.writeString(index, "DICTINDX", StandardCharsets.US_ASCII);
        assertError(1, "Erreur : ", search(base, "congres"));

        Files.writeString(index, "DICTINDY", StandardCharsets.US_ASCII);
        assertError(1, "Erreur : Index invalide", search(base, "congres"));
    }

    @Test
    void searchesTheDictionaryBuiltFromTheCompressedMiniDump(@TempDir Path directory) throws IOException {
        String base = directory.resolve("definitions").toString();
        ByteArrayOutputStream diagnostics = new ByteArrayOutputStream();
        try (PrintStream err = new PrintStream(diagnostics, true, StandardCharsets.UTF_8)) {
            assertEquals(0, DictionaryMaker.run(new String[]{"fr", base},
                    new ByteArrayInputStream(compressedMiniDump()), err, Clock.systemUTC()));
        }
        assertEquals(64, Files.size(directory.resolve("definitions.index")));
        assertTitles(search(base, "CONGRES"), List.of("Congr\u00e8s", "congres", "congr\u00e8s"));
        assertTitles(search(base, "aujourd\u2019hui"), List.of("aujourd'hui"));
        assertTitles(search(base, "tour"), List.of("tour"));
        assertError(3, "Aucune entree trouvee", search(base, "dog"));
    }

    private static void assertTitles(Result result, List<String> expected) {
        assertEquals(0, result.code());
        List<String> titles = new ArrayList<>();
        for (String line : result.text().lines().toList()) {
            titles.add(new JSONObject(line).getString("title"));
        }
        assertEquals(expected, titles);
    }

    private static void assertError(int code, String diagnostic, Result result) {
        assertEquals(code, result.code());
        assertTrue(result.diagnostic().startsWith(diagnostic));
        assertTrue(StandardCharsets.US_ASCII.newEncoder().canEncode(result.diagnostic()));
        assertEquals(0, result.output().length);
    }

    private static Result search(String... args) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream diagnostics = new ByteArrayOutputStream();
        try (PrintStream err = new PrintStream(diagnostics, true, StandardCharsets.UTF_8)) {
            int code = DictionarySearcher.run(args, out, err);
            return new Result(code, out.toByteArray(), diagnostics.toString(StandardCharsets.UTF_8));
        }
    }

    private static byte[] compressedMiniDump() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (InputStream xml = DictionarySearcherTest.class.getClassLoader().getResourceAsStream("mini-dump-fr.xml");
                BZip2CompressorOutputStream compressed = new BZip2CompressorOutputStream(bytes)) {
            xml.transferTo(compressed);
        }
        return bytes.toByteArray();
    }

    private record Result(int code, byte[] output, String diagnostic) {
        private String text() {
            return new String(output, StandardCharsets.UTF_8);
        }
    }
}
