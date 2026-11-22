#include <limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "codes.h"
#include "dictionnaire.h"

// Mesure la plus longue ligne pour adapter le tampon de lecture.
static int mesurer_lignes(FILE *fichier, size_t *maximum) {
    size_t longueur = 0;
    int c;
    while ((c = fgetc(fichier)) != EOF) {
        if (c == '\n') {
            longueur = 0;
        } else {
            if (longueur == (size_t) INT_MAX - 2) return ERREUR_CONTENU;
            longueur++;
            if (longueur > *maximum) *maximum = longueur;
        }
    }
    return ferror(fichier) ? ERREUR_FICHIER : 0;
}

// Repère et termine le premier mot si toutes ses lettres sont valides.
static int isoler_mot(char *ligne) {
    size_t i = 0;
    size_t longueur = strlen(ligne);
    while (i < longueur && ligne[i] != ' ' && ligne[i] != '\t' &&
           ligne[i] != '\r' && ligne[i] != '\n') {
        if (ligne[i] < 'A' || ligne[i] > 'Z') return 0;
        i++;
    }
    ligne[i] = '\0';
    return i > 0;
}

// Lit les lignes entières et ajoute les mots valides au Trie.
static int lire_mots(FILE *fichier, char *ligne, int capacite, Trie *t, int *nbMots) {
    while (fgets(ligne, capacite, fichier) != NULL) {
        if (isoler_mot(ligne)) {
            int ajout = inserer_mot(t, ligne);
            if (ajout < 0 || (ajout > 0 && *nbMots == INT_MAX)) return ERREUR_CONTENU;
            *nbMots += ajout;
        }
    }
    if (ferror(fichier)) return ERREUR_FICHIER;
    return *nbMots == 0 ? ERREUR_CONTENU : 0;
}

// Mesure le fichier puis le relit avec un tampon ajusté à la plus longue ligne.
int charger_dictionnaire(const char *chemin, Trie *t, int *nbMots) {
    *t = NULL;
    *nbMots = 0;
    FILE *fichier = fopen(chemin, "rb");
    if (fichier == NULL) return ERREUR_FICHIER;
    size_t maximum = 0;
    int resultat = mesurer_lignes(fichier, &maximum);
    if (resultat == 0) {
        char *ligne = malloc(maximum + 2);
        if (ligne == NULL) {
            resultat = ERREUR_CONTENU;
        } else {
            rewind(fichier);
            resultat = lire_mots(fichier, ligne, (int) maximum + 2, t, nbMots);
            free(ligne);
        }
    }
    if (fclose(fichier) != 0 && resultat == 0) resultat = ERREUR_FICHIER;
    if (resultat != 0) {
        liberer_trie(*t);
        *t = NULL;
        *nbMots = 0;
    }
    return resultat;
}
