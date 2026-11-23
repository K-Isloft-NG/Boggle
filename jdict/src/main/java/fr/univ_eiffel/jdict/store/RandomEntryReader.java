package fr.univ_eiffel.jdict.store;

import fr.univ_eiffel.jdict.model.DictionaryFormatException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONParserConfiguration;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/** Lit une entrée du dictionnaire à partir de sa position en octets. */
public final class RandomEntryReader implements Closeable {
    private static final JSONParserConfiguration STRICT_JSON = new JSONParserConfiguration().withStrictMode();

    private final RandomAccessFile input;
    private final EntryCodec codec;

    /** Ouvre le fichier de définitions en lecture seule. */
    public RandomEntryReader(Path definitionFile, EntryCodec codec) throws IOException {
        input = new RandomAccessFile(definitionFile.toFile(), "r");
        this.codec = codec;
    }

    /** Renvoie la ligne décodée située entre les bornes inclusives. */
    public String read(EntryRange range) throws IOException {
        if (range.end() >= input.length() || range.length() <= 0) {
            throw new DictionaryFormatException("Plage hors limites a l'octet " + range.start() + ".");
        }
        byte[] stored = new byte[range.length()];
        input.seek(range.start());
        input.readFully(stored);
        try {
            return StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(codec.decode(stored))).toString();
        } catch (CharacterCodingException error) {
            throw new DictionaryFormatException("UTF-8 invalide a l'octet " + range.start() + ".", error);
        }
    }

    /** Lit le titre d'une entrée JSON à la position demandée. */
    public String readTitle(EntryRange range) throws IOException {
        String line = read(range);
        try {
            return new JSONObject(line, STRICT_JSON).getString("title");
        } catch (JSONException error) {
            throw new DictionaryFormatException("Titre illisible a l'octet " + range.start() + ".", error);
        }
    }

    /** Ferme le fichier de définitions. */
    @Override
    public void close() throws IOException {
        input.close();
    }
}
