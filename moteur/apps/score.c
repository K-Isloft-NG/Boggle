#include <stdio.h>
#include "author.h"
#include "boggle.h"

// Calcule le score des mots reçus en arguments.
int main(int argc, char *argv[]) {
    if (traiter_auteur(argc, argv)) {
        return CODE_RETOUR_AUTEUR;
    }
    char **mots = &argv[1];
    int nombre = argc - 1;
    printf("%d\n", score(mots, nombre));
    return 0;
}
