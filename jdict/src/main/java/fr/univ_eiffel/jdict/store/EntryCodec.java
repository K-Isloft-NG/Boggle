package fr.univ_eiffel.jdict.store;

/** Convertit les octets d'une ligne vers et depuis leur format de stockage. */
public interface EntryCodec {
    /** Prépare les octets d'une ligne avant son écriture. */
    byte[] encode(byte[] line);

    /** Retrouve les octets de la ligne à partir des octets stockés. */
    byte[] decode(byte[] stored);
}
