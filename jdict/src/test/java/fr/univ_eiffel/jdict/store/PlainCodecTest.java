package fr.univ_eiffel.jdict.store;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

final class PlainCodecTest {
    private final EntryCodec codec = new PlainCodec();

    @ParameterizedTest
    @ValueSource(strings = {"abc", "", "a\nb"})
    void roundTripsBytes(String line) {
        byte[] utf8 = line.getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(utf8, codec.decode(codec.encode(utf8)));
    }

    @Test
    void onlyAddsOrRemovesFinalLf() {
        byte[] abc = {'a', 'b', 'c'};
        assertArrayEquals(new byte[]{'a', 'b', 'c', '\n'}, codec.encode(abc));
        assertArrayEquals(abc, codec.decode(abc));
    }
}
