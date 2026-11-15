package fr.univ_eiffel.jdict.dump;

import fr.univ_eiffel.jdict.model.DictionaryFormatException;
import fr.univ_eiffel.jdict.model.WikiPage;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class DumpReaderTest {
    // Les échappements Unicode fixent les caractères attendus quel que soit l'encodage du source.
    private static final String PLAGE = """
            == {{langue|fr}} ==
            === {{S|nom|fr}} ===
            '''plage''' {{pron|pla\u0292|fr}} {{f}}
            # [[\u00e9tendue|\u00c9tendue]] de [[sable]] ou de [[galet]]s bordant un plan d\u2019[[eau]].
            #* ''Une plage de sable fin.''
            # Partie d\u2019un tout, a & b, <ref>note</ref>.
            == {{langue|de}} ==
            === {{S|verbe|de}} ===
            # [[plagen|Conjugaison]] allemande.""";

    @Test
    void followsDocumentOrder() throws IOException {
        List<String> titles = new ArrayList<>();
        for (WikiPage page : readMiniDump()) {
            titles.add(page.title());
        }
        assertEquals(List.of("plage", "congr\u00e8s", "Congr\u00e8s", "congres", "aujourd'hui",
                "c\u0153ur", "ancien", "vide"), titles);
    }

    @Test
    void ignoresOtherNamespaces() throws IOException {
        for (WikiPage page : readMiniDump()) {
            assertFalse(List.of("Discussion:plage", "Utilisateur:Exemple", "Wiktionnaire:Accueil")
                    .contains(page.title()));
        }
    }

    @Test
    void decodesEntitiesAndKeepsUnicodeAndLines() throws IOException {
        assertEquals(PLAGE, readMiniDump().get(0).text());
    }

    @Test
    void readsEmptyText() throws IOException {
        assertEquals(new WikiPage("vide", ""), readMiniDump().get(7));
    }

    @Test
    void transmitsRedirections() throws IOException {
        WikiPage page = readMiniDump().get(6);
        assertEquals("ancien", page.title());
        assertTrue(page.text().startsWith("#REDIRECTION"));
    }

    @Test
    void readsBzip2() throws IOException {
        byte[] xml;
        try (InputStream resource = miniDump()) {
            xml = resource.readAllBytes();
        }

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (BZip2CompressorOutputStream compressed = new BZip2CompressorOutputStream(bytes)) {
            compressed.write(xml);
        }
        assertEquals(readMiniDump(), collect(DumpReader.fromBzip2(new ByteArrayInputStream(bytes.toByteArray()))));
    }

    @Test
    void readsConcatenatedBzip2Members() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (BZip2CompressorOutputStream first = new BZip2CompressorOutputStream(bytes)) {
            first.write("<mediawiki><page><title>mot</title>".getBytes(StandardCharsets.UTF_8));
        }
        try (BZip2CompressorOutputStream second = new BZip2CompressorOutputStream(bytes)) {
            second.write("<ns>0</ns></page></mediawiki>".getBytes(StandardCharsets.UTF_8));
        }
        assertEquals(List.of(new WikiPage("mot", "")),
                collect(DumpReader.fromBzip2(new ByteArrayInputStream(bytes.toByteArray()))));
    }

    @Test
    void rejectsNonBzip2Input() {
        assertThrows(DictionaryFormatException.class,
                () -> DumpReader.fromBzip2(utf8("ceci n'est pas du bzip2")));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "<mediawiki><page><title>mot</title><ns>0</ns><revision><text>cou",
        "<mediawiki><page><title>mot</ns></page></mediawiki>"
    })
    void wrapsMalformedXml(String xml) {
        DictionaryFormatException error =
                assertThrows(DictionaryFormatException.class, () -> collect(new DumpReader(utf8(xml))));
        assertInstanceOf(XMLStreamException.class, error.getCause());
    }

    @Test
    void selectsFieldsByTheirPositionInThePage() throws IOException {
        String xml = """
                <mediawiki>
                  <page>
                    <title>mot</title><ns>0</ns>
                    <text>ailleurs</text>
                    <revision>
                      <text>bon</text>
                      <contributor><title>autre</title><ns>2</ns><text>autre</text></contributor>
                    </revision>
                  </page>
                  <page><title>sans texte</title><ns>0</ns><revision/></page>
                </mediawiki>
                """;
        assertEquals(List.of(new WikiPage("mot", "bon"), new WikiPage("sans texte", "")),
                collect(new DumpReader(utf8(xml))));
    }

    @Test
    void rejectsUntitledPageInMainNamespace() {
        String xml = "<mediawiki><page><ns>0</ns></page></mediawiki>";
        assertThrows(DictionaryFormatException.class, () -> collect(new DumpReader(utf8(xml))));
    }

    @Test
    void readsMoreEntitiesThanTheDefaultJdkLimit() throws IOException {
        // Le JDK limite par défaut à 50 millions les entités cumulées : un vrai dump les dépasse.
        int pageCount = 60_000;
        String page = "<page><title>mot</title><ns>0</ns><revision><text>"
                + "&lt;".repeat(1_000) + "</text></revision></page>";
        InputStream xml = new SequenceInputStream(Collections.enumeration(List.of(
                utf8("<mediawiki>"),
                new RepeatedBytes(page.getBytes(StandardCharsets.UTF_8), pageCount),
                utf8("</mediawiki>"))));

        long[] pagesRead = {0};
        try (DumpReader reader = new DumpReader(xml)) {
            reader.forEachPage(read -> pagesRead[0]++);
        }
        assertEquals(pageCount, pagesRead[0]);
    }

    private static List<WikiPage> readMiniDump() throws IOException {
        return collect(new DumpReader(miniDump()));
    }

    private static InputStream miniDump() {
        return DumpReaderTest.class.getClassLoader().getResourceAsStream("mini-dump.xml");
    }

    private static InputStream utf8(String text) {
        return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    }

    private static List<WikiPage> collect(DumpReader reader) throws IOException {
        try (reader) {
            List<WikiPage> pages = new ArrayList<>();
            reader.forEachPage(pages::add);
            return pages;
        }
    }

    /** Répète un bloc d'octets un nombre donné de fois, sans le garder en mémoire. */
    private static final class RepeatedBytes extends InputStream {
        private final byte[] block;
        private long remaining;
        private int position;

        private RepeatedBytes(byte[] block, long repetitions) {
            this.block = block;
            this.remaining = repetitions;
        }

        @Override
        public int read() {
            if (remaining == 0) {
                return -1;
            }
            int value = block[position++] & 0xFF;
            if (position == block.length) {
                position = 0;
                remaining--;
            }
            return value;
        }

        @Override
        public int read(byte[] target, int offset, int length) {
            if (remaining == 0) {
                return -1;
            }
            int count = Math.min(length, block.length - position);
            System.arraycopy(block, position, target, offset, count);
            position += count;
            if (position == block.length) {
                position = 0;
                remaining--;
            }
            return count;
        }
    }
}
