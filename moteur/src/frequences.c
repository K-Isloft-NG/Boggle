#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "frequences.h"

// Lit une entrée et vérifie le symbole et le poids.
static int lire_entree(FILE *fichier, Symbole *symbole) {
    // Un symbole, un poids de neuf chiffres et des espaces tiennent dans 60 caractères.
    char ligne[64];
    char reste;
    while (fgets(ligne, sizeof(ligne), fichier) != NULL) {
        if (strchr(ligne, '\n') == NULL && !feof(fichier)) return -1;
        int conversions = sscanf(ligne, "%2s %9d %c", symbole->texte, &symbole->poids, &reste);
        if (conversions == EOF) continue;
        if (conversions != 2 || symbole->poids < 0) return -1;
        for (int i = 0; symbole->texte[i] != '\0'; i++) {
            if (symbole->texte[i] < 'A' || symbole->texte[i] > 'Z') return -1;
        }
        return 1;
    }
    return ferror(fichier) ? -1 : 0;
}

// Vérifie les entrées puis alloue et remplit la table.
int charger_frequences(const char *chemin, TableFrequences *table) {
    assert(chemin != NULL);
    assert(table != NULL);
    FILE *fichier = fopen(chemin, "r");
    Symbole symbole;
    int resultat;
    table->symboles = NULL;
    table->nombre = 0;
    table->total = 0;
    if (fichier == NULL) return ERREUR_FICHIER;
    while ((resultat = lire_entree(fichier, &symbole)) == 1) {
        if (symbole.poids > RAND_MAX - table->total || table->nombre == RAND_MAX) {
            fclose(fichier);
            return ERREUR_CONTENU;
        }
        table->nombre++;
        table->total += symbole.poids;
    }
    if (resultat < 0 || table->nombre == 0 || table->total == 0) {
        fclose(fichier);
        table->nombre = 0;
        table->total = 0;
        return ERREUR_CONTENU;
    }
    rewind(fichier);
    table->symboles = malloc((size_t)table->nombre * sizeof(Symbole));
    if (table->symboles == NULL) {
        fclose(fichier);
        table->nombre = 0;
        table->total = 0;
        return ERREUR_CONTENU;
    }
    for (int i = 0; i < table->nombre; i++) {
        if (lire_entree(fichier, &table->symboles[i]) != 1) {
            fclose(fichier);
            liberer_frequences(table);
            return ERREUR_CONTENU;
        }
    }
    fclose(fichier);
    return 0;
}

// Tire un symbole en parcourant les poids.
const char *tirer_symbole(const TableFrequences *table) {
    assert(table != NULL);
    assert(table->symboles != NULL && table->total > 0);
    // Le biais du modulo est faible lorsque le total est très inférieur à RAND_MAX.
    int r = rand() % table->total;
    for (int i = 0; i < table->nombre; i++) {
        r -= table->symboles[i].poids;
        if (r < 0) return table->symboles[i].texte;
    }
    return NULL;
}

// Libère la table et remet ses champs à zéro.
void liberer_frequences(TableFrequences *table) {
    assert(table != NULL);
    free(table->symboles);
    table->symboles = NULL;
    table->nombre = 0;
    table->total = 0;
}
