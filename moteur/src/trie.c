#include <stdlib.h>
#include "trie.h"

// Initialise un nœud sans fils ni frère.
Trie nouveau_noeud(char elem) {
    Trie noeud = malloc(sizeof(TrieNode));
    if (noeud == NULL) return NULL;

    noeud->elem = elem;
    noeud->finDeMot = 0;
    noeud->num = -1;
    noeud->fils = NULL;
    noeud->frere = NULL;
    return noeud;
}

// Cherche la lettre ou la relie à la fin de la liste.
Trie chercher_inserer(Trie *t, char x) {
    if (*t == NULL) {
        *t = nouveau_noeud(x);
        return *t;
    }
    if ((*t)->elem == x) return *t;
    return chercher_inserer(&(*t)->frere, x);
}

// Descend d'un fils par lettre et marque la dernière.
int inserer_mot(Trie *t, const char *mot) {
    Trie noeud = chercher_inserer(t, *mot);
    if (noeud == NULL) return -1;
    if (*(mot + 1) != '\0') return inserer_mot(&noeud->fils, mot + 1);

    if (noeud->finDeMot) return 0;
    noeud->finDeMot = 1;
    return 1;
}

// Libère les frères et les fils avant le nœud courant.
void liberer_trie(Trie t) {
    if (t == NULL) return;
    liberer_trie(t->frere);
    liberer_trie(t->fils);
    free(t);
}
