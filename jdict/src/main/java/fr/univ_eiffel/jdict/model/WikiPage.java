package fr.univ_eiffel.jdict.model;

import java.util.Objects;

/** Contient le titre et le texte d'une page du Wiktionnaire. */
public record WikiPage(String title, String text) {
    /** Vérifie que le titre et le texte sont présents. */
    public WikiPage {
        Objects.requireNonNull(title, "Le titre de la page est obligatoire.");
        Objects.requireNonNull(text, "Le texte de la page est obligatoire.");
    }
}
