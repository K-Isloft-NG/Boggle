#pragma once

// Codes de retour pour les anomalies.
#define ERREUR_PARAMETRES 255
#define ERREUR_FICHIER 254
#define ERREUR_CONTENU 253

// Un symbole et son poids dans le tirage.
typedef struct {
    char texte[3];
    int poids;
} Symbole;

// Tableau des symboles et somme de leurs poids.
typedef struct {
    Symbole *symboles;
    int nombre;
    int total;
} TableFrequences;

// Charge et vérifie les fréquences depuis un fichier.
int charger_frequences(const char *chemin, TableFrequences *table);

// Tire un symbole selon son poids.
const char *tirer_symbole(const TableFrequences *table);

// Libère le tableau des symboles.
void liberer_frequences(TableFrequences *table);
