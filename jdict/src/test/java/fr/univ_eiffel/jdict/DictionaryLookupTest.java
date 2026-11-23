package fr.univ_eiffel.jdict;

import fr.univ_eiffel.jdict.build.IndexBuilder;
import fr.univ_eiffel.jdict.model.DefinitionFileHeader;
import fr.univ_eiffel.jdict.model.DictionaryEntry;
import fr.univ_eiffel.jdict.search.DictionaryLookup;
import fr.univ_eiffel.jdict.search.SearchQuery;
import fr.univ_eiffel.jdict.store.BinaryIndexWriter;
import fr.univ_eiffel.jdict.store.DefinitionFileWriter;
import fr.univ_eiffel.jdict.store.JsonEntrySerializer;
import fr.univ_eiffel.jdict.store.PlainCodec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class DictionaryLookupTest {
    static final List<String> SMALL_TITLES = List.of("plage", "congr\u00e8s", "Congr\u00e8s", "congres",
            "aujourd'hui", "presqu\u2019\u00eele");

    @Test
    void findsExactTitlesAndAllNormalizedMatches(@TempDir Path directory) throws IOException {
        Path base = writeDictionary(directory, SMALL_TITLES);
        try (DictionaryLookup lookup = DictionaryLookup.open(base)) {
            for (String title : List.of("congres", "congr\u00e8s", "Congr\u00e8s", "plage")) {
                assertEquals(List.of(jsonLine(title)), lookup.find(SearchQuery.parse(title)));
            }
            List<String> expected = List.of(jsonLine("Congr\u00e8s"), jsonLine("congres"), jsonLine("congr\u00e8s"));
            assertEquals(expected, lookup.find(SearchQuery.parse("CONGRES")));
            assertEquals(expected, lookup.find(SearchQuery.parse("CONGR\u00c8S")));
            assertEquals(List.of(jsonLine("plage")), lookup.find(SearchQuery.parse("PLAGE")));
        }
    }

    @Test
    void doesNotReturnNeighborsForMissingTitles(@TempDir Path directory) throws IOException {
        try (DictionaryLookup lookup = DictionaryLookup.open(writeDictionary(directory, SMALL_TITLES))) {
            for (String word : List.of("zebre", "ZEBRE", "a", "zzz", "congre")) {
                assertTrue(lookup.find(SearchQuery.parse(word)).isEmpty(), word);
            }
        }
    }

    @Test
    void findsBothApostropheFormsInQueries(@TempDir Path directory) throws IOException {
        try (DictionaryLookup lookup = DictionaryLookup.open(writeDictionary(directory, SMALL_TITLES))) {
            assertEquals(List.of(jsonLine("presqu\u2019\u00eele")), lookup.find(SearchQuery.parse("presqu'\u00eele")));
            assertEquals(List.of(jsonLine("aujourd'hui")), lookup.find(SearchQuery.parse("aujourd\u2019hui")));
            assertEquals(List.of(jsonLine("presqu\u2019\u00eele")), lookup.find(SearchQuery.parse("PRESQU'ILE")));
        }
    }

    @Test
    void findsAllWordsWrittenInReverseOrder(@TempDir Path directory) throws IOException {
        List<String> titles = new ArrayList<>();
        for (int i = 999; i >= 0; i--) {
            titles.add(String.format(Locale.ROOT, "m%04d", i));
        }
        try (DictionaryLookup lookup = DictionaryLookup.open(writeDictionary(directory, titles))) {
            for (String title : titles) {
                assertEquals(List.of(jsonLine(title)), lookup.find(SearchQuery.parse(title)));
            }
            assertEquals(List.of(jsonLine("m0500")), lookup.find(SearchQuery.parse("M0500")));
            assertTrue(lookup.find(SearchQuery.parse("m1000")).isEmpty());
            assertTrue(lookup.find(SearchQuery.parse("m")).isEmpty());
        }
    }

    @Test
    void handlesAnEmptyIndex(@TempDir Path directory) throws IOException {
        try (DictionaryLookup lookup = DictionaryLookup.open(writeDictionary(directory, List.of()))) {
            assertTrue(lookup.find(SearchQuery.parse("congres")).isEmpty());
            assertTrue(lookup.find(SearchQuery.parse("CONGRES")).isEmpty());
        }
    }

    static Path writeDictionary(Path directory, List<String> titles) throws IOException {
        Path base = directory.resolve("definitions");
        IndexBuilder index = new IndexBuilder();
        try (DefinitionFileWriter writer = new DefinitionFileWriter(Path.of(base + ".dict"),
                DefinitionFileHeader.forLanguage("fr", Instant.EPOCH), new JsonEntrySerializer(), new PlainCodec())) {
            for (String title : titles) {
                index.add(writer.write(entry(title)));
            }
        }
        new BinaryIndexWriter().write(Path.of(base + ".index"), index.sortedEntries());
        return base;
    }

    static String jsonLine(String title) {
        return new JsonEntrySerializer().serialize(entry(title));
    }

    private static DictionaryEntry entry(String title) {
        return new DictionaryEntry(title, Map.of("nom", List.of("D\u00e9finition de " + title + ".")));
    }
}
