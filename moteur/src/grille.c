#include <assert.h>
#include <stdio.h>
#include <string.h>
#include "codes.h"
#include "grille.h"

// Lit une dimension entière comprise entre 1 et 1000.
int lire_entier(const char *texte, int *valeur) {
    assert(texte != NULL);
    assert(valeur != NULL);

    char reste;
    const char *p = texte;
    while (*p == ' ' || *p == '\t' || *p == '\n' || *p == '\r' || *p == '\v' || *p == '\f') p++;
    if (*p == '+') p++;
    while (*p == '0') p++;
    int chiffres = 0;
    while (*p >= '0' && *p <= '9') {
        chiffres++;
        if (chiffres > 4) return 0;
        p++;
    }
    if (*p != '\0') return 0;
    return sscanf(texte, "%d%c", valeur, &reste) == 1 && *valeur >= 1 && *valeur <= 1000;
}

// Vérifie chaque case avant de conserver le tableau d'arguments.
int construire_grille(Grille *g, int hauteur, int largeur, char *cases[]) {
    assert(g != NULL);
    assert(cases != NULL);

    if (hauteur < 1 || hauteur > 1000 || largeur < 1 || largeur > 1000)
        return ERREUR_PARAMETRES;

    for (int i = 0; i < hauteur * largeur; i++) {
        assert(cases[i] != NULL);
        size_t longueur = strlen(cases[i]);
        if (longueur < 1 || longueur > 2) return ERREUR_PARAMETRES;

        for (size_t j = 0; j < longueur; j++) {
            if (cases[i][j] < 'A' || cases[i][j] > 'Z') return ERREUR_PARAMETRES;
        }
    }

    g->hauteur = hauteur;
    g->largeur = largeur;
    g->cases = cases;
    return 0;
}

// Applique le déplacement sur chaque coordonnée pour respecter les bords.
int voisin_grille(const Grille *g, int indice, int k) {
    assert(g != NULL);
    assert(indice >= 0 && indice < g->hauteur * g->largeur);
    assert(k >= 0 && k <= 8);

    int ligne = indice / g->largeur + k / 3 - 1;
    int colonne = indice % g->largeur + k % 3 - 1;
    if (k == 4 || ligne < 0 || ligne >= g->hauteur ||
        colonne < 0 || colonne >= g->largeur) return -1;

    return ligne * g->largeur + colonne;
}
