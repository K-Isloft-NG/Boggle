package fr.univ_eiffel.jdict.export;

import java.util.regex.Pattern;

/** Produit le script d'insertion d'un dictionnaire. */
public final class SqlScriptWriter {
    private static final Pattern LANGUAGE = Pattern.compile("[a-z]{2}");
    private static final Pattern VERSION = Pattern.compile("[0-9]{8}");
    private static final Pattern CONTROL = Pattern.compile("\\p{Cc}");

    private SqlScriptWriter() {
    }

    /** Valide les champs et renvoie le script SQL terminé par un LF. */
    public static String render(String language, String label, String version, long wordCount) {
        if (language == null || !LANGUAGE.matcher(language).matches()) {
            throw new IllegalArgumentException("La langue doit contenir deux lettres minuscules.");
        }
        if (version == null || !VERSION.matcher(version).matches()) {
            throw new IllegalArgumentException("La version doit contenir huit chiffres (AAAAMMJJ).");
        }
        if (label == null || label.isBlank() || label.codePointCount(0, label.length()) > 100
                || CONTROL.matcher(label).find()) {
            throw new IllegalArgumentException("Le libelle doit contenir de 1 a 100 caracteres sans controle.");
        }
        if (wordCount <= 0) {
            throw new IllegalArgumentException("Le nombre de mots doit etre positif.");
        }
        String escapedLabel = label.replace("'", "''");
        return "-- Dictionnaire de la langue " + language + ", version " + version + ", " + wordCount + " mots.\n"
                + "-- Script genere par SqlScriptMaker.\n"
                + "INSERT INTO boggle.dictionnaire (code_langue, libelle, version, nb_mots)\n"
                + "VALUES ('" + language + "', '" + escapedLabel + "', '" + version + "', " + wordCount + ")\n"
                + "ON CONFLICT (code_langue, version) DO NOTHING;\n";
    }
}
