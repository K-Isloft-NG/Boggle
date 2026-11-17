package fr.univ_eiffel.jdict.store;

import fr.univ_eiffel.jdict.model.DefinitionFileHeader;
import fr.univ_eiffel.jdict.model.DictionaryEntry;
import fr.univ_eiffel.jdict.model.DictionaryFormatException;
import fr.univ_eiffel.jdict.text.ConsoleMessages;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONParserConfiguration;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/** Lit un fichier de définitions non compressé dans l'ordre de ses lignes. */
public final class DefinitionFileReader implements Closeable {
    private static final JSONParserConfiguration STRICT_JSON = new JSONParserConfiguration().withStrictMode();
    private final BufferedReader input;
    private final DefinitionFileHeader header;
    private final Path file;
    private boolean started;

    /** Ouvre le fichier UTF-8 et lit son en-tête. */
    public DefinitionFileReader(Path file) throws IOException {
        this.file = file;
        input = Files.newBufferedReader(file, StandardCharsets.UTF_8);
        try {
            header = readHeader();
        } catch (IOException error) {
            try {
                input.close();
            } catch (IOException closeError) {
                error.addSuppressed(closeError);
            }
            throw error;
        }
    }

    /** Renvoie l'en-tête du fichier. */
    public DefinitionFileHeader header() {
        return header;
    }

    /** Transmet chaque entrée une seule fois et renvoie leur nombre. */
    public long forEachEntry(Consumer<DictionaryEntry> consumer) throws IOException {
        Objects.requireNonNull(consumer);
        if (started) {
            throw new IllegalStateException("Les entrees ont deja ete lues.");
        }
        started = true;
        long lineNumber = 1;
        String line;
        while ((line = input.readLine()) != null) {
            lineNumber++;
            DictionaryEntry entry;
            try {
                entry = parseEntry(line);
            } catch (JSONException | IllegalArgumentException error) {
                throw new DictionaryFormatException("Entree invalide a la ligne " + lineNumber + " : "
                        + ConsoleMessages.ascii(file.toString()), error);
            }
            consumer.accept(entry);
        }
        return lineNumber - 1;
    }

    /** Ferme le fichier de définitions. */
    @Override
    public void close() throws IOException {
        input.close();
    }

    private DefinitionFileHeader readHeader() throws IOException {
        try {
            String line = input.readLine();
            if (line == null) {
                throw new JSONException("Fichier vide.");
            }
            JSONObject fields = new JSONObject(line, STRICT_JSON);
            return new DefinitionFileHeader(fields.getString("description"), fields.getString("created_on"),
                    fields.getString("language"));
        } catch (JSONException error) {
            throw new DictionaryFormatException("En-tete invalide : " + ConsoleMessages.ascii(file.toString()), error);
        }
    }

    private static DictionaryEntry parseEntry(String line) {
        JSONObject fields = new JSONObject(line, STRICT_JSON);
        String title = fields.getString("title");
        JSONObject types = fields.getJSONObject("definitions");

        Map<String, List<String>> definitions = new LinkedHashMap<>();
        for (String type : types.keySet()) {
            JSONArray values = types.getJSONArray(type);
            List<String> texts = new ArrayList<>();
            for (int i = 0; i < values.length(); i++) {
                texts.add(values.getString(i));
            }
            definitions.put(type, texts);
        }
        return new DictionaryEntry(title, definitions);
    }
}
