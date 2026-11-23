package fr.univ_eiffel.jdict.store;

/** Définit l'ordre des entrées du dictionnaire dans l'index. */
public final class IndexOrder {
    private IndexOrder() {
    }

    /** Compare les formes normalisées, puis les titres d'origine. */
    public static int compare(String keyA, String titleA, String keyB, String titleB) {
        int comparison = keyA.compareTo(keyB);
        return comparison == 0 ? titleA.compareTo(titleB) : comparison;
    }
}
