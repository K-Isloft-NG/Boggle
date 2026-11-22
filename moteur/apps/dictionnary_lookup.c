#include <stdio.h>
#include <string.h>
#include "author.h"
#include "codes.h"
#include "lex.h"

// Cherche un mot dans le dictionnaire et renvoie son statut.
int main(int argc, char *argv[]) {
    if (traiter_auteur(argc, argv)) return CODE_RETOUR_AUTEUR;
    if (argc != 3 || *argv[2] == '\0') return ERREUR_PARAMETRES;

    FILE *fichier = fopen(argv[1], "rb");
    if (fichier == NULL) return ERREUR_FICHIER;

    EnteteLex entete;
    int resultat = lire_entete(fichier, &entete);
    if (resultat == 0) {
        size_t longueur = strspn(argv[2], "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        if (argv[2][longueur] == '\0') {
            resultat = statut_mot(fichier, &entete, 0, argv[2]);
        } else {
            resultat = LEX_ABSENT;
        }
    }
    if (fclose(fichier) != 0) return ERREUR_CONTENU;
    return resultat;
}
