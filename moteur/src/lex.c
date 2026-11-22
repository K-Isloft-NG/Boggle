#include <string.h>
#include "codes.h"
#include "lex.h"

// Numérote le nœud, ses fils puis ses frères, et renvoie la taille obtenue.
static long numeroter(Trie t, long debut) {
    const int maxInt = (int)(~0u >> 1);
    if (t == NULL) return 0;
    if (debut >= maxInt) return -1;

    t->num = (int) debut;
    long fils = numeroter(t->fils, debut + 1);
    if (fils < 0) return -1;
    long freres = numeroter(t->frere, debut + 1 + fils);
    if (freres < 0) return -1;
    return 1 + fils + freres;
}

// Écrit le nœud courant puis les deux sous-arbres dans l'ordre des numéros.
static int ecrire_cellules(FILE *f, Trie t) {
    if (t == NULL) return 0;

    CelluleLex cellule;
    // Le bourrage doit être identique d'une écriture à l'autre.
    memset(&cellule, 0, sizeof(cellule));
    cellule.elem = t->elem;
    cellule.finDeMot = t->finDeMot;
    cellule.fils = t->fils == NULL ? ABSENT_LEX : t->fils->num;
    cellule.frere = t->frere == NULL ? ABSENT_LEX : t->frere->num;
    if (fwrite(&cellule, sizeof(cellule), 1, f) != 1) return ERREUR_FICHIER;
    if (ecrire_cellules(f, t->fils) != 0) return ERREUR_FICHIER;
    return ecrire_cellules(f, t->frere);
}

// Écrit l'en-tête et les cellules, puis retire un fichier incomplet.
int ecrire_lex(Trie t, int nbMots, const char *chemin) {
    long nombre = numeroter(t, 0);
    if (nombre <= 0) return ERREUR_FICHIER;

    FILE *fichier = fopen(chemin, "wb");
    if (fichier == NULL) return ERREUR_FICHIER;

    EnteteLex entete = {sizeof(EnteteLex), nbMots, (int) nombre, sizeof(CelluleLex)};
    int resultat = ERREUR_FICHIER;
    if (fwrite(&entete, sizeof(entete), 1, fichier) == 1) {
        resultat = ecrire_cellules(fichier, t);
    }
    if (fclose(fichier) != 0) resultat = ERREUR_FICHIER;
    if (resultat != 0) remove(chemin);
    return resultat;
}

// Vérifie que le fichier a les dimensions annoncées par l'en-tête.
int lire_entete(FILE *f, EnteteLex *h) {
    if (fread(h, sizeof(*h), 1, f) != 1) return ERREUR_CONTENU;
    if (h->tailleEntete != sizeof(*h) || h->tailleCellule != sizeof(CelluleLex) ||
        h->nbMots < 0 || h->nbCellules < 1) return ERREUR_CONTENU;
    if (fseek(f, 0, SEEK_END) != 0) return ERREUR_CONTENU;

    long taille = ftell(f);
    long attendue = h->tailleEntete + (long) h->nbCellules * h->tailleCellule;
    return taille == attendue ? 0 : ERREUR_CONTENU;
}

// Accède à une cellule par son déplacement depuis le début du fichier.
int lire_cellule(FILE *f, const EnteteLex *h, int num, CelluleLex *c) {
    if (num < 0 || num >= h->nbCellules) return ERREUR_CONTENU;
    long position = h->tailleEntete + (long) num * h->tailleCellule;
    if (fseek(f, position, SEEK_SET) != 0) return ERREUR_CONTENU;
    if (fread(c, sizeof(*c), 1, f) != 1) return ERREUR_CONTENU;

    // Les liens d'un parcours préfixe pointent toujours vers l'avant.
    if ((c->fils != ABSENT_LEX && (c->fils <= num || c->fils >= h->nbCellules)) ||
        (c->frere != ABSENT_LEX && (c->frere <= num || c->frere >= h->nbCellules))) {
        return ERREUR_CONTENU;
    }
    return 0;
}

// Cherche la lettre parmi les frères, au maximum une fois par lettre de l'alphabet.
static int chercher_frere(FILE *f, const EnteteLex *h, int num, char lettre,
                          CelluleLex *cellule, int restants) {
    if (num == ABSENT_LEX) return LEX_ABSENT;
    if (restants == 0 || lire_cellule(f, h, num, cellule) != 0) return ERREUR_CONTENU;
    if (cellule->elem == lettre) return LEX_PRESENT;
    return chercher_frere(f, h, cellule->frere, lettre, cellule, restants - 1);
}

// Suit une lettre par niveau jusqu'à identifier le mot ou le préfixe.
int statut_mot(FILE *f, const EnteteLex *h, int num, const char *mot) {
    CelluleLex cellule;
    int resultat = chercher_frere(f, h, num, *mot, &cellule, 26);
    if (resultat != LEX_PRESENT) return resultat;
    if (*(mot + 1) == '\0') return cellule.finDeMot ? LEX_PRESENT : LEX_PREFIXE;
    if (cellule.fils == ABSENT_LEX) return LEX_ABSENT;
    return statut_mot(f, h, cellule.fils, mot + 1);
}
