#pragma once

#include "grille.h"

// Renvoie le nombre de cases du chemin, 0 si absent ou -1 si la mémoire manque.
int chercher_chemin(const Grille *g, const char *mot, int *chemin);
