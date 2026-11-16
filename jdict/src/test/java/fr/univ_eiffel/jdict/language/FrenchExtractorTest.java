package fr.univ_eiffel.jdict.language;

import fr.univ_eiffel.jdict.model.DictionaryEntry;
import fr.univ_eiffel.jdict.model.WikiPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FrenchExtractorTest {
    private final LanguageExtractor extractor = new FrenchExtractor();

    @ParameterizedTest
    @ValueSource(strings = {"\n", "\r\n"})
    void readsDefinitionWithEitherLineEnding(String separator) {
        String text = String.join(separator, "== {{langue|fr}} ==", "=== {{S|nom|fr}} ===", "# un mot.");
        assertEquals(Map.of("nom", List.of("un mot.")), extract(text).definitions());
    }

    @Test
    void mergesSectionsBySimpleType() {
        DictionaryEntry result = extract("""
                == {{langue|fr}} ==
                === {{S|nom|fr|num=1}} ===
                # un
                === {{S|nom|fr|num=2}} ===
                # deux
                ==== {{S|verbe|fr|flexion}} ====
                # faire
                """);
        assertEquals(List.of("nom", "verbe"), new ArrayList<>(result.definitions().keySet()));
        assertEquals(List.of("un", "deux"), result.definitions().get("nom"));
        assertEquals(List.of("faire"), result.definitions().get("verbe"));
    }

    @Test
    void ignoresExamplesSubSensesNotesAndEmptyDefinitions() {
        assertEmpty("""
                == {{langue|fr}} ==
                === {{S|nom|fr}} ===
                #* exemple
                #: note
                ## sous-sens
                #; terme
                #
                * synonyme
                """);
    }

    @Test
    void selectsOnlyFrenchLanguageSections() {
        assertEquals(Map.of("nom", List.of("un mot.")), extract("""
                == {{langue|de}} ==
                === {{S|nom|de}} ===
                * exemple
                == {{langue|fr}} ==
                === {{S|nom|fr}} ===
                # un mot.
                == {{langue|en}} ==
                === {{S|noun|en}} ===
                # ignored
                """).definitions());
        assertEmpty("== {{langue|en}} ==\n=== {{S|noun|en}} ===\n# ignored");
    }

    @ParameterizedTest
    @ValueSource(strings = {"#REDIRECTION [[vieux]]", "#redirect [[x]]", ""})
    void ignoresRedirectionsAndEmptyPages(String text) {
        assertEmpty(text);
    }

    @Test
    void resetsTypeAtHeadingWithoutFrenchWordType() {
        String headings = """
                === {{S|\u00e9tymologie}} ===
                # autre
                ==== {{S|synonymes}} ====
                # synonyme
                === {{S|nom|de}} ===
                # nom allemand
                """;
        assertEmpty("== {{langue|fr}} ==\n" + headings);
        assertEquals(Map.of("nom", List.of("un mot.")), extract("""
                == {{langue|fr}} ==
                === {{S|nom|fr}} ===
                # un mot.
                ==== {{S|synonymes}} ====
                # synonyme
                """).definitions());
    }

    private DictionaryEntry extract(String text) {
        return extractor.extract(new WikiPage("mot", text)).orElseThrow();
    }

    private void assertEmpty(String text) {
        assertTrue(extractor.extract(new WikiPage("mot", text)).isEmpty());
    }
}
