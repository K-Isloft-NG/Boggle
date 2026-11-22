#include <assert.h>
#include <stdlib.h>
#include <string.h>
#include "chemin.h"

// Essaie d'ajouter une case au chemin en cours.
static int explorer(const Grille *g, const char *mot, int indice, int profondeur,
                    char *utilise, int *chemin) {
    assert(g != NULL);
    assert(mot != NULL);
    assert(utilise != NULL);
    assert(chemin != NULL);
    assert(indice >= 0 && indice < g->hauteur * g->largeur);
    assert(profondeur >= 0 && profondeur < g->hauteur * g->largeur);

    if (utilise[indice]) return 0;
    const char *caseCourante = g->cases[indice];
    size_t longueur = strlen(caseCourante);
    if (strncmp(mot, caseCourante, longueur) != 0) return 0;

    utilise[indice] = 1;
    chemin[profondeur] = indice;
    mot += longueur;
    if (*mot == '\0') return profondeur + 1;

    for (int k = 0; k < 9; k++) {
        int voisin = voisin_grille(g, indice, k);
        if (voisin >= 0 && !utilise[voisin]) {
            int resultat = explorer(g, mot, voisin, profondeur + 1, utilise, chemin);
            if (resultat > 0) return resultat;
        }
    }

    // La case redevient disponible lorsqu'aucune suite ne convient.
    utilise[indice] = 0;
    return 0;
}

// Cherche un chemin en essayant les départs dans l'ordre des indices.
int chercher_chemin(const Grille *g, const char *mot, int *chemin) {
    assert(g != NULL);
    assert(g->cases != NULL);
    assert(mot != NULL);
    assert(chemin != NULL);

    int nombre = g->hauteur * g->largeur;
    char *utilise = calloc((size_t) nombre, sizeof(*utilise));
    if (utilise == NULL) return -1;

    int resultat = 0;
    for (int i = 0; i < nombre && resultat == 0; i++) {
        resultat = explorer(g, mot, i, 0, utilise, chemin);
    }

    free(utilise);
    return resultat;
}
