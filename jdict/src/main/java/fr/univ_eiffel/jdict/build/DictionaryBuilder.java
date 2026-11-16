package fr.univ_eiffel.jdict.build;

import fr.univ_eiffel.jdict.dump.DumpReader;
import fr.univ_eiffel.jdict.language.LanguageExtractor;
import fr.univ_eiffel.jdict.model.DictionaryEntry;
import fr.univ_eiffel.jdict.store.DefinitionFileWriter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.function.LongConsumer;

/** Enchaîne la lecture des pages, l'extraction et l'écriture des définitions. */
public final class DictionaryBuilder {
    private static final int PROGRESS_INTERVAL = 100_000;

    /** Indique le nombre de pages lues et d'entrées écrites. */
    public record BuildReport(long pages, long entries) {
    }

    /** Construit le dictionnaire et signale la progression de la lecture. */
    public BuildReport build(DumpReader reader, LanguageExtractor extractor, DefinitionFileWriter writer,
            LongConsumer progress) throws IOException {
        Counters counts = new Counters();
        try {
            reader.forEachPage(page -> {
                counts.pages++;
                if (counts.pages % PROGRESS_INTERVAL == 0) {
                    progress.accept(counts.pages);
                }
                Optional<DictionaryEntry> entry = extractor.extract(page);
                if (entry.isPresent()) {
                    try {
                        writer.write(entry.get());
                        counts.entries++;
                    } catch (IOException error) {
                        // Le traitement passé au lecteur ne peut pas lever d'exception contrôlée.
                        throw new UncheckedIOException(error);
                    }
                }
            });
        } catch (UncheckedIOException error) {
            throw error.getCause();
        }
        return new BuildReport(counts.pages, counts.entries);
    }

    private static final class Counters {
        private long pages;
        private long entries;
    }
}
