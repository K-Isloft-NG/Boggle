package fr.univ_eiffel.jdict.search;

import fr.univ_eiffel.jdict.store.BinaryIndexReader;
import fr.univ_eiffel.jdict.store.IndexOrder;
import fr.univ_eiffel.jdict.store.IndexReader;
import fr.univ_eiffel.jdict.store.PlainCodec;
import fr.univ_eiffel.jdict.store.RandomEntryReader;
import fr.univ_eiffel.jdict.text.TextNormalizer;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/** Recherche des définitions par dichotomie dans l'index du dictionnaire. */
public final class DictionaryLookup implements Closeable {
    private final IndexReader index;
    private final RandomEntryReader entries;

    /** Associe l'index à son lecteur de définitions. */
    public DictionaryLookup(IndexReader index, RandomEntryReader entries) {
        this.index = index;
        this.entries = entries;
    }

    /** Ouvre l'index et le fichier de définitions non compressé. */
    public static DictionaryLookup open(Path base) throws IOException {
        IndexReader index = new BinaryIndexReader(Path.of(base + ".index"));
        try {
            return new DictionaryLookup(index, new RandomEntryReader(Path.of(base + ".dict"), new PlainCodec()));
        } catch (IOException | RuntimeException error) {
            try {
                index.close();
            } catch (IOException closeError) {
                error.addSuppressed(closeError);
            }
            throw error;
        }
    }

    /** Renvoie les lignes JSON trouvées, dans l'ordre de l'index. */
    public List<String> find(SearchQuery query) throws IOException {
        TreeSet<Integer> positions = new TreeSet<>();
        boolean normalized = query.normalized();
        for (String variant : query.variants()) {
            String key = TextNormalizer.normalize(variant);
            int first = lowerBound(key, variant, normalized);
            if (normalized) {
                for (int position = first; position < index.size(); position++) {
                    if (!TextNormalizer.normalize(titleAt(position)).equals(key)) {
                        break;
                    }
                    positions.add(position);
                }
            } else if (first < index.size() && titleAt(first).equals(variant)) {
                positions.add(first);
            }
        }
        List<String> results = new ArrayList<>(positions.size());
        for (int position : positions) {
            results.add(entries.read(index.range(position)));
        }
        return results;
    }

    private int lowerBound(String key, String title, boolean normalized) throws IOException {
        int low = 0;
        int high = index.size();
        while (low < high) {
            int middle = (low + high) >>> 1;
            String currentTitle = titleAt(middle);
            String currentKey = TextNormalizer.normalize(currentTitle);
            int comparison = normalized ? currentKey.compareTo(key)
                    : IndexOrder.compare(currentKey, currentTitle, key, title);
            if (comparison < 0) {
                low = middle + 1;
            } else {
                high = middle;
            }
        }
        return low;
    }

    private String titleAt(int position) throws IOException {
        return entries.readTitle(index.range(position));
    }

    /** Ferme le lecteur de définitions et le lecteur d'index. */
    @Override
    public void close() throws IOException {
        try (index) {
            entries.close();
        }
    }
}
