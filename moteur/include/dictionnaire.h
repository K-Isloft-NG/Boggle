#pragma once

#include "trie.h"

// Charge les mots valides et compte les mots distincts.
int charger_dictionnaire(const char *chemin, Trie *t, int *nbMots);
