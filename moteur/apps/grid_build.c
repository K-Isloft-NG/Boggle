#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include "author.h"
#include "frequences.h"

// Lit une dimension entière comprise entre 1 et 1000.
static int lire_dimension(const char *texte, int *valeur) {
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

// Construit une grille à partir des fréquences fournies.
int main(int argc, char *argv[]) {
    int hauteur, largeur;
    TableFrequences table;
    if (traiter_auteur(argc, argv)) return CODE_RETOUR_AUTEUR;
    if (argc != 4) {
        fprintf(stderr, "Usage : grid_build frequences hauteur largeur\n");
        return ERREUR_PARAMETRES;
    }
    if (!lire_dimension(argv[2], &hauteur) || !lire_dimension(argv[3], &largeur)) {
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
