package fr.univ_eiffel.jdict.export;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SqlScriptWriterTest {
    @Test
    void rendersExpectedScript() {
        assertEquals("""
                -- Dictionnaire de la langue fr, version 20261115, 1551367 mots.
                -- Script genere par SqlScriptMaker.
                INSERT INTO boggle.dictionnaire (code_langue, libelle, version, nb_mots)
                VALUES ('fr', 'Francais (Wiktionnaire)', '20261115', 1551367)
                ON CONFLICT (code_langue, version) DO NOTHING;
                """, SqlScriptWriter.render("fr", "Francais (Wiktionnaire)", "20261115", 1551367));
        assertTrue(SqlScriptWriter.render("fr", "L'ami", "20261115", 1).contains("'L''ami'"));
    }

    @Test
    void rejectsInvalidFields() {
        for (String language : List.of("FR", "fra", "f")) {
            assertThrows(IllegalArgumentException.class, () -> SqlScriptWriter.render(language, "Francais", "20261115", 1));
        }
        for (String version : List.of("2026-11-15", "2026111", "202611150")) {
            assertThrows(IllegalArgumentException.class, () -> SqlScriptWriter.render("fr", "Francais", version, 1));
        }
        for (String label : List.of("", "  ", "a".repeat(101), "a\nb")) {
            assertThrows(IllegalArgumentException.class, () -> SqlScriptWriter.render("fr", label, "20261115", 1));
        }
        assertThrows(IllegalArgumentException.class, () -> SqlScriptWriter.render("fr", "Francais", "20261115", 0));
        assertThrows(IllegalArgumentException.class, () -> SqlScriptWriter.render("fr", "Francais", "20261115", -1));
    }
}
