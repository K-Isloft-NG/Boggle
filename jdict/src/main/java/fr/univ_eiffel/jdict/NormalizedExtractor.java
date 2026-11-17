package fr.univ_eiffel.jdict;

import fr.univ_eiffel.jdict.export.NormalizedWordExporter;
import fr.univ_eiffel.jdict.store.DefinitionFileReader;
import fr.univ_eiffel.jdict.text.ConsoleMessages;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/** Exporte les mots normalisés du dictionnaire pour le moteur Boggle. */
public final class NormalizedExtractor {
    private static final int PROGRESS_INTERVAL = 500_000;

    private NormalizedExtractor() {
    }

    /** Écrit les mots sur la sortie standard. */
    public static void main(String[] args) {
        int code = run(args, System.out, System.err);
        if (code != 0) {
            System.exit(code);
        }
    }

    static int run(String[] args, OutputStream out, PrintStream err) {
        if (args.length < 1 || args.length > 2) {
            err.print("Usage : NormalizedExtractor <chemin sans extension> [types]\n");
            return 2;
        }

        Set<String> types = args.length == 2 ? parseTypes(args[1]) : Set.of();
        NormalizedWordExporter exporter = new NormalizedWordExporter(types);
        Path file = Path.of(args[0] + ".dict");
        long count;
        try (DefinitionFileReader reader = new DefinitionFileReader(file)) {
            long[] progress = {0};
            count = reader.forEachEntry(entry -> {
                exporter.accept(entry);
                progress[0]++;
                if (progress[0] % PROGRESS_INTERVAL == 0) {
                    err.print("Entrees lues : " + progress[0] + "\n");
                }
            });
            exporter.writeTo(out);
        } catch (IOException error) {
            err.print("Erreur : " + ConsoleMessages.ascii(error.getMessage()) + "\n");
            return 1;
        }
        err.print("Termine : " + count + " entrees lues, " + exporter.size() + " mots ecrits\n");
        return 0;
    }

    private static Set<String> parseTypes(String argument) {
        Set<String> types = new HashSet<>();
        for (String part : argument.split(",")) {
            String type = part.strip();
            if (!type.isEmpty()) {
                types.add(type);
            }
        }
        return types;
    }
}
