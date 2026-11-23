package fr.univ_eiffel.jdict;

import fr.univ_eiffel.jdict.format.EntryFormatter;
import fr.univ_eiffel.jdict.format.JsonEntryFormatter;
import fr.univ_eiffel.jdict.format.YamlEntryFormatter;
import fr.univ_eiffel.jdict.search.DictionaryLookup;
import fr.univ_eiffel.jdict.search.SearchQuery;
import fr.univ_eiffel.jdict.text.ConsoleMessages;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;

/** Recherche un mot et affiche ses définitions en JSON ou en YAML. */
public final class DictionarySearcher {
    private DictionarySearcher() {
    }

    /** Lance la recherche sur les fichiers du dictionnaire. */
    public static void main(String[] args) {
        int code = run(args, System.out, System.err);
        if (code != 0) {
            System.exit(code);
        }
    }

    static int run(String[] args, OutputStream out, PrintStream err) {
        if (args.length != 2) {
            err.print("Usage : DictionarySearcher <chemin sans extension> <mot | MOT | yaml:mot>\n");
            return 2;
        }

        SearchQuery query;
        try {
            query = SearchQuery.parse(args[1]);
        } catch (IllegalArgumentException error) {
            err.print("Requete vide\n");
            return 2;
        }

        try {
            List<String> results;
            try (DictionaryLookup lookup = DictionaryLookup.open(Path.of(args[0]))) {
                results = lookup.find(query);
            }
            if (results.isEmpty()) {
                err.print("Aucune entree trouvee\n");
                return 3;
            }

            EntryFormatter formatter = query.yaml() ? new YamlEntryFormatter() : new JsonEntryFormatter();
            for (String line : results) {
                out.write(formatter.format(line).getBytes(StandardCharsets.UTF_8));
            }
            out.flush();
            return 0;
        } catch (IOException | InvalidPathException error) {
            err.print("Erreur : " + ConsoleMessages.ascii(error.getMessage()) + "\n");
            return 1;
        }
    }
}
