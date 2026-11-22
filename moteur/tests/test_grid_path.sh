#!/bin/bash
cd "$(dirname "$0")/.." || exit 1
GRILLE="G A I R R U V E QU E O T A S M J"
reussites=0
echecs=0

# Vérifie la sortie standard et le code renvoyé pour chaque appel.
verifier() {
    local description=$1 attendu=$2 code_attendu=$3
    shift 3
    local sortie code=0
    sortie=$("$@" 2>/dev/null) || code=$?
    if [ "$code" -eq "$code_attendu" ] &&
       { [ "$sortie" = "$attendu" ] ||
         { [ "$attendu" = "Auteur: " ] && [[ "$sortie" == "Auteur: "* ]]; }; }; then
        printf 'OK %s\n' "$description"
        reussites=$((reussites + 1))
    else
        printf 'ECHEC %s : sortie « %s », code %d\n' "$description" "$sortie" "$code"
        echecs=$((echecs + 1))
    fi
}

verifier "diagonale OUI" "10 5 2" 0 bin/grid_path OUI 4 4 $GRILLE
verifier "mot TOIT absent" "" 1 bin/grid_path TOIT 4 4 $GRILLE
verifier "mot QUE avec une case QU" "8 9" 0 bin/grid_path QUE 4 4 $GRILLE
verifier "mot QU en une case" "8" 0 bin/grid_path QU 4 4 $GRILLE
verifier "mot GAIR sur une ligne" "0 1 2 3" 0 bin/grid_path GAIR 4 4 $GRILLE
verifier "retour arrière pour RUE" "4 5 9" 0 bin/grid_path RUE 4 4 $GRILLE
verifier "mot EVE avec deux cases E distinctes" "7 6 9" 0 bin/grid_path EVE 4 4 $GRILLE
verifier "case S inutilisable deux fois" "" 1 bin/grid_path SAS 4 4 $GRILLE
verifier "cases A non voisines" "" 1 bin/grid_path AA 4 4 $GRILLE
verifier "Q seul absent" "" 1 bin/grid_path Q 4 4 $GRILLE
verifier "mot en minuscules absent" "" 1 bin/grid_path oui 4 4 $GRILLE
verifier "mot avec un chiffre absent" "" 1 bin/grid_path A1 1 1 A
verifier "mot vide refusé" "" 255 bin/grid_path "" 4 4 $GRILLE

verifier "mot QUE avec Q et U séparés" "0 1 2" 0 bin/grid_path QUE 2 2 Q U E R
verifier "mot QUER avec Q et U séparés" "0 1 2 3" 0 bin/grid_path QUER 2 2 Q U E R
verifier "grille à une case" "0" 0 bin/grid_path A 1 1 A
verifier "grille à une case sans réutilisation" "" 1 bin/grid_path AA 1 1 A
verifier "mot ABCDE sur une ligne" "0 1 2 3 4" 0 bin/grid_path ABCDE 1 5 A B C D E
verifier "lettres ACE non voisines" "" 1 bin/grid_path ACE 1 5 A B C D E

verifier "aucun argument" "" 255 bin/grid_path
verifier "un seul argument" "" 255 bin/grid_path A
verifier "quatre arguments pour une grille de seize cases" "" 255 bin/grid_path A 4 4 G
verifier "une case de trop" "" 255 bin/grid_path A 4 4 $GRILLE A
verifier "une case manquante" "" 255 bin/grid_path A 4 4 G A I R R U V E QU E O T A S M
for dimension in 0 abc 1001; do
    verifier "dimension invalide $dimension" "" 255 bin/grid_path A "$dimension" 4 $GRILLE
done
verifier "case en minuscules" "" 255 bin/grid_path A 1 1 a
verifier "case de trois lettres" "" 255 bin/grid_path A 1 1 ABC
verifier "case contenant un chiffre" "" 255 bin/grid_path A 1 1 A1
verifier "case vide" "" 255 bin/grid_path A 1 1 ""
verifier "affichage de l'auteur" "Auteur: " 255 bin/grid_path author

verifier "mot absent après exploration d'une grille uniforme" "" 1 \
    bin/grid_path AAAAAAAAAAAAAAAAB 4 4 A A A A A A A A A A A A A A A A

if grille_produite=$(bin/grid_build tests/freq_qu.txt 3 3); then
    verifier "A absent de la grille générée" "" 1 bin/grid_path A 3 3 $grille_produite
    verifier "QU présent dans la grille générée" "0" 0 bin/grid_path QU 3 3 $grille_produite
else
    printf 'ECHEC génération de la grille\n'
    echecs=$((echecs + 1))
fi

printf 'Réussites : %d, échecs : %d\n' "$reussites" "$echecs"
[ "$echecs" -eq 0 ]
