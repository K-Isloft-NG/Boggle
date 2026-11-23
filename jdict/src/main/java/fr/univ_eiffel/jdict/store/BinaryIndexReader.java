package fr.univ_eiffel.jdict.store;

import fr.univ_eiffel.jdict.model.DictionaryFormatException;
import fr.univ_eiffel.jdict.text.ConsoleMessages;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;

/** Lit les positions d'un index DICTINDX directement dans le fichier. */
public final class BinaryIndexReader implements IndexReader {
    private static final int HEADER_LENGTH = BinaryIndexWriter.HEADER.length();
    private static final int RANGE_LENGTH = 2 * Integer.BYTES;

    private final RandomAccessFile input;
    private final int size;

    /** Ouvre l'index et vérifie son en-tête et sa longueur. */
    public BinaryIndexReader(Path file) throws IOException {
        input = new RandomAccessFile(file.toFile(), "r");
        try {
            size = readSize(file);
        } catch (IOException error) {
            try {
                input.close();
            } catch (IOException closeError) {
                error.addSuppressed(closeError);
            }
            throw error;
        }
    }

    private int readSize(Path file) throws IOException {
        long bytes = input.length() - HEADER_LENGTH;
        String message = "Index invalide : " + ConsoleMessages.ascii(file.toString());
        if (bytes < 0 || bytes % RANGE_LENGTH != 0 || bytes / RANGE_LENGTH > Integer.MAX_VALUE) {
            throw new DictionaryFormatException(message);
        }

        byte[] header = new byte[HEADER_LENGTH];
        input.readFully(header);
        if (!BinaryIndexWriter.HEADER.equals(new String(header, StandardCharsets.US_ASCII))) {
            throw new DictionaryFormatException(message);
        }
        return (int) (bytes / RANGE_LENGTH);
    }

    /** Renvoie le nombre de couples de positions du fichier. */
    @Override
    public int size() {
        return size;
    }

    /** Lit uniquement le couple de positions demandé. */
    @Override
    public EntryRange range(int position) throws IOException {
        Objects.checkIndex(position, size);
        input.seek(HEADER_LENGTH + (long) RANGE_LENGTH * position);
        int start = input.readInt();
        int end = input.readInt();
        try {
            return new EntryRange(start, end);
        } catch (IllegalArgumentException error) {
            throw new DictionaryFormatException("Bornes invalides a la position " + position + " de l'index.", error);
        }
    }

    /** Ferme le fichier d'index. */
    @Override
    public void close() throws IOException {
        input.close();
    }
}
