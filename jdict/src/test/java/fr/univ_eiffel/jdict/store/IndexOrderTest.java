package fr.univ_eiffel.jdict.store;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class IndexOrderTest {
    @Test
    void comparesNormalizedKeysThenOriginalTitles() {
        assertTrue(IndexOrder.compare("CONGRES", "Congr\u00e8s", "CONGRES", "congres") < 0);
        assertTrue(IndexOrder.compare("A", "z", "B", "a") < 0);
    }
}
