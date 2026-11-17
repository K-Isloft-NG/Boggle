package fr.univ_eiffel.jdict.export;

import java.util.Objects;

/** Représente une case du moteur et son poids de tirage. */
public record CharWeight(String element, int weight) {
    /** Vérifie que la case existe et que son poids est positif. */
    public CharWeight {
        Objects.requireNonNull(element);
        if (weight < 1) {
            throw new IllegalArgumentException("Le poids doit etre positif.");
        }
    }
}
