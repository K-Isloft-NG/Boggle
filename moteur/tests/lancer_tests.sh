#!/bin/bash
cd "$(dirname "$0")/.." || exit 1
if ! make; then echo "ECHEC compilation"; exit 1; fi
tmp=$(mktemp -d "$PWD/obj/tests.XXXXXX") || exit 1
trap 'rm -rf -- "$tmp"' EXIT
erreur=0
for script in tests/test_*.sh; do
    code=0
    bash "$script" > "$tmp/resultat" 2>&1 || code=$?
    bilan=$(grep -E 'réussites :|Réussites :' "$tmp/resultat" | tail -1)
    if [ -z "$bilan" ] && grep -q '^IGNORE' "$tmp/resultat" && [ "$code" -eq 0 ]; then
        bilan="ignoré"
    elif [ -z "$bilan" ]; then
        bilan="bilan absent"
        code=1
    fi
    printf '%s : %s\n' "$script" "$bilan"
    if [ "$code" -ne 0 ]; then
        cat "$tmp/resultat"
        erreur=1
    fi
done
exit "$erreur"
