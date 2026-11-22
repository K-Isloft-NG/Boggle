#include <stdio.h>
#include <stdlib.h>
#include "frequences.h"

// Lit une entrée complète et contrôle ses séparateurs.
static int lire_entree(FILE *fichier, Symbole *symbole) {
    fpos_t debut;
    int caractere;
    if (fgetpos(fichier, &debut) != 0) return -1;
    int resultat = fscanf(fichier, "%2s", symbole->texte);
    if (resultat == EOF) return ferror(fichier) ? -1 : 0;
    if (resultat != 1) return -1;
    caractere = fgetc(fichier);
    if (caractere != ' ' && caractere != '\t') return -1;
    do {
        caractere = fgetc(fichier);
    } while (caractere == ' ' || caractere == '\t');
    int negatif = 0;
    if (caractere == '+' || caractere == '-') {
        negatif = caractere == '-';
        caractere = fgetc(fichier);
    }
    if (caractere < '0' || caractere > '9') return -1;
    int poids = 0;
    do {
        int chiffre = caractere - '0';
        if (poids > (RAND_MAX - chiffre) / 10) return -1;
        poids = poids * 10 + chiffre;
        caractere = fgetc(fichier);
    } while (caractere >= '0' && caractere <= '9');
    if (negatif && poids != 0) return -1;
    if (caractere != EOF && ungetc(caractere, fichier) == EOF) return -1;
    if (fsetpos(fichier, &debut) != 0 ||
        fscanf(fichier, "%2s %d", symbole->texte, &symbole->poids) != 2) return -1;
    for (int i = 0; symbole->texte[i] != '\0'; i++) {
        if (symbole->texte[i] < 'A' || symbole->texte[i] > 'Z') return -1;
    }
    while ((caractere = fgetc(fichier)) != '\n' && caractere != EOF) {
        if (caractere != ' ' && caractere != '\t' && caractere != '\r') return -1;
    }
    if (ferror(fichier) || symbole->poids < 0) return -1;
    return 1;
}

// Vérifie les entrées puis alloue et remplit la table.
int charger_frequences(const char *chemin, TableFrequences *table) {
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
    free(table->symboles);
    table->symboles = NULL;
    table->nombre = 0;
    table->total = 0;
}
