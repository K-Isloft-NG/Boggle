package fr.univ_eiffel.jdict.format;

/** Affiche la ligne JSON dans sa forme d'origine. */
public final class JsonEntryFormatter implements EntryFormatter {
    /** Ajoute le séparateur de ligne au JSON d'origine. */
    @Override
    public String format(String jsonLine) {
        return jsonLine + "\n";
    }
}
