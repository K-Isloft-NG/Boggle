package fr.univ_eiffel.jdict.text;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class TextNormalizerTest {
    // Les échappements Unicode rendent les données indépendantes de l'encodage du source.
    @ParameterizedTest
    @CsvSource(value = {
        "congr\u00e8s|CONGRES", "Congr\u00e8s|CONGRES", "congres|CONGRES",
        "c\u0153ur|COEUR", "C\u0153ur|COEUR", "\u00e6ther|AETHER",
        "\u00c9T\u00c9|ETE", "na\u00efve|NAIVE", "gar\u00e7on|GARCON", "\u00c7a|CA",
        "aujourd'hui|AUJOURD'HUI", "Stra\u00dfe|STRASSE", "trait-d'union|TRAIT-D'UNION",
        "''|''", "\u00d8 42|\u00d8 42"
    }, delimiter = '|')
    void normalizesAndIsIdempotent(String word, String expected) {
        String normalized = TextNormalizer.normalize(word);
        assertEquals(expected, normalized);
        assertEquals(normalized, TextNormalizer.normalize(normalized));
    }

    @Test
    void rejectsNull() {
        assertThrows(NullPointerException.class, () -> TextNormalizer.normalize(null));
    }
}
