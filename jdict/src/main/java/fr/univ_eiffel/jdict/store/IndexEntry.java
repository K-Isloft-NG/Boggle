package fr.univ_eiffel.jdict.store;

import java.util.Objects;

/** Associe un titre à la position de ses premier et dernier octets. */
public record IndexEntry(String title, int start, int end) {
    /** Vérifie que le titre et les positions sont valides. */
    public IndexEntry {
        Objects.requireNonNull(title, "Le titre est obligatoire.");
        if (start < 0 || end < start) {
            throw new IllegalArgumentException("Les positions sont invalides.");
        }
    }
}
