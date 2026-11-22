#include <stdio.h>
#include "author.h"
#include "codes.h"
#include "dictionnaire.h"
#include "lex.h"

// Convertit un dictionnaire texte en Trie sur disque.
int main(int argc, char *argv[]) {
    if (traiter_auteur(argc, argv)) return CODE_RETOUR_AUTEUR;
    if (argc != 3) {
        fprintf(stderr, "Usage : dictionnary_build dictionnaire.txt dictionnaire.lex\n");
        return ERREUR_PARAMETRES;
    }

    Trie trie;
    int nbMots;
    int resultat = charger_dictionnaire(argv[1], &trie, &nbMots);
    if (resultat != 0) {
        fprintf(stderr, "Impossible de charger le dictionnaire : %s\n", argv[1]);
        return resultat;
    }

    resultat = ecrire_lex(trie, nbMots, argv[2]);
    liberer_trie(trie);
    if (resultat != 0) {
        fprintf(stderr, "Impossible d'écrire le dictionnaire : %s\n", argv[2]);
    }
    return resultat;
}
