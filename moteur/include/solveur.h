#pragma once

#include "grille.h"
#include "lex.h"

// Partage la grille, le Trie et l'état courant entre les appels récursifs.
typedef struct {
    const CelluleLex *cellules;
    const Grille *grille;
    int minimum;
    char *utilise;
    char *trouve;
    char *mot;
    int nbLettres;
    int nbMots;
} ContexteSolveur;

// Affiche les mots trouvés et renvoie leur nombre, ou -1 si la mémoire manque.
int resoudre(const CelluleLex *cellules, int nbCellules, const Grille *g,
             int minimum, FILE *sortie);
