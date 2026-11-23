package fr.univ_eiffel.jdict.store;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Écrit l'en-tête et les positions binaires d'un index DICTINDX. */
public final class BinaryIndexWriter implements IndexWriter {
    public static final String HEADER = "DICTINDX";

    /** Écrit deux entiers grand-boutistes par entrée. */
    @Override
    public void write(Path file, List<IndexEntry> sortedEntries) throws IOException {
        Path parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(file)))) {
            out.write(HEADER.getBytes(StandardCharsets.US_ASCII));
            for (IndexEntry entry : sortedEntries) {
                out.writeInt(entry.start());
                out.writeInt(entry.end());
            }
        }
    }
}
