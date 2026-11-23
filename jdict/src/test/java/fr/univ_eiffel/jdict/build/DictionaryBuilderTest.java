package fr.univ_eiffel.jdict.build;

import fr.univ_eiffel.jdict.dump.DumpReader;
import fr.univ_eiffel.jdict.language.FrenchExtractor;
import fr.univ_eiffel.jdict.model.DefinitionFileHeader;
import fr.univ_eiffel.jdict.store.DefinitionFileWriter;
import fr.univ_eiffel.jdict.store.JsonEntrySerializer;
import fr.univ_eiffel.jdict.store.PlainCodec;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

final class DictionaryBuilderTest {
    @Test
    void keepsSevenEntriesInDumpOrder(@TempDir Path directory) throws IOException {
        Path file = directory.resolve("definitions.dict");
        DefinitionFileHeader header = DefinitionFileHeader.forLanguage("fr", Instant.EPOCH);
        InputStream xml = DictionaryBuilderTest.class.getClassLoader().getResourceAsStream("mini-dump-fr.xml");
        IndexBuilder index = new IndexBuilder();
        try (DumpReader reader = new DumpReader(xml);
                DefinitionFileWriter writer = new DefinitionFileWriter(file, header, new JsonEntrySerializer(), new PlainCodec())) {
            DictionaryBuilder.BuildReport report =
                    new DictionaryBuilder().build(reader, new FrenchExtractor(), writer, index,
                            count -> fail("Progression inattendue"));
            assertEquals(new DictionaryBuilder.BuildReport(11, 7), report);
            assertEquals(report.entries(), index.size());
        }

        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        assertEquals(8, lines.size());
        List<String> titles = new ArrayList<>();
        for (String line : lines.subList(1, lines.size())) {
            titles.add(new JSONObject(line).getString("title"));
        }
        assertEquals(List.of("plage", "congr\u00e8s", "Congr\u00e8s", "congres", "aujourd'hui", "tour", "pomme"), titles);
        assertPlage(lines.get(1));
        assertEquals(List.of("''Pluriel de'' [[congre#fr|congre]]."),
                definitions(lines.get(4)).getJSONArray("nom").toList());
        JSONObject tour = definitions(lines.get(6));
        assertEquals(List.of("{{lexique|g\u00e9om\u00e9trie|fr}} D\u00e9placement circulaire.",
                "[[construction|Construction]] plus haute que large."), tour.getJSONArray("nom").toList());
        assertEquals(List.of("Faire un tour."), tour.getJSONArray("verbe").toList());
        JSONObject pomme = definitions(lines.get(7));
        assertEquals(List.of("[[fruit|Fruit]] du [[pommier]]."), pomme.getJSONArray("nom").toList());
    }

    private static void assertPlage(String line) {
        assertTrue(line.indexOf("\"nom\"") < line.indexOf("\"verbe\""));
        JSONObject definitions = definitions(line);
        assertEquals(2, definitions.length());
        assertEquals(List.of("[[\u00e9tendue|\u00c9tendue]] de [[sable]] ou de [[galet]]s bordant un plan d\u2019[[eau]].",
                "{{lexique|math\u00e9matiques|fr}} Partie d\u2019un tout constituant un sous-ensemble coh\u00e9rent et contigu."),
                definitions.getJSONArray("nom").toList());
        assertEquals(List.of(
                "''Premi\u00e8re personne du singulier de l\u2019indicatif pr\u00e9sent de'' [[plager]].",
                "''Troisi\u00e8me personne du singulier de l\u2019indicatif pr\u00e9sent de'' [[plager]]."),
                definitions.getJSONArray("verbe").toList());
    }

    private static JSONObject definitions(String line) {
        return new JSONObject(line).getJSONObject("definitions");
    }
}
