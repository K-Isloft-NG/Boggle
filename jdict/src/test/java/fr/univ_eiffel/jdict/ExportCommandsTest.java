package fr.univ_eiffel.jdict;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ExportCommandsTest {
    private static final String HEADER = """
            {"description":"definition file","created_on":"20261117T090000Z","language":"fr"}
            """;
    private static final String WORDS = "A\nAESIR\nCA\nCOEUR\nCONGRES\nMR\nPARIS\nPLAGE\nSNCF\nZEBRE\n";

    @Test
    void exportsAllWordsOrSelectedTypes(@TempDir Path directory) throws IOException {
        String base = copyDefinitions(directory);
        Result all = capture(NormalizedExtractor::run, base);
        assertEquals(0, all.code());
        assertArrayEquals(WORDS.getBytes(StandardCharsets.US_ASCII), all.output());
        assertEquals("Termine : 18 entrees lues, 10 mots ecrits\n", all.diagnostics());

        Result selected = capture(NormalizedExtractor::run, base, " nom-pr , sigle ,");
        assertEquals(0, selected.code());
        assertEquals("AESIR\nPARIS\nSNCF\n", selected.text());
    }

    @Test
    void reportsWordExportErrors(@TempDir Path directory) throws IOException {
        String base = directory.resolve("echec").toString();
        assertError(2, "Usage : NormalizedExtractor", capture(NormalizedExtractor::run));
        assertError(2, "Usage : NormalizedExtractor", capture(NormalizedExtractor::run, base, "nom", "verbe"));
        Result missing = capture(NormalizedExtractor::run, base);
        assertError(1, "Erreur : ", missing);
        assertTrue(missing.diagnostics().contains("echec.dict"));

        Files.writeString(Path.of(base + ".dict"), HEADER + "invalide\n", StandardCharsets.UTF_8);
        assertError(1, "Erreur : Entree invalide a la ligne 2", capture(NormalizedExtractor::run, base));
    }

    @Test
    void exportsCharacterWeights(@TempDir Path directory) throws IOException {
        String base = createCharacterFixture(directory);
        Result result = capture(CharTableMaker::run, base);
        assertEquals(0, result.code());
        assertEquals("A 6000\nB 4000\n", result.text());
        assertEquals("Termine : 1 entrees lues, 2 lignes ecrites\n", result.diagnostics());

        assertError(2, "Usage : CharTableMaker", capture(CharTableMaker::run, base, "nom"));
        assertError(1, "Erreur : ", capture(CharTableMaker::run, directory.resolve("absent").toString()));
    }

    @Test
    void writesSqlFromDictionaryHeaderAndNonemptyWordLines(@TempDir Path directory) throws IOException {
        String base = createHeaderFixture(directory);
        Path words = Files.writeString(directory.resolve("words.txt"), "ABC\n\nPLAGE\nTOUR\n", StandardCharsets.UTF_8);
        Result result = capture(SqlScriptMaker::run, base, words.toString(), "20261115", "Fran\u00e7ais (Wiktionnaire)");
        assertEquals(0, result.code());
        assertEquals("""
                -- Dictionnaire de la langue fr, version 20261115, 3 mots.
                -- Script genere par SqlScriptMaker.
                INSERT INTO boggle.dictionnaire (code_langue, libelle, version, nb_mots)
                VALUES ('fr', 'Fran\u00e7ais (Wiktionnaire)', '20261115', 3)
                ON CONFLICT (code_langue, version) DO NOTHING;
                """, result.text());
        assertEquals("Termine : script ecrit pour 3 mots\n", result.diagnostics());

        Result apostrophe = capture(SqlScriptMaker::run, base, words.toString(), "20261115", "L'ami");
        assertEquals(0, apostrophe.code());
        assertTrue(apostrophe.text().contains("'L''ami'"));
    }

    @Test
    void reportsSqlExportErrors(@TempDir Path directory) throws IOException {
        String base = createHeaderFixture(directory);
        String words = directory.resolve("words.txt").toString();
        assertError(2, "Usage : SqlScriptMaker", capture(SqlScriptMaker::run, base, words, "20261115"));
        assertError(1, "Erreur : ", capture(SqlScriptMaker::run, base, words, "20261115", "Francais"));
        Files.writeString(Path.of(words), "\n", StandardCharsets.UTF_8);
        assertError(1, "Erreur : le fichier de mots est vide.", capture(SqlScriptMaker::run,
                base, words, "20261115", "Francais"));
        Files.writeString(Path.of(words), "ABC\n", StandardCharsets.UTF_8);
        assertError(2, "Erreur : ", capture(SqlScriptMaker::run, base, words, "2026", "Francais"));
    }

    @Test
    void exportsWordsAndWeightsFromCompressedMiniDump(@TempDir Path directory) throws IOException {
        String base = directory.resolve("definitions").toString();
        ByteArrayOutputStream diagnostics = new ByteArrayOutputStream();
        try (PrintStream err = new PrintStream(diagnostics, true, StandardCharsets.UTF_8)) {
            assertEquals(0, DictionaryMaker.run(new String[]{"fr", base},
                    new ByteArrayInputStream(compressedMiniDump()), err, Clock.systemUTC()));
        }

        Result words = capture(NormalizedExtractor::run, base);
        assertEquals(0, words.code());
        assertEquals("CONGRES\nPLAGE\nPOMME\nTOUR\n", words.text());

        Result table = capture(CharTableMaker::run, base);
        assertEquals(0, table.code());
        assertTrue(table.text().endsWith("\n"));
        assertFalse(table.text().contains("\r"));
        String previous = "";
        for (String line : table.text().lines().toList()) {
            assertTrue(line.matches("^[A-Z]{1,2} [1-9][0-9]*$"));
            String element = line.substring(0, line.indexOf(' '));
            assertTrue(previous.compareTo(element) < 0);
            previous = element;
        }
    }

    @Test
    void producesIdenticalBytesOnRepeatedRuns(@TempDir Path directory) throws IOException {
        String wordBase = copyDefinitions(directory);
        String tableBase = createCharacterFixture(directory);
        assertRepeatable(NormalizedExtractor::run, wordBase);
        assertRepeatable(CharTableMaker::run, tableBase);
    }

    private static void assertRepeatable(Command command, String base) {
        Result first = capture(command, base);
        Result second = capture(command, base);
        assertEquals(List.of(0, 0), List.of(first.code(), second.code()));
        assertArrayEquals(first.output(), second.output());
    }

    private static void assertError(int code, String diagnostic, Result result) {
        assertEquals(code, result.code());
        assertTrue(result.diagnostics().startsWith(diagnostic));
        assertTrue(StandardCharsets.US_ASCII.newEncoder().canEncode(result.diagnostics()));
        assertEquals(0, result.output().length);
    }

    private static String createHeaderFixture(Path directory) throws IOException {
        Files.writeString(directory.resolve("entete.dict"), HEADER + "entree volontairement invalide\n", StandardCharsets.UTF_8);
        return directory.resolve("entete").toString();
    }

    private static String createCharacterFixture(Path directory) throws IOException {
        Files.writeString(directory.resolve("lettres.dict"), HEADER + """
                {"title":"aab","definitions":{"nom":["{{lexique|fr}} [[x|ba]]"]}}
                """, StandardCharsets.UTF_8);
        return directory.resolve("lettres").toString();
    }

    private static String copyDefinitions(Path directory) throws IOException {
        try (InputStream fixture = ExportCommandsTest.class.getClassLoader().getResourceAsStream("mini-definitions-fr.dict")) {
            Files.copy(fixture, directory.resolve("definitions.dict"));
        }
        return directory.resolve("definitions").toString();
    }

    private static byte[] compressedMiniDump() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (InputStream fixture = ExportCommandsTest.class.getClassLoader().getResourceAsStream("mini-dump-fr.xml");
                BZip2CompressorOutputStream compressed = new BZip2CompressorOutputStream(bytes)) {
            fixture.transferTo(compressed);
        }
        return bytes.toByteArray();
    }

    private static Result capture(Command command, String... args) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream diagnostics = new ByteArrayOutputStream();
        try (PrintStream err = new PrintStream(diagnostics, true, StandardCharsets.UTF_8)) {
            int code = command.run(args, out, err);
            return new Result(code, out.toByteArray(), diagnostics.toString(StandardCharsets.UTF_8));
        }
    }

    private interface Command {
        int run(String[] args, OutputStream out, PrintStream err);
    }

    private record Result(int code, byte[] output, String diagnostics) {
        private String text() {
            return new String(output, StandardCharsets.UTF_8);
        }
    }
}
