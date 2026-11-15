package fr.univ_eiffel.jdict.dump;

import fr.univ_eiffel.jdict.model.WikiPage;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** Compte les pages et les redirections d'un dump. */
public final class DumpStatistics {
    private static final String REDIRECT = "#REDIRECT";
    private static final int PROGRESS_INTERVAL = 100_000;

    private DumpStatistics() {
    }

    /** Renvoie les trois lignes de statistiques du dump. */
    public static String summarize(DumpReader reader) throws IOException {
        return summarize(reader, null);
    }

    private static String summarize(DumpReader reader, PrintStream progress) throws IOException {
        Statistics statistics = new Statistics();
        reader.forEachPage(page -> {
            statistics.add(page);
            if (progress != null && statistics.pages % PROGRESS_INTERVAL == 0) {
                progress.print("Pages lues : " + statistics.pages + "\n");
            }
        });
        return "pages: " + statistics.pages + "\n"
                + "redirections: " + statistics.redirects + "\n"
                + "texte le plus long: " + statistics.longest + "\n";
    }

    /** Lit un dump sur l'entrée standard et affiche ses statistiques. */
    public static void main(String[] args) {
        long start = System.nanoTime();
        PrintStream diagnostics = new PrintStream(System.err, true, StandardCharsets.UTF_8);
        boolean rawXml = args.length > 0 && "xml".equals(args[0]);
        String summary;
        try (DumpReader reader = rawXml ? new DumpReader(System.in) : DumpReader.fromBzip2(System.in)) {
            summary = summarize(reader, diagnostics);
        } catch (IOException error) {
            diagnostics.print("Erreur de lecture du dump : " + error.getMessage() + "\n");
            System.exit(1);
            return;
        }
        System.out.writeBytes(summary.getBytes(StandardCharsets.UTF_8));
        double seconds = (System.nanoTime() - start) / 1_000_000_000.0;
        diagnostics.printf(Locale.ROOT, "Durée : %.3f s\n", seconds);
    }

    private static final class Statistics {
        private long pages;
        private long redirects;
        private int longest;

        private void add(WikiPage page) {
            String text = page.text();
            pages++;
            if (text.regionMatches(true, 0, REDIRECT, 0, REDIRECT.length())) {
                redirects++;
            }
            longest = Math.max(longest, text.length());
        }
    }
}
