#pragma once

#include <stdio.h>
#include "trie.h"

// Dimensions et compteurs placés au début du fichier.
typedef struct {
    int tailleEntete;
    int nbMots;
    int nbCellules;
    int tailleCellule;
} EnteteLex;

// Une lettre et les numéros des cellules qui lui sont reliées.
typedef struct {
    char elem;
    char finDeMot;
    int fils;
    int frere;
} CelluleLex;

// Numéro d'un fils ou d'un frère inexistant.
#define ABSENT_LEX (-1)

// Statuts possibles d'une recherche.
#define LEX_PRESENT 0
#define LEX_PREFIXE 1
#define LEX_ABSENT 2

// Enregistre le Trie en ordre préfixe.
int ecrire_lex(Trie t, int nbMots, const char *chemin);

// Lit et vérifie les dimensions enregistrées.
int lire_entete(FILE *f, EnteteLex *h);

// Lit la cellule désignée par son numéro.
int lire_cellule(FILE *f, const EnteteLex *h, int num, CelluleLex *c);

// Recherche un mot à partir du numéro de la première cellule.
int statut_mot(FILE *f, const EnteteLex *h, int num, const char *mot);
