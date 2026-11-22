#!/bin/bash
cd "$(dirname "$0")/.." || exit 1
tmp=$(mktemp -d "$PWD/obj/dictionnaire.XXXXXX") || exit 1
trap 'rm -rf -- "$tmp"' EXIT
petit="$tmp/p.lex"
trie="$tmp/autre.lex"
reussites=0
echecs=0

# Compte et affiche le résultat d'un contrôle.
bilan_cas() {
    if [ "$2" -eq 0 ]; then
        printf 'OK %s\n' "$1"
        reussites=$((reussites + 1))
    else
        printf 'ECHEC %s\n' "$1"
        echecs=$((echecs + 1))
    fi
}

# Compare le code et vérifie la sortie silencieuse ou le préfixe d'auteur.
verifier() {
    local description=$1 code_attendu=$2 sortie_attendue=$3
    shift 3
    local code=0
    "$@" > "$tmp/sortie" 2> "$tmp/erreur" || code=$?
    if [ "$code" -eq "$code_attendu" ]; then
        if { [ "$sortie_attendue" = vide ] && [ ! -s "$tmp/sortie" ]; } ||
           { [ "$sortie_attendue" = auteur ] && grep -q '^Auteur: ' "$tmp/sortie"; }; then
            bilan_cas "$description" 0
            return
        fi
    fi
    printf '  Code obtenu : %s, attendu : %s\n' "$code" "$code_attendu"
    bilan_cas "$description" 1
}

# Lit les quatre champs de l'en-tête et la taille du fichier.
verifier_format() {
    local fichier=$1 mots=$2 cellules=$3 taille=$4
    local entete nombreMots nombreCellules cellule octets
    read -r entete < <(od -An -t d4 -j 0 -N 4 "$fichier")
    read -r nombreMots < <(od -An -t d4 -j 4 -N 4 "$fichier")
    read -r nombreCellules < <(od -An -t d4 -j 8 -N 4 "$fichier")
    read -r cellule < <(od -An -t d4 -j 12 -N 4 "$fichier")
    octets=$(wc -c < "$fichier")
    [ "$entete" = 16 ] && [ "$nombreMots" = "$mots" ] &&
        [ "$nombreCellules" = "$cellules" ] && [ "$cellule" = 12 ] &&
        [ "$octets" -eq "$taille" ]
    bilan_cas "format : $mots mots, $cellules cellules, $taille octets" "$?"
}

verifier "construction du petit dictionnaire" 0 vide bin/dictionnary_build tests/dico_petit.txt "$petit"
verifier_format "$petit" 6 18 232
verifier "seconde construction" 0 vide bin/dictionnary_build tests/dico_petit.txt "$tmp/copier.lex"
cmp -s "$petit" "$tmp/copier.lex"
bilan_cas "constructions identiques octet pour octet" "$?"

printf 'AA\r\nBON\r\n' > "$tmp/crlf.txt"
verifier "construction depuis des lignes CRLF" 0 vide bin/dictionnary_build "$tmp/crlf.txt" "$tmp/crlf.lex"
verifier_format "$tmp/crlf.lex" 2 5 76

printf '%s\n' BONNE BON BONJOUR QUE CAFE AA > "$tmp/autre.txt"
verifier "construction dans un autre ordre" 0 vide bin/dictionnary_build "$tmp/autre.txt" "$trie"
for fichier in "$petit" "$trie"; do
    nom=$(basename "$fichier")
    for mot in BONJOUR BON AA QUE CAFE BONNE; do
        verifier "$nom, mot présent : $mot" 0 vide bin/dictionnary_lookup "$fichier" "$mot"
    done
    for mot in BONJ BONN B A Q CA; do
        verifier "$nom, préfixe : $mot" 1 vide bin/dictionnary_lookup "$fichier" "$mot"
    done
    for mot in BONNES ZOO LIGNE IGNOREE ZZ bonjour; do
        verifier "$nom, mot absent : $mot" 2 vide bin/dictionnary_lookup "$fichier" "$mot"
    done
done

verifier "recherche avec mot vide" 255 vide bin/dictionnary_lookup "$petit" ""
verifier "recherche sans mot" 255 vide bin/dictionnary_lookup "$petit"
verifier "recherche avec deux mots" 255 vide bin/dictionnary_lookup "$petit" BON AA
verifier "construction sans argument" 255 vide bin/dictionnary_build
verifier "construction avec un seul argument" 255 vide bin/dictionnary_build tests/dico_petit.txt
verifier "construction avec trois arguments" 255 vide bin/dictionnary_build tests/dico_petit.txt "$tmp/x.lex" surplus
verifier "dictionnaire introuvable" 254 vide bin/dictionnary_build "$tmp/inexistant.txt" "$tmp/x.lex"
test ! -e "$tmp/x.lex"
bilan_cas "aucune sortie créée si l'entrée est introuvable" "$?"
verifier "dossier de sortie introuvable" 254 vide bin/dictionnary_build tests/dico_petit.txt "$tmp/inexistant/x.lex"

printf '' > "$tmp/vide.txt"
printf 'zoo\n' > "$tmp/invalide.txt"
for nom in vide invalide; do
    verifier "dictionnaire $nom" 253 vide bin/dictionnary_build "$tmp/$nom.txt" "$tmp/$nom.lex"
    test ! -e "$tmp/$nom.lex"
    bilan_cas "aucune sortie créée pour le dictionnaire $nom" "$?"
done

head -c 10 "$petit" > "$tmp/entete-courte.lex"
head -c 100 "$petit" > "$tmp/cellules-courtes.lex"
verifier "fichier lex introuvable" 254 vide bin/dictionnary_lookup "$tmp/inexistant.lex" BON
verifier "fichier lex vide" 253 vide bin/dictionnary_lookup "$tmp/vide.txt" BON
verifier "en-tête tronqué" 253 vide bin/dictionnary_lookup "$tmp/entete-courte.lex" BON
verifier "cellules tronquées" 253 vide bin/dictionnary_lookup "$tmp/cellules-courtes.lex" BON
verifier "texte à la place du fichier lex" 253 vide bin/dictionnary_lookup tests/dico_petit.txt BON
verifier "auteur de la construction" 255 auteur bin/dictionnary_build author
verifier "auteur de la recherche" 255 auteur bin/dictionnary_lookup author

printf 'Réussites : %d, échecs : %d\n' "$reussites" "$echecs"
[ "$echecs" -eq 0 ]
