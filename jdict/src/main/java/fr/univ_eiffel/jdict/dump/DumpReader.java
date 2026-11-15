package fr.univ_eiffel.jdict.dump;

import fr.univ_eiffel.jdict.model.DictionaryFormatException;
import fr.univ_eiffel.jdict.model.WikiPage;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.function.Consumer;

/** Lit les pages de l'espace principal d'un dump MediaWiki. */
public final class DumpReader implements Closeable {
    private final InputStream input;
    private boolean started;

    /** Prend en charge un flux XML déjà décompressé. */
    public DumpReader(InputStream xml) {
        input = Objects.requireNonNull(xml, "Le flux XML est obligatoire.");
    }

    /** Ouvre un flux XML compressé en bzip2. */
    public static DumpReader fromBzip2(InputStream compressed) throws IOException {
        BufferedInputStream buffered = new BufferedInputStream(
                Objects.requireNonNull(compressed, "Le flux bzip2 est obligatoire."));
        try {
            return new DumpReader(new BZip2CompressorInputStream(buffered, true));
        } catch (IOException cause) {
            DictionaryFormatException error =
                    new DictionaryFormatException("Impossible d'ouvrir le flux bzip2.", cause);
            try {
                buffered.close();
            } catch (IOException closeError) {
                error.addSuppressed(closeError);
            }
            throw error;
        }
    }

    /** Transmet une seule fois les pages de l'espace 0 dans l'ordre du dump. */
    public void forEachPage(Consumer<WikiPage> consumer) throws IOException {
        Objects.requireNonNull(consumer, "Le traitement des pages est obligatoire.");
        if (started) {
            throw new IllegalStateException("Le dump a déjà été parcouru.");
        }
        started = true;

        XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
        factory.setProperty(XMLInputFactory.IS_COALESCING, true);
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        // Un dump complet dépasse les limites par défaut du JDK sur les entités (&quot;, &amp;, &lt;) :
        // 0 retire la limite sur le cumul et sur la taille d'une page (limites présentes selon la version du JDK).
        factory.setProperty("jdk.xml.totalEntitySizeLimit", 0);
        factory.setProperty("jdk.xml.maxGeneralEntitySizeLimit", 0);

        try {
            XMLStreamReader xml = factory.createXMLStreamReader(input, StandardCharsets.UTF_8.name());
            try {
                readPages(xml, consumer);
            } finally {
                xml.close();
            }
        } catch (XMLStreamException cause) {
            throw new DictionaryFormatException("Le XML du dump est invalide.", cause);
        }
    }

    private static void readPages(XMLStreamReader xml, Consumer<WikiPage> consumer)
            throws XMLStreamException, DictionaryFormatException {
        int depth = 0;
        while (xml.hasNext()) {
            int event = xml.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if (depth == 1 && "page".equals(xml.getLocalName())) {
                    readPage(xml, consumer);
                } else {
                    depth++;
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                depth--;
            }
        }
    }

    private static void readPage(XMLStreamReader xml, Consumer<WikiPage> consumer)
            throws XMLStreamException, DictionaryFormatException {
        ArrayDeque<String> elements = new ArrayDeque<>();
        elements.push("page");
        String title = null;
        String namespace = null;
        String text = "";

        while (!elements.isEmpty()) {
            int event = xml.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String name = xml.getLocalName();
                // getElementText consomme aussi la balise de fermeture.
                if (elements.size() == 1 && "title".equals(name)) {
                    title = xml.getElementText();
                } else if (elements.size() == 1 && "ns".equals(name)) {
                    namespace = xml.getElementText().strip();
                } else if (elements.size() == 2 && "revision".equals(elements.peek()) && "text".equals(name)) {
                    text = xml.getElementText();
                } else {
                    elements.push(name);
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                elements.pop();
            }
        }

        if ("0".equals(namespace)) {
            if (title == null) {
                throw new DictionaryFormatException("Une page de l'espace 0 est sans titre.");
            }
            consumer.accept(new WikiPage(title, text));
        }
    }

    /** Ferme le flux fourni au lecteur. */
    @Override
    public void close() throws IOException {
        input.close();
    }
}
