package fr.univ_eiffel.jdict.export;

import fr.univ_eiffel.jdict.model.DictionaryEntry;
import fr.univ_eiffel.jdict.text.TextNormalizer;
import fr.univ_eiffel.jdict.text.WikiMarkupStripper;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Calcule les poids des lettres et des digrammes à partir du dictionnaire. */
public final class CharTableBuilder {
    private static final int ALPHABET_SIZE = 26;
    private static final int WEIGHT_SCALE = 10_000;

    private final long[] letters = new long[ALPHABET_SIZE];
    private final long[][] pairs = new long[ALPHABET_SIZE][ALPHABET_SIZE];

    /** Compte les lettres et les paires dans le titre et les définitions. */
    public void accept(DictionaryEntry entry) {
        count(TextNormalizer.normalize(entry.title()));
        for (List<String> definitions : entry.definitions().values()) {
            for (String definition : definitions) {
                count(TextNormalizer.normalize(WikiMarkupStripper.strip(definition)));
            }
        }
    }

    private void count(String text) {
        int previous = -1;
        for (int i = 0; i < text.length(); i++) {
            int current = text.charAt(i) - 'A';
            if (current >= 0 && current < ALPHABET_SIZE) {
                letters[current]++;
                if (previous >= 0) {
                    pairs[previous][current]++;
                }
                previous = current;
            } else {
                previous = -1;
            }
        }
    }

    /** Renvoie les cases pondérées par partition des occurrences, dans l'ordre alphabétique. */
    public List<CharWeight> build() {
        Map<String, Long> counts = new TreeMap<>();
        long[] remaining = letters.clone();
        for (int first = 0; first < ALPHABET_SIZE; first++) {
            if (letters[first] == 0) {
                continue;
            }
            for (int second = 0; second < ALPHABET_SIZE; second++) {
                long count = pairs[first][second];
                if (first != second && count * 100 >= letters[first] * 90) {
                    counts.put(letter(first) + letter(second), count);
                    remaining[first] -= count;
                    remaining[second] -= count;
                }
            }
        }
        for (int i = 0; i < ALPHABET_SIZE; i++) {
            if (remaining[i] > 0) {
                counts.put(letter(i), remaining[i]);
            }
        }

        long total = 0;
        for (long count : counts.values()) {
            total += count;
        }
        List<CharWeight> weights = new ArrayList<>();
        for (Map.Entry<String, Long> element : counts.entrySet()) {
            long rounded = (2 * element.getValue() * WEIGHT_SCALE + total) / (2 * total);
            weights.add(new CharWeight(element.getKey(), (int) Math.max(1, rounded)));
        }
        return weights;
    }

    private static String letter(int index) {
        return String.valueOf((char) ('A' + index));
    }

    /** Écrit les cases et leurs poids en ASCII avec un LF par ligne. */
    public static void write(List<CharWeight> weights, OutputStream out) throws IOException {
        BufferedOutputStream buffered = new BufferedOutputStream(out);
        for (CharWeight weight : weights) {
            String line = weight.element() + " " + weight.weight() + "\n";
            buffered.write(line.getBytes(StandardCharsets.US_ASCII));
        }
        buffered.flush();
    }
}
