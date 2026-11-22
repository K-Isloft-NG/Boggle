#pragma once

// Une lettre, sa marque de fin de mot et ses liens dans le Trie.
typedef struct noeud {
    char elem;
    char finDeMot;
    int num;
    struct noeud *fils;
    struct noeud *frere;
} TrieNode;

// Adresse du premier nœud d'une liste de frères.
typedef TrieNode *Trie;

// Crée un nœud pour la lettre donnée.
Trie nouveau_noeud(char elem);

// Cherche la lettre parmi les frères et l'ajoute si elle manque.
Trie chercher_inserer(Trie *t, char x);

// Renvoie 1 pour un nouveau mot, 0 pour un doublon et -1 si la mémoire manque.
int inserer_mot(Trie *t, const char *mot);

// Libère tous les nœuds du Trie.
void liberer_trie(Trie t);
