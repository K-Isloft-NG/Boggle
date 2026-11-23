<?php
declare(strict_types=1);

namespace Boogle\Service;

use Boogle\Core\Session;
use InvalidArgumentException;
use JsonException;
use PDOException;
use RuntimeException;

final class PartieService
{
    private bool $abandonNonEnregistre = false;

    public function __construct(
        private readonly Moteur $moteur,
        private readonly EnregistrementPartieService $enregistrement
    ) {
    }

    public function demarrer(): void
    {
        $hauteur = ReglesJeu::HAUTEUR_GRILLE;
        $largeur = ReglesJeu::LARGEUR_GRILLE;
        $_SESSION['partie'] = [
            'grille'       => $this->moteur->creerGrille($hauteur, $largeur),
            'hauteur'      => $hauteur,
            'largeur'      => $largeur,
            'debut'        => microtime(true),
            'duree_s'      => ReglesJeu::DUREE_PARTIE_S,
            'propositions' => [],
            'terminee'     => false,
            'score'        => 0,
            'solutions'    => [],
            'id_partie'    => null,
            'enregistrement' => 'en_attente',
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

        return max(0, (int) ceil($partie['debut'] + $partie['duree_s'] - microtime(true)));
    }

    public function proposer(string $saisie): array
    {
        $partie = $_SESSION['partie'] ?? null;
        $instant = microtime(true);
        if ($partie === null || $partie['terminee'] || $instant >= $partie['debut'] + $partie['duree_s']) {
            throw new InvalidArgumentException('La partie est terminée.');
        }
        $tMs = (int) floor(($instant - $partie['debut']) * 1000);
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
        $this->enregistrerSiNecessaire(ReglesJeu::STATUT_TERMINEE);
    }

    public function abandonner(): void
    {
        $this->abandonNonEnregistre = false;
        $partie = $_SESSION['partie'] ?? null;
        if ($partie === null || $partie['terminee']) {
            return;
        }

        $propositions = $partie['propositions'];
        $derniere = $propositions[array_key_last($propositions)] ?? null;
        $ecoule = (int) floor((microtime(true) - $partie['debut']) * 1000);
        $abandonTMs = max($derniere['t_ms'] ?? 0, min($ecoule, $partie['duree_s'] * 1000));
        $this->enregistrerSiNecessaire(ReglesJeu::STATUT_ANNULEE, $abandonTMs);
        $this->abandonNonEnregistre = $_SESSION['partie']['enregistrement'] === 'echec';
        unset($_SESSION['partie']);
    }

    public function abandonNonEnregistre(): bool
    {
        return $this->abandonNonEnregistre;
    }

    private function enregistrerSiNecessaire(string $statut, ?int $abandonTMs = null): void
    {
        $partie = $_SESSION['partie'];
        if (isset($partie['id_partie']) || ($partie['enregistrement'] ?? 'en_attente') !== 'en_attente') {
            return;
        }

        $idUtilisateur = Session::utilisateurId();
        if ($idUtilisateur === null) {
            error_log('Enregistrement de partie : identifiant utilisateur absent.');
            $_SESSION['partie']['enregistrement'] = 'echec';
            return;
        }

        try {
            $_SESSION['partie']['id_partie'] = $this->enregistrement->enregistrer(
                $partie, $idUtilisateur, $statut, $abandonTMs
            );
            $_SESSION['partie']['enregistrement'] = 'ok';
        } catch (RuntimeException | JsonException $erreur) {
            // Un diagnostic SQL peut contenir la valeur d'un paramètre.
            $message = match (true) {
                $erreur instanceof PDOException => 'Erreur PostgreSQL (code ' . $erreur->getCode() . ').',
                $erreur instanceof JsonException => 'Données de partie impossibles à encoder (code ' . $erreur->getCode() . ').',
                in_array($erreur->getMessage(), [
                    'Dictionnaire introuvable.', 'Identifiant de partie manquant.', 'Date de partie invalide.',
                ], true) => $erreur->getMessage(),
                default => 'Impossible d’enregistrer la partie.',
            };
            error_log(get_class($erreur) . ' : ' . $message);
            $_SESSION['partie']['id_partie'] = null;
            $_SESSION['partie']['enregistrement'] = 'echec';
        }
    }
}
