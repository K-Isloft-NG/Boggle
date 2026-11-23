package fr.univ_eiffel.jdict.build;

import fr.univ_eiffel.jdict.store.IndexEntry;
import fr.univ_eiffel.jdict.store.IndexOrder;
import fr.univ_eiffel.jdict.text.TextNormalizer;

import java.util.ArrayList;
import java.util.List;

/** Mémorise les positions et les clés utilisées pour trier l'index. */
public final class IndexBuilder {
    private final List<KeyedEntry> entries = new ArrayList<>();

    /** Ajoute la position en calculant une fois la clé normalisée. */
    public void add(IndexEntry entry) {
        entries.add(new KeyedEntry(TextNormalizer.normalize(entry.title()), entry));
    }

    /** Renvoie le nombre d'entrées ajoutées. */
    public int size() {
        return entries.size();
    }

    /** Trie les entrées par clé et titre, puis renvoie leurs positions dans cet ordre. */
    public List<IndexEntry> sortedEntries() {
        entries.sort((a, b) -> IndexOrder.compare(a.key(), a.entry().title(), b.key(), b.entry().title()));
        List<IndexEntry> sorted = new ArrayList<>(entries.size());
        for (KeyedEntry entry : entries) {
            sorted.add(entry.entry());
        }
        return sorted;
    }

    private record KeyedEntry(String key, IndexEntry entry) {
    }
}
