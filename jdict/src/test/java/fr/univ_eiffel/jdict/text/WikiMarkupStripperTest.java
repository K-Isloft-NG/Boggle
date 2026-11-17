package fr.univ_eiffel.jdict.text;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class WikiMarkupStripperTest {
    @Test
    void removesTemplatesAndKeepsDisplayedLinks() {
        assertEquals(" b cc", WikiMarkupStripper.strip("{{lexique|fr}} [[a|b]] [[cc]]"));
        assertEquals(" x", WikiMarkupStripper.strip("{{a|{{b}}}} x"));
        assertEquals("c", WikiMarkupStripper.strip("[[a#fr|b|c]]"));
    }

    @Test
    void handlesUnclosedMarkupAndPlainText() {
        assertEquals("avant ", WikiMarkupStripper.strip("avant {{a x"));
        assertEquals("[[ouverte", WikiMarkupStripper.strip("[[ouverte"));
        assertEquals("''italique''", WikiMarkupStripper.strip("''italique''"));
        assertEquals("", WikiMarkupStripper.strip(""));
    }
}
