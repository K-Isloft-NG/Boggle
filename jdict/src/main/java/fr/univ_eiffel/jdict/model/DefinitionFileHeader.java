package fr.univ_eiffel.jdict.model;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

/** Contient les métadonnées de la première ligne du fichier de définitions. */
public record DefinitionFileHeader(String description, String createdOn, String language) {
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'", Locale.ROOT).withZone(ZoneOffset.UTC);

    /** Vérifie que chaque champ est présent. */
    public DefinitionFileHeader {
        Objects.requireNonNull(description);
        Objects.requireNonNull(createdOn);
        Objects.requireNonNull(language);
    }

    /** Construit l'en-tête d'un dictionnaire avec une date de création en UTC. */
    public static DefinitionFileHeader forLanguage(String language, Instant createdAt) {
        return new DefinitionFileHeader("definition file", DATE_FORMAT.format(createdAt), language);
    }
}
