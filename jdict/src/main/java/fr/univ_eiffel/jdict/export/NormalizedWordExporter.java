package fr.univ_eiffel.jdict.export;

import fr.univ_eiffel.jdict.model.DictionaryEntry;
import fr.univ_eiffel.jdict.text.BoggleWordFilter;
import fr.univ_eiffel.jdict.text.TextNormalizer;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/** Accumule les formes normalisées acceptées par le moteur Boggle. */
public final class NormalizedWordExporter {
    private final Set<String> acceptedTypes;
    private final TreeSet<String> words = new TreeSet<>();

    /** Crée un exporteur, avec tous les types si l'ensemble est vide. */
    public NormalizedWordExporter(Set<String> acceptedTypes) {
        this.acceptedTypes = Set.copyOf(acceptedTypes);
    }

    /** Ajoute la forme normalisée si le titre et ses types sont acceptés. */
    public void accept(DictionaryEntry entry) {
        if (!acceptedTypes.isEmpty() && Collections.disjoint(acceptedTypes, entry.definitions().keySet())) {
            return;
        }
        String word = TextNormalizer.normalize(entry.title());
        if (BoggleWordFilter.isBoggleWord(word)) {
            words.add(word);
        }
    }

    /** Renvoie le nombre de formes distinctes. */
    public int size() {
        return words.size();
    }

    /** Écrit une forme par ligne en ASCII, dans l'ordre alphabétique. */
    public void writeTo(OutputStream out) throws IOException {
        BufferedOutputStream buffered = new BufferedOutputStream(out);
        for (String word : words) {
            buffered.write(word.getBytes(StandardCharsets.US_ASCII));
            buffered.write('\n');
        }
        buffered.flush();
    }
}
