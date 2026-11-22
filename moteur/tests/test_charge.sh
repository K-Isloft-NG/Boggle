#!/bin/bash
cd "$(dirname "$0")/.." || exit 1
source_mots="../../ressources/instructions/module_L1/moduleL1Bis_R3.03_Analyse/03_donnees/sorties/words.txt"
if [ ! -f "$source_mots" ]; then
    echo "IGNORE dictionnaire réel introuvable"
    exit 0
fi

tmp=$(mktemp -d "$PWD/obj/charge.XXXXXX") || exit 1
trap 'rm -rf -- "$tmp"' EXIT
lex="$tmp/words.lex"
reussites=0
echecs=0
export LC_ALL=C

# Compare le statut obtenu au statut calculé depuis le fichier texte.
verifier() {
    local mot=$1 attendu=$2 code=0
    bin/dictionnary_lookup "$lex" "$mot" > "$tmp/sortie" || code=$?
    if [ "$code" -eq "$attendu" ] && [ ! -s "$tmp/sortie" ]; then
        reussites=$((reussites + 1))
    else
        printf 'ECHEC %s : code %s, attendu %s\n' "$mot" "$code" "$attendu"
        echecs=$((echecs + 1))
    fi
}

TIMEFORMAT='Temps de construction : %3R s'
if [ -x /usr/bin/time ]; then
    time /usr/bin/time -v -o "$tmp/mesure" bin/dictionnary_build "$source_mots" "$lex" || exit 1
    grep 'Maximum resident set size' "$tmp/mesure" || exit 1
else
    time bin/dictionnary_build "$source_mots" "$lex" || exit 1
    echo "Mémoire maximale : /usr/bin/time indisponible"
fi

read -r entete < <(od -An -t d4 -j 0 -N 4 "$lex")
read -r mots < <(od -An -t d4 -j 4 -N 4 "$lex")
read -r cellules < <(od -An -t d4 -j 8 -N 4 "$lex")
read -r cellule < <(od -An -t d4 -j 12 -N 4 "$lex")
octets=$(wc -c < "$lex")
printf 'Taille en-tête : %s ; mots : %s ; cellules : %s ; taille cellule : %s ; fichier : %s octets\n' \
    "$entete" "$mots" "$cellules" "$cellule" "$octets"
if [ "$entete" != 16 ] || [ "$mots" != 1551367 ] || [ "$cellules" != 3067074 ] ||
   [ "$cellule" != 12 ] || [ "$octets" != 36804904 ]; then
    echo "ECHEC dimensions du dictionnaire"
    echecs=$((echecs + 1))
fi

TIMEFORMAT='Temps des recherches et des comparaisons : %3R s'
nombre=0
time while IFS= read -r mot; do
    verifier "$mot" 0
    prefixe=${mot%?}
    if grep -Fxq -- "$prefixe" "$source_mots"; then
        verifier "$prefixe" 0
    else
        verifier "$prefixe" 1
    fi
    variante=$mot"Q"
    if grep -Fxq -- "$variante" "$source_mots"; then
        verifier "$variante" 0
    elif [ "$(grep -c "^$variante" "$source_mots")" -gt 0 ]; then
        verifier "$variante" 1
    else
        verifier "$variante" 2
    fi
    nombre=$((nombre + 1))
done < <(awk 'NR % 5000 == 0 { print; if (++pris == 300) exit }' "$source_mots")
if [ "$nombre" -ne 300 ]; then
    echo "ECHEC taille de l'échantillon"
    echecs=$((echecs + 1))
fi
printf 'Mots échantillonnés : %s, réussites : %s, échecs : %s\n' "$nombre" "$reussites" "$echecs"
[ "$echecs" -eq 0 ]
