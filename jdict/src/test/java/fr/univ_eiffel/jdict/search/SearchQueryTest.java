package fr.univ_eiffel.jdict.search;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SearchQueryTest {
    @Test
    void parsesYamlAndDetectsUppercase() {
        SearchQuery exact = SearchQuery.parse("congres");
        assertEquals(new SearchQuery("congres", false), exact);
        assertFalse(exact.normalized());
        SearchQuery yaml = SearchQuery.parse("yaml:CONGRES");
        assertEquals(new SearchQuery("CONGRES", true), yaml);
        assertTrue(yaml.normalized());
        assertTrue(SearchQuery.parse("CONGR\u00c8S").normalized());
        assertEquals(" x ", SearchQuery.parse(" x ").word());
        assertEquals("json:x", SearchQuery.parse("json:x").word());
        assertThrows(IllegalArgumentException.class, () -> SearchQuery.parse("yaml:"));
        assertThrows(IllegalArgumentException.class, () -> SearchQuery.parse(""));
    }

    @Test
    void listsDistinctApostropheVariantsInOrder() {
        assertEquals(List.of("presqu'\u00eele", "presqu\u2019\u00eele"), SearchQuery.parse("presqu'\u00eele").variants());
        assertEquals(List.of("plage"), SearchQuery.parse("plage").variants());
        assertEquals(List.of("a'b\u2019c", "a\u2019b\u2019c", "a'b'c"), SearchQuery.parse("a'b\u2019c").variants());
    }
}
