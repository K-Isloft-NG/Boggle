package fr.univ_eiffel.jdict.store;

import java.util.Arrays;

/** Stocke chaque ligne sans compression avec un LF final. */
public final class PlainCodec implements EntryCodec {
    /** Ajoute un LF aux octets de l'entrée. */
    @Override
    public byte[] encode(byte[] line) {
        byte[] stored = Arrays.copyOf(line, line.length + 1);
        stored[line.length] = '\n';
        return stored;
    }

    /** Retire le LF final s'il est présent. */
    @Override
    public byte[] decode(byte[] stored) {
        if (stored.length > 0 && stored[stored.length - 1] == '\n') {
            return Arrays.copyOf(stored, stored.length - 1);
        }
        return stored;
    }
}
