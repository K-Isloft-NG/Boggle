package fr.univ_eiffel.jdict.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Représente un mot et ses définitions, dans l'ordre de la page. */
public record DictionaryEntry(String title, Map<String, List<String>> definitions) {
    /** Vérifie les données et copie les définitions dans leur ordre. */
    public DictionaryEntry {
        if (Objects.requireNonNull(title, "Le titre est obligatoire.").isBlank()) {
            throw new IllegalArgumentException("Le titre est vide.");
        }
        if (Objects.requireNonNull(definitions, "Les definitions sont obligatoires.").isEmpty()) {
            throw new IllegalArgumentException("Les definitions sont vides.");
        }

        Map<String, List<String>> copy = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> type : definitions.entrySet()) {
            String name = Objects.requireNonNull(type.getKey(), "Le type est obligatoire.");
            List<String> values = List.copyOf(type.getValue());
            if (values.isEmpty()) {
                throw new IllegalArgumentException("La liste de definitions est vide.");
            }
            copy.put(name, values);
        }
        definitions = Collections.unmodifiableMap(copy);
    }
}
