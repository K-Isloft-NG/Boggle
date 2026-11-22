#!/bin/bash
cd "$(dirname "$0")/.." || exit 1
tmp=$(mktemp -d "$PWD/obj/solve.XXXXXX") || exit 1
trap 'rm -rf -- "$tmp"' EXIT
export LC_ALL=C
grille="G A I R R U V E QU E O T A S M J"
dico="$tmp/dico.lex"
petit="$tmp/petit.lex"
reussites=0
echecs=0

# Compare le code de retour et le contenu de la sortie standard.
verifier() {
    local description=$1 type=$2 attendu=$3 statut=$4 code=0 correct=1
    shift 4
    "$@" > "$tmp/sortie" 2> "$tmp/erreur" || code=$?
    case "$type" in
        mots)
            if [ "$(wc -l < "$tmp/sortie")" -eq 1 ] &&
               grep -Eq '^[A-Z]+( [A-Z]+)*$' "$tmp/sortie" &&
               [ "$(tr ' ' '\n' < "$tmp/sortie" | sort)" = "$(printf '%s\n' "$attendu" | tr ' ' '\n' | sort)" ]; then
                correct=0
            fi
            ;;
        ligne) cmp -s "$tmp/sortie" "$tmp/ligne-vide" && correct=0 ;;
        silence) [ ! -s "$tmp/sortie" ] && correct=0 ;;
        auteur) grep -q '^Auteur: ' "$tmp/sortie" && correct=0 ;;
    esac
    if [ "$code" -eq "$statut" ] && [ "$correct" -eq 0 ]; then
        printf 'OK %s\n' "$description"
        reussites=$((reussites + 1))
    else
        printf 'ECHEC %s : code %s (attendu %s), sortie « %s »\n' "$description" "$code" "$statut" "$(cat "$tmp/sortie")"
        echecs=$((echecs + 1))
    fi
}

printf '\n' > "$tmp/ligne-vide"
printf '%s\n' A Q QU QUE QUER > "$tmp/petit.txt"
verifier "construction du dictionnaire de la grille" silence "" 0 bin/dictionnary_build tests/dico_solve.txt "$dico"
verifier "construction du petit dictionnaire" silence "" 0 bin/dictionnary_build "$tmp/petit.txt" "$petit"
if [ ! -f "$dico" ] || [ ! -f "$petit" ]; then exit 1; fi

verifier "ensemble des dix mots sans doublon" mots "OUI VIE VIRE QUE GAI RUE EVE AIR AIRE VUE" 0 bin/solve "$dico" 3 4 4 $grille
verifier "mots de quatre lettres ou plus" mots "VIRE AIRE" 0 bin/solve "$dico" 4 4 4 $grille
verifier "aucun mot de cinq lettres" ligne "" 0 bin/solve "$dico" 5 4 4 $grille
verifier "QUE compte trois lettres" mots "QUE" 0 bin/solve "$petit" 3 1 2 QU E
verifier "QUE disparaît au minimum quatre" ligne "" 0 bin/solve "$petit" 4 1 2 QU E
verifier "la case QU ne donne pas Q seul" mots "QU" 0 bin/solve "$petit" 1 1 1 QU
verifier "case unique au minimum un" mots "A" 0 bin/solve "$petit" 1 1 1 A
verifier "case unique au minimum deux" ligne "" 0 bin/solve "$petit" 2 1 1 A
verifier "cases Q et U séparées" mots "QUE QUER" 0 bin/solve "$petit" 3 2 2 Q U E R

verifier "aucun argument" silence "" 255 bin/solve
for minimum in 0 abc 1001; do
    verifier "minimum invalide $minimum" silence "" 255 bin/solve "$dico" "$minimum" 1 1 A
done
verifier "dimension invalide" silence "" 255 bin/solve "$dico" 3 0 1 A
verifier "une case manquante" silence "" 255 bin/solve "$dico" 3 1 2 A
verifier "une case de trop" silence "" 255 bin/solve "$dico" 3 1 1 A B
verifier "case minuscule" silence "" 255 bin/solve "$dico" 3 1 1 a
verifier "case de trois lettres" silence "" 255 bin/solve "$dico" 3 1 1 ABC
verifier "dictionnaire introuvable" silence "" 254 bin/solve "$tmp/inexistant.lex" 3 1 1 A
head -c 20 "$dico" > "$tmp/tronque.lex" || exit 1
verifier "dictionnaire tronqué" silence "" 253 bin/solve "$tmp/tronque.lex" 3 1 1 A
verifier "texte à la place du dictionnaire binaire" silence "" 253 bin/solve tests/dico_solve.txt 3 1 1 A

# Après l'en-tête de 16 octets, le fils de la deuxième cellule commence à l'octet 32.
cp "$dico" "$tmp/lien.lex" || exit 1
printf '\000' | dd of="$tmp/lien.lex" bs=1 seek=32 conv=notrunc status=none || exit 1
verifier "fils qui pointe en arrière" silence "" 253 bin/solve "$tmp/lien.lex" 3 1 1 A
cp "$dico" "$tmp/lien.lex" || exit 1
printf '\177' | dd of="$tmp/lien.lex" bs=1 seek=32 conv=notrunc status=none || exit 1
verifier "fils hors du dictionnaire" silence "" 253 bin/solve "$tmp/lien.lex" 3 1 1 A
verifier "affichage de l'auteur" auteur "" 255 bin/solve author

printf 'Réussites : %d, échecs : %d\n' "$reussites" "$echecs"
[ "$echecs" -eq 0 ]
