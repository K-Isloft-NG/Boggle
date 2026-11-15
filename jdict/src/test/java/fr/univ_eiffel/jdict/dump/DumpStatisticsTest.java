package fr.univ_eiffel.jdict.dump;

import fr.univ_eiffel.jdict.model.WikiPage;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class DumpStatisticsTest {
    @Test
    void summarizesMiniDumpWithThreeLfTerminatedLines() throws IOException {
        InputStream xml = DumpStatisticsTest.class.getClassLoader().getResourceAsStream("mini-dump.xml");
        try (DumpReader reader = new DumpReader(xml)) {
            assertEquals("pages: 8\nredirections: 1\ntexte le plus long: 308\n", DumpStatistics.summarize(reader));
        }
    }

    @Test
    void readsSinglePageLargerThanFiveMillionCharacters() throws IOException {
        // L'échappement Unicode rend le texte attendu indépendant de l'encodage du source.
        String text = "# d\u00e9finition d\u2019essai\n".repeat(260_000);
        String xml = "<mediawiki><page><title>longue</title><ns>0</ns><revision><text>"
                + text + "</text></revision></page></mediawiki>";

        List<WikiPage> pages = new ArrayList<>();
        try (DumpReader reader = new DumpReader(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)))) {
            reader.forEachPage(pages::add);
        }
        assertTrue(text.length() > 5_000_000);
        assertEquals(List.of(new WikiPage("longue", text)), pages);
    }

    @Test
    void countsRedirectsRegardlessOfCase() throws IOException {
        String xml = """
                <mediawiki>
                  <page><title>mot</title><ns>0</ns><revision><text>#rEdIrEcT [[cible]]</text></revision></page>
                </mediawiki>
                """;
        try (DumpReader reader = new DumpReader(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)))) {
            assertEquals("pages: 1\nredirections: 1\ntexte le plus long: 19\n", DumpStatistics.summarize(reader));
        }
    }
}
