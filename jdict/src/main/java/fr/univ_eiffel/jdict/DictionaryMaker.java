package fr.univ_eiffel.jdict;

import fr.univ_eiffel.jdict.build.DictionaryBuilder;
import fr.univ_eiffel.jdict.dump.DumpReader;
import fr.univ_eiffel.jdict.language.LanguageExtractor;
import fr.univ_eiffel.jdict.language.LanguageExtractors;
import fr.univ_eiffel.jdict.model.DefinitionFileHeader;
import fr.univ_eiffel.jdict.store.DefinitionFileWriter;
import fr.univ_eiffel.jdict.store.JsonEntrySerializer;
import fr.univ_eiffel.jdict.store.PlainCodec;
import fr.univ_eiffel.jdict.text.ConsoleMessages;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.time.Clock;

/** Lance la construction d'un dictionnaire depuis un dump bzip2 sur l'entrée standard. */
public final class DictionaryMaker {
    private DictionaryMaker() {
    }

    /** Construit le dictionnaire à partir des arguments de la commande. */
    public static void main(String[] args) {
        int code = run(args, System.in, System.err, Clock.systemUTC());
        if (code != 0) {
            System.exit(code);
        }
    }

    static int run(String[] args, InputStream in, PrintStream err, Clock clock) {
        if (args.length != 2) {
            err.print("Usage : DictionaryMaker <langue> <chemin sans extension>\n");
            return 2;
        }

        LanguageExtractor extractor;
        try {
            extractor = LanguageExtractors.forCode(args[0]);
        } catch (IllegalArgumentException error) {
            err.print(ConsoleMessages.ascii(error.getMessage()) + "\n");
            return 2;
        }

        Path file = Path.of(args[1] + ".dict");
        DefinitionFileHeader header = DefinitionFileHeader.forLanguage(extractor.languageCode(), clock.instant());
        DictionaryBuilder.BuildReport report;
        try (DumpReader reader = DumpReader.fromBzip2(in);
                DefinitionFileWriter writer = new DefinitionFileWriter(file, header, new JsonEntrySerializer(), new PlainCodec())) {
            report = new DictionaryBuilder().build(reader, extractor, writer,
                    pages -> err.print("Pages lues : " + pages + "\n"));
        } catch (IOException error) {
            err.print("Erreur : " + ConsoleMessages.ascii(error.getMessage()) + "\n");
            return 1;
        }

        err.print("Termine : " + report.pages() + " pages lues, " + report.entries() + " entrees ecrites\n");
        return 0;
    }
}
