package fr.univ_eiffel.jdict.build;

import fr.univ_eiffel.jdict.model.DefinitionFileHeader;
import fr.univ_eiffel.jdict.model.DictionaryEntry;
import fr.univ_eiffel.jdict.store.DefinitionFileWriter;
import fr.univ_eiffel.jdict.store.IndexEntry;
import fr.univ_eiffel.jdict.store.JsonEntrySerializer;
import fr.univ_eiffel.jdict.store.PlainCodec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class IndexBuilderTest {
    @Test
    void sortsTitlesAndRetainsTheOriginalByteRanges(@TempDir Path directory) throws IOException {
        List<String> titles = List.of("plage", "congr\u00e8s", "Congr\u00e8s", "congres", "aujourd'hui", "presqu\u2019\u00eele");
        List<IndexEntry> written = new ArrayList<>();
        IndexBuilder index = new IndexBuilder();
        try (DefinitionFileWriter writer = new DefinitionFileWriter(directory.resolve("definitions.dict"),
                DefinitionFileHeader.forLanguage("fr", Instant.EPOCH), new JsonEntrySerializer(), new PlainCodec())) {
            for (String title : titles) {
                IndexEntry entry = writer.write(new DictionaryEntry(title, Map.of("nom", List.of("Definition."))));
                written.add(entry);
                index.add(entry);
            }
        }
        assertEquals(6, index.size());
        assertEquals(List.of(written.get(4), written.get(2), written.get(3), written.get(1),
                written.get(0), written.get(5)), index.sortedEntries());
    }
}
