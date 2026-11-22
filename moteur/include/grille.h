#pragma once

// Grille dont les cases pointent vers les arguments de la commande.
typedef struct {
    int hauteur;
    int largeur;
    char **cases;
} Grille;

// Lit un entier compris entre 1 et 1000.
int lire_entier(const char *texte, int *valeur);

// Vérifie les dimensions et les cases avant de ranger la grille.
int construire_grille(Grille *g, int hauteur, int largeur, char *cases[]);

// Renvoie l'indice du voisin ou -1 si la position ne désigne aucun voisin.
int voisin_grille(const Grille *g, int indice, int k);
