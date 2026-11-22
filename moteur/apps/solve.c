#include <stdio.h>
#include <stdlib.h>
#include "author.h"
#include "codes.h"
#include "solveur.h"

// Charge le dictionnaire et affiche les mots de la grille.
int main(int argc, char *argv[]) {
    if (traiter_auteur(argc, argv)) return CODE_RETOUR_AUTEUR;
    if (argc < 6) {
        fprintf(stderr, "Usage : solve dictionnaire.lex minimum hauteur largeur case...\n");
        return ERREUR_PARAMETRES;
    }

    int minimum, hauteur, largeur;
    if (!lire_entier(argv[2], &minimum) ||
        !lire_entier(argv[3], &hauteur) ||
        !lire_entier(argv[4], &largeur) || argc != 5 + hauteur * largeur) {
        fprintf(stderr, "Minimum, dimensions ou nombre de cases invalide\n");
        return ERREUR_PARAMETRES;
    }

    Grille grille;
    int resultat = construire_grille(&grille, hauteur, largeur, &argv[5]);
    if (resultat != 0) {
        fprintf(stderr, "Une case est invalide\n");
        return resultat;
    }

    FILE *fichier = fopen(argv[1], "rb");
    if (fichier == NULL) {
        fprintf(stderr, "Dictionnaire inaccessible : %s\n", argv[1]);
        return ERREUR_FICHIER;
    }

    EnteteLex entete;
    CelluleLex *cellules = NULL;
    resultat = lire_entete(fichier, &entete);
    if (resultat == 0) resultat = charger_cellules(fichier, &entete, &cellules);
    if (fclose(fichier) != 0 && resultat == 0) resultat = ERREUR_FICHIER;
    if (resultat != 0) {
        fprintf(stderr, "Impossible de charger le dictionnaire : %s\n", argv[1]);
        free(cellules);
        return resultat;
    }

    int nbMots = resoudre(cellules, entete.nbCellules, &grille, minimum, stdout);
    free(cellules);
    if (nbMots < 0) {
        fprintf(stderr, "Mémoire insuffisante pour la recherche\n");
        return ERREUR_CONTENU;
    }
    return 0;
}
