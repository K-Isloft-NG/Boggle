#!/bin/bash
cd "$(dirname "$0")/.." || exit 1
source_mots="../../ressources/instructions/module_L1/moduleL1Bis_R3.03_Analyse/03_donnees/sorties/words.txt"
if [ ! -f "$source_mots" ]; then echo "IGNORE dictionnaire réel introuvable"; exit 0; fi
tmp=$(mktemp -d "$PWD/obj/charge.XXXXXX") || exit 1
trap 'rm -rf -- "$tmp"' EXIT
export LC_ALL=C
lex="$tmp/words.lex"
reussites=0
echecs=0

# Compare la recherche au statut calculé depuis le fichier texte.
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

# Une lecture mémorise les mots, puis détermine les statuts des variantes.
awk '
    { dictionnaire[$0] = 1 }
    NR % 5000 == 0 && nombre < 300 { echantillon[++nombre] = $0 }
    END {
        for (i = 1; i <= nombre; i++) {
            variante = echantillon[i] "Q"
            variantes[variante] = 1
            longueurs[length(variante)] = 1
        }
        for (mot in dictionnaire) {
            for (longueur in longueurs) {
                if (length(mot) >= longueur) {
                    debut = substr(mot, 1, longueur)
                    if (debut in variantes) prefixe[debut] = 1
                }
            }
        }
        for (i = 1; i <= nombre; i++) {
            mot = echantillon[i]
            court = substr(mot, 1, length(mot) - 1)
            variante = mot "Q"
            statut_court = (court in dictionnaire) ? 0 : 1
            statut_variante = (variante in dictionnaire) ? 0 : ((variante in prefixe) ? 1 : 2)
            print mot, court, statut_court, variante, statut_variante
        }
    }
' "$source_mots" > "$tmp/attendus" || exit 1
nombre=0
while read -r mot court statut_court variante statut_variante; do
    verifier "$mot" 0
    verifier "$court" "$statut_court"
    verifier "$variante" "$statut_variante"
    nombre=$((nombre + 1))
done < "$tmp/attendus"
if [ "$nombre" -ne 300 ]; then echo "ECHEC taille de l'échantillon"; echecs=$((echecs + 1)); fi
printf 'Mots échantillonnés : %s, réussites : %s, échecs : %s\n' "$nombre" "$reussites" "$echecs"
[ "$echecs" -eq 0 ]
