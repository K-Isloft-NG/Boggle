package fr.univ_eiffel.jdict.text;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ConsoleMessagesTest {
    @Test
    void removesAccentsAndReplacesNonAsciiCharacters() {
        assertEquals("Erreur : ete", ConsoleMessages.ascii("Erreur : \u00e9t\u00e9"));
        assertEquals("?", ConsoleMessages.ascii("\u65e5"));
        assertEquals("null", ConsoleMessages.ascii(null));
    }
}
