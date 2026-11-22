#include <assert.h>
#include <stdlib.h>
#include "solveur.h"

// Cherche une lettre dans la liste de frères.
static int chercher_frere(const CelluleLex *cellules, int tete, char lettre) {
    assert(cellules != NULL);
    assert(tete >= 0 || tete == ABSENT_LEX);

    if (tete == ABSENT_LEX) return ABSENT_LEX;
    if (cellules[tete].elem == lettre) return tete;
    return chercher_frere(cellules, cellules[tete].frere, lettre);
}

// Avance dans le Trie avec toutes les lettres de la case, puis essaie ses voisins.
static void explorer(ContexteSolveur *ctx, int indice, int tete, FILE *sortie) {
    assert(ctx != NULL);
    assert(sortie != NULL);
    const Grille *g = ctx->grille;
    assert(indice >= 0 && indice < g->hauteur * g->largeur);

    const char *texte = g->cases[indice];
    int n = tete;
    for (int i = 0; texte[i] != '\0'; i++) {
        n = chercher_frere(ctx->cellules, n, texte[i]);
        if (n == ABSENT_LEX) return;
        if (texte[i + 1] != '\0') n = ctx->cellules[n].fils;
    }

    int debut = ctx->nbLettres;
    for (int i = 0; texte[i] != '\0'; i++) {
        ctx->mot[ctx->nbLettres++] = texte[i];
    }
    ctx->mot[ctx->nbLettres] = '\0';

    const CelluleLex *cellule = &ctx->cellules[n];
    if (cellule->finDeMot && ctx->nbLettres >= ctx->minimum && !ctx->trouve[n]) {
        ctx->trouve[n] = 1;
        if (ctx->nbMots > 0) fprintf(sortie, " ");
        fprintf(sortie, "%s", ctx->mot);
        ctx->nbMots++;
    }

    if (cellule->fils != ABSENT_LEX) {
        ctx->utilise[indice] = 1;
        for (int k = 0; k < 9; k++) {
            int voisin = voisin_grille(g, indice, k);
            if (voisin >= 0 && !ctx->utilise[voisin]) {
                explorer(ctx, voisin, cellule->fils, sortie);
            }
        }
        ctx->utilise[indice] = 0;
    }

    ctx->nbLettres = debut;
    ctx->mot[debut] = '\0';
}

// Prépare les tableaux de travail avant d'essayer tous les départs.
int resoudre(const CelluleLex *cellules, int nbCellules, const Grille *g,
             int minimum, FILE *sortie) {
    assert(cellules != NULL);
    assert(g != NULL && g->cases != NULL);
    assert(sortie != NULL);
    assert(nbCellules > 0);
    assert(g->hauteur >= 1 && g->hauteur <= 1000);
    assert(g->largeur >= 1 && g->largeur <= 1000);

    int nombre = g->hauteur * g->largeur;
    size_t nbCases = (size_t) nombre;
    ContexteSolveur ctx = {cellules, g, minimum, NULL, NULL, NULL, 0, 0};
    ctx.utilise = calloc(nbCases, sizeof(*ctx.utilise));
    ctx.trouve = calloc((size_t) nbCellules, sizeof(*ctx.trouve));
    ctx.mot = calloc(2 * nbCases + 1, sizeof(*ctx.mot));

    int resultat = -1;
    if (ctx.utilise != NULL && ctx.trouve != NULL && ctx.mot != NULL) {
        for (int indice = 0; indice < nombre; indice++) {
            explorer(&ctx, indice, 0, sortie);
        }
        fprintf(sortie, "\n");
        resultat = ctx.nbMots;
    }

    free(ctx.mot);
    free(ctx.trouve);
    free(ctx.utilise);
    return resultat;
}
