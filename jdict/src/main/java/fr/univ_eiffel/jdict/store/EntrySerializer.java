package fr.univ_eiffel.jdict.store;

import fr.univ_eiffel.jdict.model.DefinitionFileHeader;
import fr.univ_eiffel.jdict.model.DictionaryEntry;

/** Convertit les données du dictionnaire en lignes de texte. */
public interface EntrySerializer {
    /** Sérialise une entrée sans fin de ligne. */
    String serialize(DictionaryEntry entry);

    /** Sérialise l'en-tête sans fin de ligne. */
    String serializeHeader(DefinitionFileHeader header);
}
