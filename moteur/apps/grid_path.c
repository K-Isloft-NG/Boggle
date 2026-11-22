#include <stdio.h>
#include <stdlib.h>
#include "author.h"
#include "chemin.h"
#include "codes.h"

// Affiche la première suite de cases formant le mot.
int main(int argc, char *argv[]) {
    if (traiter_auteur(argc, argv)) return CODE_RETOUR_AUTEUR;
    if (argc < 5) {
        fprintf(stderr, "Usage : grid_path mot hauteur largeur case...\n");
        return ERREUR_PARAMETRES;
    }

    int hauteur, largeur;
    const char *mot = argv[1];
    if (*mot == '\0' || !lire_entier(argv[2], &hauteur) ||
        !lire_entier(argv[3], &largeur) || argc != 4 + hauteur * largeur) {
        fprintf(stderr, "Mot, dimensions ou nombre de cases invalide\n");
        return ERREUR_PARAMETRES;
    }

    Grille grille;
    int resultat = construire_grille(&grille, hauteur, largeur, &argv[4]);
    if (resultat != 0) {
        fprintf(stderr, "Une case est invalide\n");
        return resultat;
    }

    int *chemin = malloc((size_t) (hauteur * largeur) * sizeof(*chemin));
    if (chemin == NULL) {
        fprintf(stderr, "Mémoire insuffisante pour le chemin\n");
        return ERREUR_CONTENU;
    }

    int longueur = chercher_chemin(&grille, mot, chemin);
    if (longueur > 0) {
        for (int i = 0; i < longueur; i++) {
            if (i > 0) printf(" ");
            printf("%d", chemin[i]);
        }
        printf("\n");
    }
    free(chemin);

    if (longueur < 0) {
        fprintf(stderr, "Mémoire insuffisante pour la recherche\n");
        return ERREUR_CONTENU;
    }
    if (longueur > 0) return 0;
    return 1;
}
