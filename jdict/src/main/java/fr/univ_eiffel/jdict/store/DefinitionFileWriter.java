package fr.univ_eiffel.jdict.store;

import fr.univ_eiffel.jdict.model.DefinitionFileHeader;
import fr.univ_eiffel.jdict.model.DictionaryEntry;
import fr.univ_eiffel.jdict.model.DictionaryFormatException;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/** Écrit le fichier de définitions et calcule les positions des entrées. */
public final class DefinitionFileWriter implements Closeable {
    private final OutputStream output;
    private final EntrySerializer serializer;
    private final EntryCodec codec;
    private long position;

    /** Ouvre le fichier et écrit la ligne d'en-tête en UTF-8. */
    public DefinitionFileWriter(Path file, DefinitionFileHeader header, EntrySerializer serializer, EntryCodec codec)
            throws IOException {
        this.serializer = Objects.requireNonNull(serializer);
        this.codec = Objects.requireNonNull(codec);
        byte[] headerBytes = (serializer.serializeHeader(header) + "\n").getBytes(StandardCharsets.UTF_8);
        Path parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        output = new BufferedOutputStream(Files.newOutputStream(file, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE));
        try {
            output.write(headerBytes);
        } catch (IOException error) {
            try {
                output.close();
            } catch (IOException closeError) {
                error.addSuppressed(closeError);
            }
            throw error;
        }
        position = headerBytes.length;
    }

    /** Écrit une entrée et renvoie l'intervalle de ses octets dans le fichier. */
    public IndexEntry write(DictionaryEntry entry) throws IOException {
        byte[] line = serializer.serialize(entry).getBytes(StandardCharsets.UTF_8);
        byte[] stored = codec.encode(line);
        long end = position + stored.length - 1;
        if (end > Integer.MAX_VALUE) {
            throw new DictionaryFormatException("Le fichier de definitions depasse 2 Gio.");
        }
        IndexEntry indexEntry = new IndexEntry(entry.title(), (int) position, (int) end);
        output.write(stored);
        position = end + 1;
        return indexEntry;
    }

    /** Vide le tampon et ferme le fichier de définitions. */
    @Override
    public void close() throws IOException {
        output.close();
    }
}
