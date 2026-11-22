# Moteur de jeu Boggle en C

Ce dossier contient le moteur du jeu : six exécutables en ligne de commande, appelés par le serveur web. Ils ne lisent rien sur l'entrée standard, tous leurs paramètres sont donnés en arguments.

## Compilation

Prérequis : `gcc` et `make` sous Linux (ou WSL).

```sh
make          # compile les six exécutables dans bin/
make clean    # supprime les fichiers objets (obj/)
```

La compilation se fait avec `-Wall -Wextra -Werror` : elle ne produit aucun avertissement.

## Organisation

| Dossier | Contenu |
|---|---|
| `apps/` | un fichier par exécutable (lecture des arguments, codes de retour) |
| `src/`, `include/` | bibliothèques partagées : `trie` (arbre fils/frère), `dictionnaire`, `lex` (format binaire), `frequences`, `grille`, `chemin`, `solveur`, `boggle` (score), `author` |
| `tests/` | scripts de test et petits fichiers de données |

## Les six exécutables

Dans les exemples, la grille 4 x 4 est donnée ligne par ligne. La case d'indice `i` est à la ligne `i / largeur` et à la colonne `i % largeur`. Une case contient une ou deux lettres majuscules : `QU` est une seule case, passée en un seul argument.

### dictionnary_build

```sh
bin/dictionnary_build dico.txt dico.lex
```

Construit l'arbre lexicographique des mots de `dico.txt` (un mot en majuscules A à Z par ligne, suivi éventuellement d'un espace et d'un commentaire) et l'enregistre dans le fichier binaire `dico.lex`. Aucune sortie.

Format de `dico.lex` : un en-tête de quatre `int` (taille de l'en-tête, nombre de mots, nombre de cellules, taille d'une cellule), puis les cellules `{char elem; char finDeMot; int fils; int frere;}` numérotées en ordre préfixe, `-1` indiquant l'absence de fils ou de frère. Pour le dictionnaire de 1 551 367 mots : 3 067 074 cellules, 36,8 Mo.

### dictionnary_lookup

```sh
bin/dictionnary_lookup dico.lex BONJOUR
```

Aucune sortie. Code de retour : `0` si le mot est présent, `1` si c'est seulement le début d'un mot présent (par exemple `BONJ`), `2` sinon. Le fichier n'est pas chargé en entier : la recherche lit uniquement les cellules nécessaires.

### grid_build

```sh
bin/grid_build frequences.txt 4 4
G A I R R U V E QU E O T A S M J
```

Tire une grille aléatoire de 4 lignes et 4 colonnes et l'affiche sur une ligne. Chaque ligne de `frequences.txt` contient un symbole (une ou deux lettres majuscules) et son poids (entier de 0 à 999 999 999), par exemple `A 36` ou `QU 26`. La somme des poids doit être positive.

Les deux nombres sont la hauteur (nombre de lignes) puis la largeur (nombre de colonnes). Dimensions acceptées, pour tous les exécutables qui prennent une grille : de 1 à 1000. Cette borne évite tout dépassement d'entier à la lecture et garde une mémoire raisonnable (un million de cases au plus), bien au-delà des grilles de jeu (4 x 4 ou 5 x 5).

### grid_path

```sh
bin/grid_path OUI 4 4 G A I R R U V E QU E O T A S M J
10 5 2
```

Paramètres : le mot, la hauteur et la largeur de la grille, puis les cases ligne par ligne. Cherche le mot dans la grille, chaque case étant utilisée au plus une fois et deux cases successives étant voisines (8 directions). Si le mot est trouvé, affiche les indices des cases du chemin et renvoie `0` ; sinon, n'affiche rien et renvoie `1`.

### solve

```sh
bin/solve dico.lex 3 4 4 G A I R R U V E QU E O T A S M J
OUI VIE VIRE ...
```

Paramètres : le dictionnaire binaire, la longueur minimale des mots (ici `3`), la hauteur et la largeur de la grille (ici `4 4`), puis les cases ligne par ligne.

Affiche sur une ligne tous les mots du dictionnaire présents dans la grille et d'au moins 3 lettres (`QU` compte pour deux lettres), chacun une seule fois. La recherche parcourt la grille et l'arbre en même temps et abandonne un chemin dès que son préfixe n'existe plus dans le dictionnaire. Pour le dictionnaire complet : environ 0,02 s et 38 Mo de mémoire pour une grille 4 x 4.

### score

```sh
bin/score OUI VIE
2
```

Affiche le score total des mots, supposés valides. Barème par longueur (en lettres, `QU` compte pour deux) : 3 ou 4 lettres : 1 point, 5 : 2, 6 : 3, 7 : 5, 8 et plus : 11, moins de 3 : 0.

### Auteur

Chaque exécutable lancé avec le seul argument `author` affiche `Auteur: ...` et renvoie `255`. Ce mot est réservé à l'identification et n'est jamais utilisé en jeu.

## Codes de retour

| Code | Signification |
|---|---|
| `0` | succès (mot présent pour `dictionnary_lookup`, chemin trouvé pour `grid_path`) |
| `1`, `2` | réponses de `dictionnary_lookup` (préfixe, absent) et de `grid_path` (absent) |
| `253` | contenu inutilisable (fichier mal formé, `.lex` corrompu, mémoire insuffisante) |
| `254` | fichier introuvable ou illisible |
| `255` | paramètres manquants ou invalides, ou argument `author` |

En cas d'erreur, rien n'est écrit sur la sortie standard ; la plupart des exécutables écrivent un message sur la sortie d'erreur (`dictionnary_lookup` reste muet, seul son code compte).

## Tests

```sh
bash tests/lancer_tests.sh
```

Compile le moteur, lance tous les scripts `tests/test_*.sh` et affiche une ligne par script. Le code de retour vaut `1` si un script échoue. Le script `test_charge.sh` utilise le dictionnaire complet `words.txt` s'il est présent dans le dossier `ressources` du projet, sinon il est ignoré.
