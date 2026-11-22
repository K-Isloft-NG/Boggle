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
    sortie=$("$@" 2>/dev/null)
    code=$?
    if [ "$sortie" = "$attendu" ] && [ "$code" -eq "$code_attendu" ]; then
        echo "OK $description"
        reussites=$((reussites + 1))
    else
        echo "ECHEC $description : sortie='$sortie', code=$code"
        echecs=$((echecs + 1))
    fi
}

# Vérifie la forme et les effectifs d'une grille réelle.
verifier_grille() {
    description=$1
    hauteur=$2
    largeur=$3
    sortie=$(bin/grid_build "$fichier_reel" "$hauteur" "$largeur" 2>/dev/null)
    code=$?
    if [ "$code" -eq 0 ] && printf '%s\n' "$sortie" | awk -v fichier="$fichier_reel" -v nombre="$((hauteur * largeur))" '
        BEGIN { while ((getline ligne < fichier) > 0) { split(ligne, t); permis[t[1]] = 1 } close(fichier) }
        NR != 1 || NF != nombre || $0 ~ /^ | $|  / { erreur = 1 }
        { for (i = 1; i <= NF; i++) { if (!($i in permis)) erreur = 1; effectif[$i]++ } }
        END { if (NR != 1) erreur = 1; if (nombre == 10000 && (effectif["E"] < 1500 || effectif["E"] > 2050 || effectif["K"] >= 100 || effectif["W"] >= 100)) erreur = 1; exit erreur }
    '; then
        echo "OK $description"
        reussites=$((reussites + 1))
    else
        echo "ECHEC $description : code=$code"
        echecs=$((echecs + 1))
    fi
}

sortie=$(bin/grid_build author 2>/dev/null)
code=$?
case "$sortie" in
    'Auteur: '*) if [ "$code" -eq 255 ]; then echo "OK auteur"; reussites=$((reussites + 1)); else echo "ECHEC auteur"; echecs=$((echecs + 1)); fi ;;
    *) echo "ECHEC auteur"; echecs=$((echecs + 1)) ;;
esac
verifier "grille QU 2 par 3" "QU QU QU QU QU QU" 0 bin/grid_build tests/freq_qu.txt 2 3
verifier "symbole de poids nul" "B B B B" 0 bin/grid_build tests/freq_poids_nul.txt 1 4
verifier "grille à une case" "QU" 0 bin/grid_build tests/freq_qu.txt 1 1
verifier "arguments absents" "" 255 bin/grid_build
verifier "largeur absente" "" 255 bin/grid_build tests/freq_qu.txt 4
for hauteur in 0 abc 4x 1001; do
    verifier "hauteur invalide $hauteur" "" 255 bin/grid_build tests/freq_qu.txt "$hauteur" 4
done
verifier "fichier introuvable" "" 254 bin/grid_build tests/inexistant.txt 4 4
verifier "fichier vide" "" 253 bin/grid_build tests/freq_vide.txt 4 4
verifier "poids négatif" "" 253 bin/grid_build tests/freq_negatif.txt 4 4
verifier "total nul" "" 253 bin/grid_build tests/freq_total_nul.txt 4 4
verifier "symbole trop long" "" 253 bin/grid_build tests/freq_symbole_long.txt 4 4
fichier_reel="../../ressources/instructions/module_L1/moduleL1Bis_R3.03_Analyse/03_donnees/sorties/chartable.txt"
if [ -f "$fichier_reel" ]; then
    verifier_grille "grille réelle 4 par 4" 4 4
    verifier_grille "grille réelle 7 par 9" 7 9
    verifier_grille "répartition sur 10000 cases" 100 100
else
    echo "IGNORE grille réelle 4 par 4"
    echo "IGNORE grille réelle 7 par 9"
    echo "IGNORE répartition sur 10000 cases"
fi
printf 'Réussites : %d, échecs : %d\n' "$reussites" "$echecs"
if [ "$echecs" -ne 0 ]; then exit 1; fi
exit 0
