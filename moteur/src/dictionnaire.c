#include <stdio.h>
#include <stdlib.h>
#include "codes.h"
#include "dictionnaire.h"

// Mesure les lignes et repère les octets nuls pouvant se trouver dans une ligne.
static int mesurer_lignes(FILE *fichier, int *maximum, int *contientNul) {
    const int maxInt = (int)(~0u >> 1);
    int longueur = 0;
    int c;
    while ((c = fgetc(fichier)) != EOF) {
        if (c == '\n') {
            longueur = 0;
        } else {
            // Deux octets restent nécessaires pour le saut de ligne et le zéro final.
            if (longueur == maxInt - 2) return ERREUR_CONTENU;
            longueur++;
            if (longueur > *maximum) *maximum = longueur;
            if (c == '\0') *contientNul = 1;
        }
    }
    return ferror(fichier) ? ERREUR_FICHIER : 0;
}

// Repère et termine le premier mot si toutes ses lettres sont valides.
static int isoler_mot(char *ligne, long longueur) {
    long i = 0;
    while (i < longueur && ligne[i] != ' ' && ligne[i] != '\t' &&
           ligne[i] != '\r' && ligne[i] != '\n') {
        if (ligne[i] < 'A' || ligne[i] > 'Z') return 0;
        i++;
    }

    ligne[i] = '\0';
    return i > 0;
}

// Lit les lignes entières et ajoute les mots valides au Trie.
static int lire_mots(FILE *fichier, char *ligne, int capacite, int contientNul,
                     Trie *t, int *nbMots) {
    const int maxInt = (int)(~0u >> 1);
    long position = 0;
    while (fgets(ligne, capacite, fichier) != NULL) {
        long longueur = 0;
        if (contientNul) {
            // La position distingue un zéro du fichier du zéro ajouté par fgets.
            long fin = ftell(fichier);
            if (fin < position) return ERREUR_FICHIER;
            longueur = fin - position;
            position = fin;
        } else {
            while (ligne[longueur] != '\0') longueur++;
        }
        if (isoler_mot(ligne, longueur)) {
            int ajout = inserer_mot(t, ligne);
            if (ajout < 0 || (ajout > 0 && *nbMots == maxInt)) return ERREUR_CONTENU;
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

    int maximum = 0;
    int contientNul = 0;
    int resultat = mesurer_lignes(fichier, &maximum, &contientNul);
    if (resultat == 0) {
        char *ligne = malloc((size_t) maximum + 2);
        if (ligne == NULL) {
            resultat = ERREUR_CONTENU;
        } else {
            rewind(fichier);
            resultat = lire_mots(fichier, ligne, maximum + 2, contientNul, t, nbMots);
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
