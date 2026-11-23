<?php
declare(strict_types=1);

namespace Boogle\Service;

use InvalidArgumentException;

final class PartieService
{
    public function __construct(private readonly Moteur $moteur)
    {
    }

    public function demarrer(): void
    {
        $hauteur = ReglesJeu::HAUTEUR_GRILLE;
        $largeur = ReglesJeu::LARGEUR_GRILLE;
        $_SESSION['partie'] = [
            'grille'       => $this->moteur->creerGrille($hauteur, $largeur),
            'hauteur'      => $hauteur,
            'largeur'      => $largeur,
            'debut'        => time(),
            'duree_s'      => ReglesJeu::DUREE_PARTIE_S,
            'propositions' => [],
            'terminee'     => false,
            'score'        => 0,
            'solutions'    => [],
        ];
    }

    public function partieEnCours(): ?array
    {
        $partie = $_SESSION['partie'] ?? null;
        if ($partie !== null && !$partie['terminee'] && $this->secondesRestantes() === 0) {
            $this->terminer();
        }

        return $_SESSION['partie'] ?? null;
    }

    public function secondesRestantes(): int
    {
        $partie = $_SESSION['partie'] ?? null;
        if ($partie === null || $partie['terminee']) {
            return 0;
        }

        return max(0, $partie['debut'] + $partie['duree_s'] - time());
    }

    public function proposer(string $saisie): array
    {
        $partie = $_SESSION['partie'] ?? null;
        if ($partie === null || $this->secondesRestantes() === 0) {
            throw new InvalidArgumentException('La partie est terminée.');
        }
        $tMs = (int) floor((microtime(true) - $partie['debut']) * 1000);
        if (count($partie['propositions']) >= ReglesJeu::PROPOSITIONS_MAX) {
            throw new InvalidArgumentException('Vous avez atteint le nombre maximal de propositions.');
        }

        $mot = ReglesJeu::normaliserMot(trim($saisie));
        if (strlen($mot) > ReglesJeu::LONGUEUR_MAX_SAISIE || preg_match('/^[A-Z]+$/D', $mot) !== 1) {
            throw new InvalidArgumentException('Saisissez un mot composé uniquement de lettres.');
        }

        $chemin = null;
        $motif = null;
        $points = 0;

        if (ReglesJeu::verifierLongueur($mot) !== null) {
            $motif = 'trop_court';
        } elseif (in_array($mot, array_column($partie['propositions'], 'mot'), true)) {
            $motif = 'doublon';
        } else {
            $chemin = $this->moteur->chemin($mot, $partie['grille'], $partie['hauteur'], $partie['largeur']);
            if ($chemin === null) {
                $motif = 'absent_grille';
            } elseif (!$this->moteur->estDansDictionnaire($mot)) {
                $motif = 'absent_dictionnaire';
            } else {
                $points = $this->moteur->score([$mot]);
            }
        }

        $proposition = [
            'mot'         => $mot,
            'chemin'      => $chemin,
            't_ms'        => max(0, $tMs),
            'valide'      => $motif === null,
            'points'      => $points,
            'motif_rejet' => $motif,
        ];
        $_SESSION['partie']['propositions'][] = $proposition;
        $_SESSION['partie']['score'] += $points;

        return $proposition;
    }

    public function terminer(): void
    {
        $partie = $_SESSION['partie'] ?? null;
        if ($partie === null || $partie['terminee']) {
            return;
        }

        $_SESSION['partie']['terminee'] = true;
        $_SESSION['partie']['solutions'] = $this->moteur->solutions(
            $partie['grille'], $partie['hauteur'], $partie['largeur'], ReglesJeu::LONGUEUR_MINIMALE_MOT
        );
    }

    public function abandonner(): void
    {
        unset($_SESSION['partie']);
    }
}
