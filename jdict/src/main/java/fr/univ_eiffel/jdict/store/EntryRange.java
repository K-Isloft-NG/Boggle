package fr.univ_eiffel.jdict.store;

/** Délimite les octets d'une entrée avec deux bornes inclusives. */
public record EntryRange(int start, int end) {
    /** Vérifie l'ordre et la valeur des bornes. */
    public EntryRange {
        if (start < 0 || end < start) {
            throw new IllegalArgumentException("Les bornes de l'entree sont invalides.");
        }
    }

    /** Renvoie le nombre d'octets entre les deux bornes inclusives. */
    public int length() {
        return end - start + 1;
    }
}
