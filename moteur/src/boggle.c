#include <string.h>
#include "boggle.h"

// Barème : moins de 3 lettres 0, 3 ou 4 : 1, 5 : 2, 6 : 3, 7 : 5, 8 ou plus : 11.
int points_du_mot(const char *mot) {
    size_t longueur = strlen(mot);
    if (longueur < 3) return 0;
    if (longueur <= 4) return 1;
    if (longueur == 5) return 2;
    if (longueur == 6) return 3;
    if (longueur == 7) return 5;
    return 11;
}

// Additionne les points sans modifier les mots.
int score(char *mots[], int nombre) {
    int total = 0;
    for (int i = 0; i < nombre; i++) {
        total += points_du_mot(mots[i]);
    }
    return total;
}
