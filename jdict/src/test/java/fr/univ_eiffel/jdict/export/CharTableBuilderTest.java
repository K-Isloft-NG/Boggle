package fr.univ_eiffel.jdict.export;

import fr.univ_eiffel.jdict.model.DictionaryEntry;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class CharTableBuilderTest {
    private static final String NINE_QU_AND_ONE_Q = "qu qu qu qu qu qu qu qu qu q";

    @Test
    void countsTitleAndVisibleDefinitionLetters() {
        assertEquals(List.of(new CharWeight("A", 6000), new CharWeight("B", 4000)),
                build("aab", "{{lexique|fr}} [[x|ba]]"));
    }

    @Test
    void accountsForBothLettersOfQu() {
        assertEquals(List.of(new CharWeight("A", 2000), new CharWeight("I", 2000),
                new CharWeight("O", 2000), new CharWeight("QU", 4000)), build("qu a i", "quo"));
    }

    @Test
    void keepsUnpairedQAndU() {
        assertEquals(List.of(new CharWeight("Q", 909), new CharWeight("QU", 8182), new CharWeight("U", 909)),
                build(NINE_QU_AND_ONE_Q, "u"));
    }

    @Test
    void appliesNinetyPercentThreshold() {
        assertEquals(List.of(new CharWeight("A", 909), new CharWeight("AB", 8182), new CharWeight("C", 909)),
                build("ab ab ab ab ab ab ab ab ab a c", ""));
        assertEquals(List.of(new CharWeight("A", 5000), new CharWeight("B", 4000), new CharWeight("C", 1000)),
                build("ab ab ab ab ab ab ab ab a c a c", ""));
    }

    @Test
    void roundsWeightsWithMinimumOfOne() {
        assertEquals(List.of(new CharWeight("A", 10000), new CharWeight("Z", 1)),
                build("-", "a".repeat(20_000) + "z"));
    }

    @Test
    void writesOnlyAsciiLinesEndingWithLf() throws IOException {
        assertOutput("A 6000\nB 4000\n", build("aab", "{{lexique|fr}} [[x|ba]]"));
        assertOutput("A 2000\nI 2000\nO 2000\nQU 4000\n", build("qu a i", "quo"));
        assertOutput("Q 909\nQU 8182\nU 909\n", build(NINE_QU_AND_ONE_Q, "u"));
        assertOutput("", build("-", ""));
    }

    private static List<CharWeight> build(String title, String definition) {
        CharTableBuilder builder = new CharTableBuilder();
        builder.accept(new DictionaryEntry(title, Map.of("nom", List.of(definition))));
        return builder.build();
    }

    private static void assertOutput(String expected, List<CharWeight> weights) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CharTableBuilder.write(weights, out);
        assertArrayEquals(expected.getBytes(StandardCharsets.US_ASCII), out.toByteArray());
    }
}
