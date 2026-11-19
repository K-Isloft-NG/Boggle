#!/bin/bash
# Tests de l'exécutable score. À lancer après make.
cd "$(dirname "$0")/.." || exit 1
reussites=0
echecs=0

# Compare la sortie et le code de retour d'une commande.
verifier() {
    description=$1
    attendu=$2
    code_attendu=$3
    shift 3
    sortie=$("$@")
    code=$?
    if [ "$sortie" = "$attendu" ] && [ "$code" -eq "$code_attendu" ]; then
        echo "OK     $description"
        reussites=$((reussites + 1))
    else
        echo "ECHEC  $description : sortie='$sortie', code=$code"
        echecs=$((echecs + 1))
    fi
}

# Le paramètre author affiche une ligne "Auteur: ..." et renvoie 255.
sortie=$(bin/score author)
code=$?
case "$sortie" in
    "Auteur: "*)
        if [ "$code" -eq 255 ]; then
            echo "OK     paramètre author"
            reussites=$((reussites + 1))
        else
            echo "ECHEC  paramètre author : code=$code"
            echecs=$((echecs + 1))
        fi
        ;;
    *)
        echo "ECHEC  paramètre author : sortie='$sortie'"
        echecs=$((echecs + 1))
        ;;
esac

verifier "deux mots de 3 lettres" 2 0 bin/score OUI VIE
verifier "aucun mot" 0 0 bin/score
verifier "mot vide" 0 0 bin/score ""
verifier "mot de 2 lettres" 0 0 bin/score AB
verifier "mot de 3 lettres" 1 0 bin/score ABC
verifier "mot de 4 lettres" 1 0 bin/score ABCD
verifier "un mot de chaque longueur de 3 à 8" 23 0 bin/score ABC ABCD ABCDE ABCDEF ABCDEFG ABCDEFGH
verifier "mot de 12 lettres" 11 0 bin/score ABCDEFGHIJKL
mot_long=$(printf '%085d' 0 | tr '0' 'A')
verifier "mot de 85 lettres" 11 0 bin/score "$mot_long"
verifier "QU compte pour 2 lettres" 12 0 bin/score AB QUE ABCDEFGH
verifier "author avec d'autres arguments est un mot" 4 0 bin/score author OUI
verifier "le même mot deux fois est compté deux fois" 2 0 bin/score OUI OUI

printf 'Réussites : %d, échecs : %d\n' "$reussites" "$echecs"
if [ "$echecs" -ne 0 ]; then
    exit 1
fi
exit 0
