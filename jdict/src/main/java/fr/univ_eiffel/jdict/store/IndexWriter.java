package fr.univ_eiffel.jdict.store;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/** Écrit un index à partir d'entrées déjà triées. */
public interface IndexWriter {
    /** Écrit les positions des entrées dans leur ordre de tri. */
    void write(Path file, List<IndexEntry> sortedEntries) throws IOException;
}
