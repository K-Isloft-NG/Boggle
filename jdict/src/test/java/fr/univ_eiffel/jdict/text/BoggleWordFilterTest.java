package fr.univ_eiffel.jdict.text;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BoggleWordFilterTest {
    @ParameterizedTest
    @ValueSource(strings = {"ABC", "A", "QU"})
    void acceptsAsciiUppercaseWords(String word) {
        assertTrue(BoggleWordFilter.isBoggleWord(word));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "A B", "A-B", "A'B", "A\u2019B", "A1", "A.", "abc", "\u00c9"})
    void rejectsOtherWords(String word) {
        assertFalse(BoggleWordFilter.isBoggleWord(word));
    }
}
