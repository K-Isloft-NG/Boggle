#include <stdio.h>
#include <string.h>
#include "author.h"

// Reconnaît la demande d'affichage de l'auteur.
int traiter_auteur(int argc, char *argv[]) {
    if (argc == 2 && strcmp(argv[1], "author") == 0) {
        printf("Auteur: Karl NATNGAM\n");
        return 1;
    }
    return 0;
}
