package fr.univ_eiffel.jdict;

import fr.univ_eiffel.jdict.export.SqlScriptWriter;
import fr.univ_eiffel.jdict.store.DefinitionFileReader;
import fr.univ_eiffel.jdict.text.ConsoleMessages;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Exporte le script d'insertion du dictionnaire dans la base. */
public final class SqlScriptMaker {
    private SqlScriptMaker() {
    }

    /** Écrit le script SQL sur la sortie standard. */
    public static void main(String[] args) {
        int code = run(args, System.out, System.err);
        if (code != 0) {
            System.exit(code);
        }
    }

    static int run(String[] args, OutputStream out, PrintStream err) {
        if (args.length != 4) {
            err.print("Usage : SqlScriptMaker <chemin sans extension> <fichier words.txt> <version AAAAMMJJ> <libelle>\n");
            return 2;
        }

        long count;
        try {
            String language = readLanguage(Path.of(args[0] + ".dict"));
            count = countWords(Path.of(args[1]));
            if (count == 0) {
                err.print("Erreur : le fichier de mots est vide.\n");
                return 1;
            }
            String script = SqlScriptWriter.render(language, args[3], args[2], count);
            out.write(script.getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (IOException error) {
            err.print("Erreur : " + ConsoleMessages.ascii(error.getMessage()) + "\n");
            return 1;
        } catch (IllegalArgumentException error) {
            err.print("Erreur : " + ConsoleMessages.ascii(error.getMessage()) + "\n");
            return 2;
        }
        err.print("Termine : script ecrit pour " + count + " mots\n");
        return 0;
    }

    private static String readLanguage(Path file) throws IOException {
        try (DefinitionFileReader reader = new DefinitionFileReader(file)) {
            return reader.header().language();
        }
    }

    private static long countWords(Path file) throws IOException {
        long count = 0;
        try (BufferedReader input = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = input.readLine()) != null) {
                if (!line.isEmpty()) {
                    count++;
                }
            }
        }
        return count;
    }
}
