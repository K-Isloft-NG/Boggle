package fr.univ_eiffel.jdict.format;

import fr.univ_eiffel.jdict.model.DictionaryFormatException;

/** Formate la ligne JSON d'une entrée pour l'affichage. */
public interface EntryFormatter {
    /** Convertit une entrée en texte terminé par un LF. */
    String format(String jsonLine) throws DictionaryFormatException;
}
