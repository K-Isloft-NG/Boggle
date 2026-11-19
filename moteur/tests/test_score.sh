#!/bin/bash
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
        echo "OK $description : sortie='$sortie', code=$code"
        reussites=$((reussites + 1))
    else
        echo "ECHEC $description : sortie='$sortie', code=$code"
        echecs=$((echecs + 1))
    fi
}

verifier CT2 "Auteur: Karl NATNGAM" 255 bin/score author
verifier CT3 2 0 bin/score OUI VIE
verifier CT4 0 0 bin/score
verifier CT5 0 0 bin/score ""
verifier CT6 0 0 bin/score AB
verifier CT7 1 0 bin/score ABC
verifier CT8 1 0 bin/score ABCD
verifier CT9 23 0 bin/score ABC ABCD ABCDE ABCDEF ABCDEFG ABCDEFGH
verifier CT10 11 0 bin/score ABCDEFGHIJKL
mot_long=$(printf '%085d' 0 | tr '0' 'A')
verifier CT11 11 0 bin/score "$mot_long"
verifier CT12 12 0 bin/score AB QUE ABCDEFGH
verifier CT13 4 0 bin/score author OUI
verifier CT14 2 0 bin/score OUI OUI
printf 'Réussites : %d, échecs : %d\n' "$reussites" "$echecs"
if [ "$echecs" -ne 0 ]; then
    exit 1
fi
exit 0
