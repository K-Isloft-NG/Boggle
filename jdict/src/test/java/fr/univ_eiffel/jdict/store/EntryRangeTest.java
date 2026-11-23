package fr.univ_eiffel.jdict.store;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class EntryRangeTest {
    @Test
    void countsBothBoundsAndRejectsInvalidRanges() {
        assertEquals(1, new EntryRange(0, 0).length());
        assertEquals(4, new EntryRange(3, 6).length());
        assertThrows(IllegalArgumentException.class, () -> new EntryRange(-1, 2));
        assertThrows(IllegalArgumentException.class, () -> new EntryRange(5, 4));
    }
}
