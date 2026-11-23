package fr.univ_eiffel.jdict.store;

import java.io.Closeable;
import java.io.IOException;

/** Donne accès aux positions des entrées d'un index. */
public interface IndexReader extends Closeable {
    /** Renvoie le nombre d'entrées dans l'index. */
    int size();

    /** Renvoie les bornes de l'entrée à la position demandée. */
    EntryRange range(int position) throws IOException;
}
