package fr.univ_eiffel.jdict.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class WikiPageTest {
    @Test
    void rejectsNullFields() {
        assertThrows(NullPointerException.class, () -> new WikiPage(null, "x"));
        assertThrows(NullPointerException.class, () -> new WikiPage("x", null));
    }

    @Test
    void exposesTitleAndText() {
        WikiPage page = new WikiPage("a", "b");
        assertEquals("a", page.title());
        assertEquals("b", page.text());
    }
}
