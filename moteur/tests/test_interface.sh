#!/bin/bash
cd "$(dirname "$0")/.." || exit 1
tmp=$(mktemp -d "$PWD/obj/interface.XXXXXX") || exit 1
trap 'rm -rf -- "$tmp"' EXIT
export LC_ALL=C
grille="G A I R R U V E QU E O T A S M J"
lex="$tmp/dico.lex"
reussites=0
echecs=0

# Compare le résultat attendu à la sortie et au code de retour.
verifier() {
    local description=$1 attendu=$2 code_attendu=$3 type=$4 code=0 sortie
    shift 4
    sortie=$("$@" 2>/dev/null) || code=$?
    if [ "$type" = auteur ]; then
        [[ "$sortie" == 'Auteur: '* ]] || code=-1
    elif [ "$type" = mots ]; then
        sortie=$(printf '%s\n' "$sortie" | tr ' ' '\n' | sort | tr '\n' ' ')
    elif [ "$type" = grille ]; then
        [[ "$sortie" =~ ^[A-Z]{1,2}(\ [A-Z]{1,2}){15}$ ]] || code=-1
        sortie=ok
    fi
    if [ "$code" -eq "$code_attendu" ] && { [ "$type" = auteur ] || [ "$sortie" = "$attendu" ]; }; then
        echo "OK $description"
        reussites=$((reussites + 1))
    else
        printf 'ECHEC %s : code %s, sortie %s\n' "$description" "$code" "$sortie"
        echecs=$((echecs + 1))
    fi
}

verifier "construction du dictionnaire" "" 0 exact bin/dictionnary_build tests/dico_solve.txt "$lex"
if [ ! -f "$lex" ]; then exit 1; fi
verifier "mot présent" "" 0 exact bin/dictionnary_lookup "$lex" OUI
verifier "préfixe présent" "" 1 exact bin/dictionnary_lookup "$lex" OU
verifier "mot absent" "" 2 exact bin/dictionnary_lookup "$lex" ZZZ
freq="../../ressources/instructions/module_L1/moduleL1Bis_R3.03_Analyse/03_donnees/sorties/chartable.txt"
if [ ! -f "$freq" ]; then freq=tests/freq_qu.txt; fi
verifier "génération de seize cases" ok 0 grille bin/grid_build "$freq" 4 4
verifier "chemin OUI" "10 5 2" 0 exact bin/grid_path OUI 4 4 $grille
verifier "chemin TOIT absent" "" 1 exact bin/grid_path TOIT 4 4 $grille
verifier "mots trouvés dans la grille" "AIR AIRE EVE GAI OUI QUE RUE VIE VIRE VUE " 0 mots bin/solve "$lex" 3 4 4 $grille
verifier "score de deux mots" "2" 0 exact bin/score OUI VIE
for executable in dictionnary_build dictionnary_lookup grid_build grid_path solve score; do
    verifier "auteur de $executable" "" 255 auteur "bin/$executable" author
done
printf 'Réussites : %s, échecs : %s\n' "$reussites" "$echecs"
[ "$echecs" -eq 0 ]
