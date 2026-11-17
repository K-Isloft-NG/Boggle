package fr.univ_eiffel.jdict;

import fr.univ_eiffel.jdict.export.CharTableBuilder;
import fr.univ_eiffel.jdict.export.CharWeight;
import fr.univ_eiffel.jdict.store.DefinitionFileReader;
import fr.univ_eiffel.jdict.text.ConsoleMessages;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;

/** Exporte la table des lettres pondérées pour le moteur Boggle. */
public final class CharTableMaker {
    private CharTableMaker() {
    }

    /** Écrit la table de lettres sur la sortie standard. */
    public static void main(String[] args) {
        int code = run(args, System.out, System.err);
        if (code != 0) {
            System.exit(code);
        }
    }

    static int run(String[] args, OutputStream out, PrintStream err) {
        if (args.length != 1) {
            err.print("Usage : CharTableMaker <chemin sans extension>\n");
            return 2;
        }

        Path file = Path.of(args[0] + ".dict");
        CharTableBuilder builder = new CharTableBuilder();
        long count;
        List<CharWeight> weights;
        try (DefinitionFileReader reader = new DefinitionFileReader(file)) {
            count = reader.forEachEntry(builder::accept);
            weights = builder.build();
            CharTableBuilder.write(weights, out);
        } catch (IOException error) {
            err.print("Erreur : " + ConsoleMessages.ascii(error.getMessage()) + "\n");
            return 1;
        }
        err.print("Termine : " + count + " entrees lues, " + weights.size() + " lignes ecrites\n");
        return 0;
    }
}
