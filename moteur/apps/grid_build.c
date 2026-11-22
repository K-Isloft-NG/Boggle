#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include "author.h"
#include "frequences.h"
#include "grille.h"

// Construit une grille à partir des fréquences fournies.
int main(int argc, char *argv[]) {
    int hauteur, largeur;
    TableFrequences table;
    if (traiter_auteur(argc, argv)) return CODE_RETOUR_AUTEUR;
    if (argc != 4) {
        fprintf(stderr, "Usage : grid_build frequences hauteur largeur\n");
        return ERREUR_PARAMETRES;
    }
    if (!lire_entier(argv[2], &hauteur) || !lire_entier(argv[3], &largeur)) {
        fprintf(stderr, "Dimensions invalides\n");
        return ERREUR_PARAMETRES;
    }
    int resultat = charger_frequences(argv[1], &table);
    if (resultat != 0) {
        fprintf(stderr, "Fichier de fréquences invalide ou inaccessible\n");
        return resultat;
    }
    srand((unsigned int) time(NULL));
    for (int i = 0; i < hauteur * largeur; i++) {
        if (i > 0) printf(" ");
        printf("%s", tirer_symbole(&table));
    }
    printf("\n");
    liberer_frequences(&table);
    return 0;
}
