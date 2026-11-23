<?php
declare(strict_types=1);

namespace Boogle\Service;

final class Moteur
{
    private const EXECUTABLES = ['grid_build', 'grid_path', 'dictionnary_lookup', 'score', 'solve'];
    private const DUREE_MAX_S = 5;

    public function __construct(
        private readonly string $dossierBin,
        private readonly string $dictionnaire,
        private readonly string $frequences
    ) {
    }

    /** @return list<string> */
    public function creerGrille(int $hauteur, int $largeur): array
    {
        $resultat = $this->executer('grid_build', [$this->frequences, (string) $hauteur, (string) $largeur]);
        $this->verifierCode('grid_build', $resultat['code'], [0]);
        $cases = $this->lireElements($resultat['sortie']);
        $this->verifierGrille($cases, $hauteur, $largeur);

        return $cases;
    }

    /** @param list<string> $grille @return list<int>|null */
    public function chemin(string $mot, array $grille, int $hauteur, int $largeur): ?array
    {
        $this->verifierMot($mot);
        $this->verifierGrille($grille, $hauteur, $largeur);
        $resultat = $this->executer('grid_path', [$mot, (string) $hauteur, (string) $largeur, ...$grille]);
        $this->verifierCode('grid_path', $resultat['code'], [0, 1]);
        if ($resultat['code'] === 1) {
            return null;
        }

        $indices = $this->lireElements($resultat['sortie']);
        if ($indices === []) {
            throw new MoteurException('Chemin vide.');
        }
        foreach ($indices as $indice) {
            if (preg_match('/^[0-9]+$/D', $indice) !== 1 || (int) $indice >= count($grille)) {
                throw new MoteurException('Indice de grille invalide.');
            }
        }

        return array_map('intval', $indices);
    }

    public function estDansDictionnaire(string $mot): bool
    {
        $this->verifierMot($mot);
        $resultat = $this->executer('dictionnary_lookup', [$this->dictionnaire, $mot]);
        $this->verifierCode('dictionnary_lookup', $resultat['code'], [0, 1, 2]);

        return $resultat['code'] === 0;
    }

    /** @param list<string> $mots */
    public function score(array $mots): int
    {
        if ($mots === []) {
            return 0;
        }
        foreach ($mots as $mot) {
            $this->verifierMot($mot);
        }
        $resultat = $this->executer('score', $mots);
        $this->verifierCode('score', $resultat['code'], [0]);
        if (preg_match('/^[0-9]+$/D', $resultat['sortie']) !== 1) {
            throw new MoteurException('Score invalide.');
        }

        return (int) $resultat['sortie'];
    }

    /**
     * @param list<string> $grille
     * @return list<string>
     */
    public function solutions(array $grille, int $hauteur, int $largeur, int $longueurMin): array
    {
        $this->verifierGrille($grille, $hauteur, $largeur);
        $resultat = $this->executer('solve', [
            $this->dictionnaire, (string) $longueurMin, (string) $hauteur, (string) $largeur, ...$grille,
        ]);
        $this->verifierCode('solve', $resultat['code'], [0]);

        return $this->lireElements($resultat['sortie']);
    }

    private function verifierMot(mixed $mot): void
    {
        if (!is_string($mot) || strlen($mot) > ReglesJeu::LONGUEUR_MAX_SAISIE || preg_match('/^[A-Z]+$/D', $mot) !== 1) {
            throw new MoteurException('Mot invalide pour le moteur.');
        }
    }

    private function verifierGrille(array $grille, int $hauteur, int $largeur): void
    {
        if (!array_is_list($grille) || count($grille) !== $hauteur * $largeur) {
            throw new MoteurException('Nombre de cases invalide.');
        }
        foreach ($grille as $case) {
            if (!is_string($case) || preg_match('/^[A-Z]{1,2}$/D', $case) !== 1) {
                throw new MoteurException('Case invalide.');
            }
        }
    }

    private function verifierCode(string $programme, int $code, array $codesAttendus): void
    {
        if (!in_array($code, $codesAttendus, true)) {
            throw new MoteurException(sprintf('%s a renvoyé un code inattendu (%d).', $programme, $code));
        }
    }

    /** @return list<string> */
    private function lireElements(string $sortie): array
    {
        return $sortie === '' ? [] : explode(' ', $sortie);
    }

    /** @return array{code: int, sortie: string} */
    private function executer(string $programme, array $arguments): array
    {
        if (!in_array($programme, self::EXECUTABLES, true)) {
            throw new MoteurException('Exécutable non autorisé.');
        }
        foreach ($arguments as $argument) {
            if (!is_string($argument) || $argument === '' || str_contains($argument, "\0")) {
                throw new MoteurException('Argument invalide pour le moteur.');
            }
        }

        $chemin = $this->dossierBin . '/' . $programme;
        $descripteurs = [0 => ['pipe', 'r'], 1 => ['pipe', 'w'], 2 => ['pipe', 'w']];
        $processus = @proc_open([$chemin, ...$arguments], $descripteurs, $tubes);
        if ($processus === false) {
            throw new MoteurException('Impossible de lancer ' . $programme . '.');
        }

        fclose($tubes[0]);
        stream_set_blocking($tubes[1], false);
        stream_set_blocking($tubes[2], false);
        $echeance = hrtime(true) + self::DUREE_MAX_S * 1_000_000_000;
        $sortie = '';
        $erreur = '';

        try {
            while (true) {
                $statut = proc_get_status($processus);
                if (!$statut['running']) {
                    // Certaines versions de PHP ne donnent le code qu'au premier constat de l'arrêt.
                    $code = $statut['exitcode'];
                    $sortie .= stream_get_contents($tubes[1]);
                    $erreur = substr($erreur . stream_get_contents($tubes[2]), 0, 300);
                    break;
                }
                if (hrtime(true) >= $echeance) {
                    proc_terminate($processus, 9);
                    throw new MoteurException('Délai dépassé pour ' . $programme . '.');
                }
                $sortie .= stream_get_contents($tubes[1], 8192);
                $erreur = substr($erreur . stream_get_contents($tubes[2], 8192), 0, 300);
                usleep(1000);
            }
        } finally {
            fclose($tubes[1]);
            fclose($tubes[2]);
            proc_close($processus);
        }

        if (in_array($code, [253, 254, 255], true)) {
            // Le moteur écrit des diagnostics fixes ou des chemins, sans les mots proposés.
            error_log(sprintf('%s (code %d) : %s', $programme, $code, trim(strtr($erreur, "\r\n", '  '))));
            throw new MoteurException('Erreur de ' . $programme . '.');
        }

        return ['code' => $code, 'sortie' => trim($sortie)];
    }
}
